package eu.dariah.de.minfba.schereg.controller;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import javax.validation.Valid;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
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

import eu.dariah.de.minfba.core.metamodel.BaseSchema;
import eu.dariah.de.minfba.core.metamodel.BaseTerminal;
import eu.dariah.de.minfba.core.metamodel.interfaces.Schema;
import eu.dariah.de.minfba.core.web.controller.BaseTranslationController;
import eu.dariah.de.minfba.core.web.controller.DataTableList;
import eu.dariah.de.minfba.core.web.pojo.ModelActionPojo;
import eu.dariah.de.minfba.schereg.importer.SchemaImportWorker;
import eu.dariah.de.minfba.schereg.service.SchemaService;

@Controller
@RequestMapping(value="/schema")
public class SchemaController extends BaseTranslationController implements InitializingBean {
	@Autowired private SchemaService schemaService;
	@Autowired private SchemaImportWorker importWorker;
	
	private Map<String, String> temporaryFilesMap = new HashMap<String, String>();
	
	@Value(value="${paths.tmpUploadDir:/tmp}")
	private String tmpUploadDirPath;
	
	
	public SchemaController() {
		super("schema");
	}
	
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		if (!Files.exists(Paths.get(tmpUploadDirPath))) {
			Files.createDirectories(Paths.get(tmpUploadDirPath));
		}
	}
	
	@RequestMapping(value = {"", "/"}, method = RequestMethod.GET)
	public String getList(Model model) {
		return "schema/home";
	}
	
	
	
	@RequestMapping(method=GET, value={"/forms/add"})
	public String getAddForm(Model model, Locale locale) {
		model.addAttribute("actionPath", "/schema/async/save");
		model.addAttribute("schema", new BaseSchema<BaseTerminal>());
		return "schema/form/edit";
	}
	
	@RequestMapping(method=GET, value={"/forms/edit/{id}"})
	public String getEditForm(@PathVariable String id, Model model, Locale locale) {
		model.addAttribute("actionPath", "/schema/async/save");
		model.addAttribute("schema", schemaService.findSchemaById(id));
		return "schema/form/edit";
	}
	
	@RequestMapping(method=GET, value={"/forms/import"})
	public String getImportForm(Model model, Locale locale) {
		return "schema/form/import";
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/getData")
	public @ResponseBody DataTableList<Schema> getData(Model model, Locale locale) {
		List<Schema> schemas = schemaService.findAllSchemas();		
		return new DataTableList<Schema>(schemas);
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/async/import", produces = "application/json; charset=utf-8")
	public @ResponseBody JsonNode prepareSchema(MultipartHttpServletRequest request, Model model, Locale locale) throws IOException {
		
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
	
	@RequestMapping(method=GET, value={"/async/file/delete/{id}"})
	public @ResponseBody ModelActionPojo deleteImportedFile(@PathVariable String id, Model model, Locale locale) {
		if (temporaryFilesMap.containsKey(id)) {
			temporaryFilesMap.remove(id);
		}
		return new ModelActionPojo(true);
	}
	
	@RequestMapping(method=GET, value={"/async/file/validate/{id}"})
	public @ResponseBody ModelActionPojo validateImportedFile(@PathVariable String id, Model model, Locale locale) {		
		return new ModelActionPojo(true);
	}
	
	@RequestMapping(method=POST, value={"/async/save"}, produces = "application/json; charset=utf-8")
	public @ResponseBody ModelActionPojo saveSchema(@Valid BaseSchema<BaseTerminal> schema, BindingResult bindingResult) {
		ModelActionPojo result = new ModelActionPojo(true); //this.getActionResult(bindingResult, locale);
		if (schema.getId().isEmpty()) {
			schema.setId(null);
		}
		schemaService.saveSchema(schema);
		return result;
	}
	
	@RequestMapping(method=GET, value={"/async/delete/{id}"}, produces = "application/json; charset=utf-8")
	public @ResponseBody ModelActionPojo deleteSchema(@PathVariable String id) {
		ModelActionPojo result;
		if (id!=null && !id.isEmpty()) {
			schemaService.deleteSchemaById(id);
			result = new ModelActionPojo(true);
		} else {
			result = new ModelActionPojo(false);
		}		
		return result;
	}
	
	public static String humanReadableByteCount(long bytes, boolean si) {
	    int unit = si ? 1000 : 1024;
	    if (bytes < unit) return bytes + " B";
	    int exp = (int) (Math.log(bytes) / Math.log(unit));
	    String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
	    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}
}
