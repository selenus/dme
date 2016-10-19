package eu.dariah.de.minfba.schereg.controller.editors;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import de.dariah.samlsp.model.pojo.AuthPojo;
import eu.dariah.de.minfba.core.metamodel.Nonterminal;
import eu.dariah.de.minfba.core.metamodel.function.DescriptionGrammarImpl;
import eu.dariah.de.minfba.core.metamodel.interfaces.Element;
import eu.dariah.de.minfba.core.metamodel.interfaces.Identifiable;
import eu.dariah.de.minfba.core.metamodel.interfaces.Mapping;
import eu.dariah.de.minfba.core.metamodel.interfaces.Schema;
import eu.dariah.de.minfba.core.metamodel.interfaces.Terminal;
import eu.dariah.de.minfba.core.metamodel.serialization.SerializableSchemaContainer;
import eu.dariah.de.minfba.core.metamodel.xml.XmlSchema;
import eu.dariah.de.minfba.core.web.pojo.ModelActionPojo;
import eu.dariah.de.minfba.core.web.pojo.MessagePojo;
import eu.dariah.de.minfba.schereg.controller.base.BaseMainEditorController;
import eu.dariah.de.minfba.schereg.exception.GenericScheregException;
import eu.dariah.de.minfba.schereg.exception.SchemaImportException;
import eu.dariah.de.minfba.schereg.importer.SchemaImportWorker;
import eu.dariah.de.minfba.schereg.model.MappableElement;
import eu.dariah.de.minfba.schereg.model.PersistedSession;
import eu.dariah.de.minfba.schereg.model.RightsContainer;
import eu.dariah.de.minfba.schereg.pojo.AuthWrappedPojo;
import eu.dariah.de.minfba.schereg.pojo.converter.AuthWrappedPojoConverter;
import eu.dariah.de.minfba.schereg.service.ElementServiceImpl;
import eu.dariah.de.minfba.schereg.service.interfaces.IdentifiableService;
import eu.dariah.de.minfba.schereg.service.interfaces.MappingService;

@Controller
@RequestMapping(value="/schema/editor/{entityId}/")
public class SchemaEditorController extends BaseMainEditorController implements InitializingBean {
	private static Map<String, String> temporaryFilesMap = new HashMap<String, String>();
	
	@Autowired private SchemaImportWorker importWorker;
	@Autowired private AuthWrappedPojoConverter authPojoConverter;
	@Autowired private MappingService mappingService;
	
	@Autowired private IdentifiableService identifiableService;
	
	@Value(value="${paths.tmpUploadDir:/tmp}")
	private String tmpUploadDirPath;
	
	public SchemaEditorController() {
		super("schemaEditor");
	}
		
	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		Files.createDirectories(Paths.get(tmpUploadDirPath));
	}
	
	@RequestMapping(value="/query/{query}", method=RequestMethod.GET)
	public @ResponseBody List<Identifiable> queryElements(@PathVariable String entityId, @PathVariable String query) {
		return identifiableService.findByNameAndSchemaId(query, entityId, new Class<?>[] { Nonterminal.class, DescriptionGrammarImpl.class });
	}
	
	@RequestMapping(method=GET, value="")
	public String getEditor(@PathVariable String entityId, Model model, @ModelAttribute String sample, Locale locale, HttpServletRequest request, HttpServletResponse response) throws IOException {
		AuthPojo auth = authInfoHelper.getAuth(request);
		RightsContainer<Schema> schema = schemaService.findByIdAndAuth(entityId, auth);
		if (schema==null) {
			response.sendRedirect("/registry/");
			return null;
		}
		
		model.addAttribute("schema", authPojoConverter.convert(schema, auth.getUserId()));
		
		List<RightsContainer<Mapping>> mappings = mappingService.getMappings(entityId);
		model.addAttribute("mapped", mappings!=null && mappings.size()>0);
		try {
			PersistedSession s = sessionService.accessOrCreate(entityId, request.getSession().getId(), auth.getUserId(), messageSource, locale);
			model.addAttribute("session", s);
		} catch (Exception e) {
			logger.error("Failed to load/initialize persisted session", e);
		} 
		return "schemaEditor";
	}
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method=GET, value={"/forms/edit"})
	public String getEditForm(@PathVariable String entityId, Model model, Locale locale, HttpServletRequest request, HttpServletResponse response) {
		if (!schemaService.getUserCanWriteEntity(entityId, authInfoHelper.getAuth(request).getUserId())) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return null;
		}
		RightsContainer<Schema> schema = schemaService.findByIdAndAuth(entityId, authInfoHelper.getAuth(request));
		model.addAttribute("actionPath", "/schema/async/save");
		model.addAttribute("schema", schema.getElement());
		model.addAttribute("readOnly", schema.isReadOnly());
		return "schema/form/edit";
	}
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method=GET, value={"/async/delete"}, produces = "application/json; charset=utf-8")
	public @ResponseBody ModelActionPojo deleteSchema(@PathVariable String entityId, HttpServletRequest request, HttpServletResponse response) {
		if (!schemaService.getUserCanWriteEntity(entityId, authInfoHelper.getAuth(request).getUserId())) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return new ModelActionPojo(false);
		}
		ModelActionPojo result;
		if (entityId!=null && !entityId.isEmpty()) {
			schemaService.deleteSchemaById(entityId, authInfoHelper.getAuth(request));
			result = new ModelActionPojo(true);
		} else {
			result = new ModelActionPojo(false);
		}		
		return result;
	}
	
	@RequestMapping(method=GET, value={"/state"}, produces = "application/json; charset=utf-8")
	public @ResponseBody ModelActionPojo getSchemaState(@PathVariable String entityId, HttpServletRequest request) {
		ModelActionPojo result;
		if (entityId!=null && !entityId.isEmpty()) {
			result = new ModelActionPojo(true);
		} else {
			result = new ModelActionPojo(false);
		}		
		ObjectNode jsonState = objectMapper.createObjectNode();
		if (importWorker.isBeingProcessed(entityId)) {
			jsonState.put("processing", true);
			jsonState.put("ready", false);
		} else {
			jsonState.put("processing", false);
			jsonState.put("ready", true);
		}
		jsonState.put("error", false);
		result.setPojo(jsonState);
		
		return result;
	}
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method=GET, value={"/forms/import"})
	public String getImportForm(@PathVariable String entityId, Model model, Locale locale, HttpServletRequest request, HttpServletResponse response) {
		AuthPojo auth = authInfoHelper.getAuth(request);
		if(!schemaService.getUserCanWriteEntity(entityId, auth.getUserId())) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return null;
		}
		model.addAttribute("actionPath", "/schema/editor/" + entityId + "/async/import");
		model.addAttribute("schema", schemaService.findSchemaById(entityId));
		return "schemaEditor/form/import";
	}
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method=GET, value={"/forms/fileupload"})
	public String getImportForm(Model model, Locale locale) {
		return "common/fileupload";
	}
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method = RequestMethod.GET, value = "/form/createRoot")
	public String getNewNonterminalForm(@PathVariable String entityId, Model model, Locale locale, HttpServletRequest request, HttpServletResponse response) {
		AuthPojo auth = authInfoHelper.getAuth(request);
		if(!schemaService.getUserCanWriteEntity(entityId, auth.getUserId())) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return null;
		}
		model.addAttribute("element", new Nonterminal());
		model.addAttribute("availableTerminals", schemaService.getAvailableTerminals(entityId));
		model.addAttribute("actionPath", "/schema/editor/" + entityId + "/async/saveNewRoot");
		return "elementEditor/form/edit_nonterminal";
	}
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method = RequestMethod.POST, value = "/async/saveNewRoot")
	public @ResponseBody ModelActionPojo saveNonterminal(@PathVariable String entityId, @Valid Nonterminal element, BindingResult bindingResult, Locale locale, HttpServletRequest request, HttpServletResponse response) {
		AuthPojo auth = authInfoHelper.getAuth(request);
		if(!schemaService.getUserCanWriteEntity(entityId, auth.getUserId())) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return new ModelActionPojo(false);
		}
		ModelActionPojo result = this.getActionResult(bindingResult, locale);
		if (result.isSuccess()) {
			element.setEntityId(entityId);
			elementService.saveOrReplaceRoot(entityId, element, authInfoHelper.getAuth(request));
		}		
		return result;
	}
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method = RequestMethod.POST, value = "/async/upload", produces = "application/json; charset=utf-8")
	public @ResponseBody JsonNode prepareSchema(@PathVariable String entityId, MultipartHttpServletRequest request, Model model, Locale locale, HttpServletResponse response) throws IOException {
		AuthPojo auth = authInfoHelper.getAuth(request);
		if(!schemaService.getUserCanWriteEntity(entityId, auth.getUserId())) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return null;
		}
		
		MultiValueMap<String, MultipartFile> multipartMap = request.getMultiFileMap();
		CommonsMultipartFile file = null;
		if (multipartMap != null && multipartMap.size()>0 && multipartMap.containsKey("file")) {
			List<MultipartFile> fileList = multipartMap.get("file");
			if (fileList.size()==1 && fileList.get(0)!=null) {
				file = (CommonsMultipartFile)fileList.get(0);
				// 'empty' file
				if (file.getOriginalFilename() == null || file.getSize() == 0) {
					file = null;
				}
			}
		}

		String tmpId = UUID.randomUUID().toString();
		String tmpFilePath = String.format("%s/%s_%s", tmpUploadDirPath, tmpId, file.getOriginalFilename());
		Files.write(Paths.get(tmpFilePath), file.getBytes());
		
		temporaryFilesMap.put(tmpId, tmpFilePath);
		
		ArrayNode filesNode = objectMapper.createArrayNode();
		ObjectNode fileNode = objectMapper.createObjectNode();
		fileNode.put("id", tmpId);
		fileNode.put("fileName", file.getOriginalFilename());
		fileNode.put("fileSize", humanReadableByteCount(file.getBytes().length, false));
		fileNode.put("deleteLink", "/async/file/delete/" + tmpId);
		fileNode.put("validateLink", "/async/file/validate/" + tmpId);

		filesNode.add(fileNode);
		
		ObjectNode result = objectMapper.createObjectNode();
		result.put("success", true);
		result.put("files", filesNode);
		
		return result;
	}
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method=GET, value={"/async/file/delete/{fileId}"})
	public @ResponseBody ModelActionPojo deleteImportedFile(@PathVariable String entityId, @PathVariable String fileId, Model model, Locale locale, HttpServletRequest request, HttpServletResponse response) {
		if (!schemaService.getUserCanWriteEntity(entityId, authInfoHelper.getAuth(request).getUserId())) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return null;
		}
		if (temporaryFilesMap.containsKey(fileId)) {
			temporaryFilesMap.remove(fileId);
		}
		return new ModelActionPojo(true);
	}
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method=GET, value={"/async/file/validate/{fileId}"})
	public @ResponseBody ModelActionPojo validateImportedFile(@PathVariable String entityId, @PathVariable String fileId, Model model, Locale locale, HttpServletRequest request, HttpServletResponse response) throws SchemaImportException {
		ModelActionPojo result = new ModelActionPojo();
		if (!schemaService.getUserCanWriteEntity(entityId, authInfoHelper.getAuth(request).getUserId())) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return new ModelActionPojo(false);
		}		
		if (temporaryFilesMap.containsKey(fileId)) {
			List<? extends Terminal> rootTerminals = importWorker.getPossibleRootTerminals(temporaryFilesMap.get(fileId));
			if (rootTerminals!=null) {
				result.setSuccess(true);
				MessagePojo msg = new MessagePojo("success", 
						messageSource.getMessage("~eu.dariah.de.minfba.common.view.forms.file.validationsucceeded.head", null, locale), 
						messageSource.getMessage("~eu.dariah.de.minfba.common.view.forms.file.validationsucceeded.body", null, locale));
				result.setMessage(msg);
				result.setPojo(rootTerminals);
				return result;
			}
		}
		result.setSuccess(false);
		// TODO: Error message
		MessagePojo msg = new MessagePojo("danger", 
				messageSource.getMessage("~eu.dariah.de.minfba.common.view.forms.file.validationfailed.head", null, locale), 
				messageSource.getMessage("~eu.dariah.de.minfba.common.view.forms.file.validationfailed.body", null, locale));
		result.setMessage(msg);
		return result;
	}
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method=POST, value={"/async/import"}, produces = "application/json; charset=utf-8")
	public @ResponseBody ModelActionPojo importSchemaElements(@RequestParam String entityId, @RequestParam(value="file.id") String fileId, 
			@RequestParam(value="schema_root") Integer schemaRoot, Locale locale, HttpServletRequest request, HttpServletResponse response) {
		AuthPojo auth = authInfoHelper.getAuth(request);
		if(!schemaService.getUserCanWriteEntity(entityId, auth.getUserId())) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return new ModelActionPojo(false);
		}
		ModelActionPojo result = new ModelActionPojo();
		try {
			if (temporaryFilesMap.containsKey(fileId)) {
				importWorker.importSchema(temporaryFilesMap.remove(fileId), entityId, schemaRoot, authInfoHelper.getAuth(request));
				result.setSuccess(true);
				return result;
			}
		} catch (Exception e) {
			MessagePojo msg = new MessagePojo("danger", 
					messageSource.getMessage("~eu.dariah.de.minfba.common.view.forms.file.generalerror.head", null, locale), 
					messageSource.getMessage("~eu.dariah.de.minfba.common.view.forms.file.generalerror.body", new Object[] {e.getLocalizedMessage()}, locale));
			result.setMessage(msg);
		}
		result.setSuccess(false);
		return result;
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/export")
	public @ResponseBody ModelActionPojo exportSchema(@PathVariable String entityId, Model model, Locale locale) {
		Schema s = schemaService.findSchemaById(entityId);
		Element r = elementService.findRootBySchemaId(entityId, true);
		
		SerializableSchemaContainer sp = new SerializableSchemaContainer();
		sp.setSchema(s);
		sp.setRoot(r);
		
		ModelActionPojo result = new ModelActionPojo(true);
		/*try {
			result.setPojo(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(sp));
		} catch (JsonProcessingException e) {*/
			result.setPojo(sp);
		/*}*/
		return result;
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/getHierarchy")
	public @ResponseBody Element getHierarchy(@PathVariable String entityId, Model model, Locale locale, HttpServletResponse response) throws IOException {
		Element result = elementService.findRootBySchemaId(entityId, true);
		if (result==null) {
			response.getWriter().print("null");
			response.setContentType("application/json");
		}
		return result;
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/getRendered")
	public @ResponseBody MappableElement getRenderedHierarchy(@PathVariable String entityId, Model model, Locale locale, HttpServletResponse response) throws IOException {
		Element e = elementService.findRootBySchemaId(entityId, true);
		MappableElement result =  ElementServiceImpl.convertElement(e, true);
		if (result==null) {
			response.getWriter().print("null");
			response.setContentType("application/json");
		}
		return result;
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/getTerminals")
	public @ResponseBody List<? extends Terminal> getTerminals(@PathVariable String entityId) {
		Schema s = schemaService.findSchemaById(entityId);
		if (s instanceof XmlSchema) {	
			return ((XmlSchema)s).getTerminals();
		}
		return null;
	}
}