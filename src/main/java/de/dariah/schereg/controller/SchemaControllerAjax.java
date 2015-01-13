package de.dariah.schereg.controller;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.exolab.castor.xml.schema.reader.SchemaReader;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartFile;
import org.xml.sax.InputSource;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;

import de.dariah.schereg.base.model.File;
import de.dariah.schereg.base.model.Mapping;
import de.dariah.schereg.base.model.Schema;
import de.dariah.schereg.base.model.validation.SchemaValidator;
import de.dariah.schereg.base.service.FileService;
import de.dariah.schereg.base.service.MappingService;
import de.dariah.schereg.base.service.SchemaService;
import de.dariah.schereg.controller.base.BaseController;
import de.dariah.schereg.importers.schema.XmlSchemaImporter;
import de.dariah.schereg.util.ContextService;
import de.dariah.schereg.util.ImmutableDateTimeSerializer;
import de.dariah.schereg.util.ScheRegConstants;
import de.dariah.schereg.util.ViewHelper;
import de.dariah.schereg.view.ModelToJsonConverter;

@Controller
@RequestMapping("/schema/ajax")
@Transactional
public class SchemaControllerAjax extends BaseController {
	
	@Autowired private SchemaService schemaService;
	@Autowired private MappingService mappingService;
	@Autowired private FileService fileService;
	
	@Autowired private SchemaValidator schemaValidator;
	
	@RequestMapping(method = RequestMethod.GET, value = "/delete", produces = "application/json; charset=utf-8")
	@Secured(value="ROLE_CONTRIBUTOR")
	public @ResponseBody String deleteSchema(@RequestParam int id, Model model, Locale locale) {	
		logger.debug(String.format("Command received to delete schema [%s]", id));
		
		Collection<Mapping> assocMappings = mappingService.getMappingsBySchema(id);		
		GsonBuilder bldr = new GsonBuilder();
		Gson gson = bldr.create();
		
		JsonObject result = new JsonObject();

		if (assocMappings==null || assocMappings.size()==0) {
			schemaService.removeSchema(id);
			result.addProperty("success", true);
			result.addProperty("message_type", "success");
			result.addProperty("message_head", messageSource.getMessage("~schemaRegistry.message.schema.delete.success.head", null, locale));
			result.addProperty("message_body", messageSource.getMessage("~schemaRegistry.message.schema.delete.success.body", null, locale));
		} else {
			result.addProperty("success", false);
			result.addProperty("message_type", "warn");
			result.addProperty("message_head", messageSource.getMessage("~schemaRegistry.message.schema.delete.fail.mappings.head", null, locale));
			result.addProperty("message_body", messageSource.getMessage("~schemaRegistry.message.schema.delete.fail.mappings.body", null, locale));
		}
		
		return gson.toJson(result);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/deleteFile")
	@Secured(value="ROLE_CONTRIBUTOR")
	public @ResponseBody String deleteFile(@RequestParam int id, Model model) {	
		logger.debug(String.format("Command received to delete file [%s]", id));
		
		fileService.removeFile(id);
		
		GsonBuilder bldr = new GsonBuilder();
		Gson gson = bldr.create();
		JsonObject result = new JsonObject();
		result.addProperty("deleted", 1);
		result.addProperty("errors", "");

		return gson.toJson(result);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/getNewForm")
	@Secured(value="ROLE_CONTRIBUTOR")
	public String getNewForm(@RequestParam int id, Model model) {	
		if (id > 0) {
			model.addAttribute("schema", schemaService.getSchema(id));		
		} else {
			model.addAttribute("schema", new Schema());
		}
		model.addAttribute("actionPath", "/schema/ajax/save");
		model.addAttribute("schemaTypes", schemaService.getSupportedSchemaTypes());
		return "schema/edit/editForm";
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/loadRow")
	public String getRow(@RequestParam int id, Model model) {
		Schema schema = schemaService.getSchema(id);
		model.addAttribute("schema", schema);
		return "schema/list/tr";
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/getSourceSelectionForm")
	@Secured(value="ROLE_CONTRIBUTOR")
	public String getSourceSelectionForm(Model model) {
		return "schema/edit/sourceForm";
	}
	
	/**
	 * Assigns custom validators to the provided WebDataBinder 
	 * @param binder Binds request parameters to objects
	 */
	@InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.setValidator(schemaValidator);
    }
	
	
	@RequestMapping(method = RequestMethod.POST, value = "/prepareSchema", produces = "application/json; charset=utf-8")
	@Secured(value="ROLE_CONTRIBUTOR")
	public @ResponseBody String prepareSchema(MultipartHttpServletRequest request, Model model, Locale locale) {
				
		MultiValueMap<String, MultipartFile> multipartMap = request.getMultiFileMap();
		CommonsMultipartFile file = null;
		if (multipartMap != null && multipartMap.size()>0 && multipartMap.containsKey("file"))
		{
			List<MultipartFile> fileList = multipartMap.get("file");
			if (fileList.size()==1 && fileList.get(0)!=null) {
				file = (CommonsMultipartFile)fileList.get(0);
				
				// 'empty' file
				if (file.getOriginalFilename() == null || file.getSize() == 0) {
					file = null;
				}
				
			}
		}
		
		File tmpFile = new File();
		DateTime now = DateTime.now();
		try {
			tmpFile.setFiletype(fileService.getFileType("XML Schema"));
			tmpFile.setFilename(file.getOriginalFilename());
			tmpFile.setFilestrem(file.getBytes());
			
			tmpFile.setCreated(now);
			tmpFile.setModified(now);
		} catch (Exception e) {
			e.printStackTrace();
		}

		fileService.saveFile(tmpFile);
	
		GsonBuilder bldr = new GsonBuilder();
		Gson gson = bldr.create();
		
		JsonObject result = new JsonObject();
		result.addProperty("success", 1);
				
		// Prepare for multi-file schemas
		JsonArray jsonFiles = new JsonArray();
		JsonObject jsonFile = new JsonObject();
		jsonFile.addProperty("id", tmpFile.getId());
		jsonFile.addProperty("fileName", tmpFile.getFilename());
		jsonFile.addProperty("fileType", fileService.getFileType(tmpFile.getFiletype().getId()).getName());
		jsonFile.addProperty("fileSize", ViewHelper.humanReadableByteCount(tmpFile.getFilestrem().length, false));
		jsonFile.addProperty("created", tmpFile.getCreated().toString());
		jsonFile.addProperty("deleteLink", "/ajax/deleteFile?id=" + tmpFile.getId());
		jsonFile.addProperty("validateLink", "/ajax/validateFile?id=" + tmpFile.getId());
		jsonFiles.add(jsonFile);
		
		result.add("files", jsonFiles);
		
		return gson.toJson(result);
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/refresh", produces = "application/json; charset=utf-8")
	public @ResponseBody String refreshAsync(@RequestParam String items, Locale locale) {
		
		GsonBuilder bldr = new GsonBuilder();
		bldr.registerTypeAdapter(DateTime.class, new ImmutableDateTimeSerializer());
		
		Gson gson = bldr.create();
		Type collectionType = new TypeToken<Collection<Schema>>(){}.getType();
		Collection<Schema> schemas = gson.fromJson(items, collectionType);

		Map<Integer, JsonElement> result = new HashMap<Integer, JsonElement>();
		
		// Updates and deletions
		if (schemas != null && schemas.size()>0) {
			for (Schema schema : schemas) {
				if (schema.getId() <= 0) {
					continue;
				}
				
				Schema dbSchema = schemaService.getSchema(schema.getId());
				if (dbSchema != null && dbSchema.getModified().isAfter(schema.getModified())) {
					result.put(schema.getId(), ModelToJsonConverter.getSchemaAsJson(dbSchema, ScheRegConstants.MODEL_JSON_STATUS_UPDATE, locale));
				} else if (dbSchema == null) {
					result.put(schema.getId(), ModelToJsonConverter.getSchemaAsJson(schema, ScheRegConstants.MODEL_JSON_STATUS_DELETE, locale));
				} else {
					result.put(schema.getId(), new JsonPrimitive(ScheRegConstants.MODEL_JSON_STATUS_NOCHANGE));
				}
			}
		}
		
		// Added schemas
		DateTime maxModified = DateTime.now().minusMinutes(5);
		Collection<Schema> newSchemas = schemaService.getSchemasCreatedAfter(maxModified);
		if (newSchemas != null && newSchemas.size() > 0) {
			for (Schema newSchema : newSchemas) {
				if (!result.containsKey(newSchema.getId())) {
					result.put(newSchema.getId(), ModelToJsonConverter.getSchemaAsJson(newSchema, ScheRegConstants.MODEL_JSON_STATUS_ADD, locale));
				}
			}
		}	
		
		return gson.toJson(result.values());
	}
	
	@RequestMapping(method=POST, value="/save", produces = "application/json; charset=utf-8")
	@Secured(value="ROLE_CONTRIBUTOR")
	public @ResponseBody String saveSchema(@Valid Schema schema, BindingResult bindingResult, HttpServletResponse response, Model model, Locale locale) {
		// Cleanup invalid references created by form
		if (schema.getFile() != null && schema.getFile().getId() <= 0) {
			schema.setFile(null);
		}
		
		GsonBuilder bldr = new GsonBuilder();
		Gson gson = bldr.create();
		
		JsonObject result = new JsonObject();
		result.addProperty("success", !bindingResult.hasErrors());
		result.addProperty("errorCount", bindingResult.getErrorCount());
	
		if (bindingResult.hasErrors()) {
			result.add("errors", getJsonErrorList(bindingResult, locale));
		} else {
			boolean isNew = (schema.getId() <= 0);
			int preExistingFileId = schemaService.getFileBySchema(schema.getId());
			int newFileId = schema.getFile().getId();
			
			Schema s = schema;
			if (!isNew) {
				s = schemaService.getSchema(schema.getId());
				s.setName(schema.getName());
				s.setDescription(schema.getDescription());
				s.setFile(schema.getFile());
			}
			
			s.setState(ScheRegConstants.STATE_OK);
			schemaService.saveSchema(s);
			
			result.addProperty("id", s.getId());
			
			if (s.getFile() != null && s.getFile().getId() > 0) {
				boolean doImport = true;
				
				if (!isNew) {
					if (preExistingFileId == newFileId) {
						doImport = false;
					}
				}
				
				if (doImport)
				{
					File file = fileService.getFile(s.getFile().getId());
					
					SecurityContext context = SecurityContextHolder.getContext();
					XmlSchemaImporter importer = new XmlSchemaImporter(s.getId(), new InputSource(new ByteArrayInputStream(file.getFilestrem())), context);
					importer.setUp();
					importer.start();
				}
			}
		}		
		return gson.toJson(result);
	}

	@RequestMapping(method = RequestMethod.GET, value = "/validateFile", produces = "application/json; charset=utf-8")
	public @ResponseBody String validateTmpFile(@RequestParam int id, Model model, Locale locale) {
		File file = fileService.getFile(id);
		
		GsonBuilder bldr = new GsonBuilder();
		Gson gson = bldr.create();
		
		JsonObject result = new JsonObject();
		
		InputSource src = new InputSource(new ByteArrayInputStream(file.getFilestrem()));
				
		try {
			SchemaReader xmlSchemaReader = new SchemaReader(src);
			xmlSchemaReader.setValidation(false);
			xmlSchemaReader.read();
			
			file.setValidated(true);
			fileService.saveFile(file);
			
			result.addProperty("success", true);
			result.addProperty("message_type", "success");
			result.addProperty("message_head", messageSource.getMessage("~schemaRegistry.dialogs.file.validationsucceeded.head", null, locale));
			result.addProperty("message_body", messageSource.getMessage("~schemaRegistry.dialogs.file.validationsucceeded.body", null, locale));
		
		} catch (IOException e) {
			logger.error(String.format("An IOException occured while validating input file [id: %d]", file.getId()), e);
			
			result.addProperty("success", false);
			result.addProperty("message_type", "warn");
			result.addProperty("message_head", messageSource.getMessage("~schemaRegistry.dialogs.file.validationfailed.head", null, locale));
			result.addProperty("message_body", messageSource.getMessage("~schemaRegistry.dialogs.file.validationfailed.body", new String [] {e.getMessage()}, locale));
		}

		return gson.toJson(result);
	}
	
}
