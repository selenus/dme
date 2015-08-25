package eu.dariah.de.minfba.schereg.controller.editor;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.joda.time.DateTime;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import eu.dariah.de.minfba.core.metamodel.interfaces.Element;
import eu.dariah.de.minfba.core.metamodel.interfaces.Schema;
import eu.dariah.de.minfba.core.metamodel.interfaces.Terminal;
import eu.dariah.de.minfba.core.metamodel.xml.XmlSchema;
import eu.dariah.de.minfba.core.web.controller.BaseTranslationController;
import eu.dariah.de.minfba.core.web.pojo.ModelActionPojo;
import eu.dariah.de.minfba.core.web.pojo.MessagePojo;
import eu.dariah.de.minfba.schereg.exception.SchemaImportException;
import eu.dariah.de.minfba.schereg.importer.SchemaImportWorker;
import eu.dariah.de.minfba.schereg.pojo.LogEntryPojo;
import eu.dariah.de.minfba.schereg.pojo.LogEntryPojo.LogType;
import eu.dariah.de.minfba.schereg.service.interfaces.ElementService;
import eu.dariah.de.minfba.schereg.service.interfaces.SchemaService;

@Controller
@RequestMapping(value="/schema/editor/{schemaId}")
@SessionAttributes({"sample", "log"})
public class MainEditorController extends BaseTranslationController implements InitializingBean {
	private static Map<String, String> temporaryFilesMap = new HashMap<String, String>();
	
	@Autowired private SchemaService schemaService;
	@Autowired private ElementService elementService;
	@Autowired private SchemaImportWorker importWorker;
	
	@Value(value="${paths.tmpUploadDir:/tmp}")
	private String tmpUploadDirPath;
	
	public MainEditorController() {
		super("schemaEditor");
	}
		
	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		Files.createDirectories(Paths.get(tmpUploadDirPath));
	}
	
	@RequestMapping(method=GET, value={"/", ""})
	public String getEditor(@PathVariable String schemaId, Model model, @ModelAttribute String sample, Locale locale) {
		model.addAttribute("schema", schemaService.findSchemaById(schemaId));
		return "schemaEditor";
	}
	
	@RequestMapping(method=GET, value={"/forms/import"})
	public String getImportForm(@PathVariable String schemaId, Model model, Locale locale) {
		model.addAttribute("actionPath", "/schema/editor/" + schemaId + "/async/import");
		model.addAttribute("schema", schemaService.findSchemaById(schemaId));
		return "schemaEditor/form/import";
	}
	
	@RequestMapping(method=GET, value={"/forms/fileupload"})
	public String getImportForm(Model model, Locale locale) {
		return "common/fileupload";
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/async/upload", produces = "application/json; charset=utf-8")
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
	
	@RequestMapping(method=GET, value={"/async/file/delete/{fileId}"})
	public @ResponseBody ModelActionPojo deleteImportedFile(@PathVariable String fileId, Model model, Locale locale) {
		if (temporaryFilesMap.containsKey(fileId)) {
			temporaryFilesMap.remove(fileId);
		}
		return new ModelActionPojo(true);
	}
	
	@RequestMapping(method=GET, value={"/async/file/validate/{fileId}"})
	public @ResponseBody ModelActionPojo validateImportedFile(@PathVariable String fileId, Model model, Locale locale) throws SchemaImportException {
		ModelActionPojo result = new ModelActionPojo();
				
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
	
	@RequestMapping(method=POST, value={"/async/import"}, produces = "application/json; charset=utf-8")
	public @ResponseBody ModelActionPojo importSchemaElements(@RequestParam String schemaId, @RequestParam(value="file.id") String fileId, 
			@RequestParam(value="schema_root") Integer schemaRoot, Locale locale) {
		ModelActionPojo result = new ModelActionPojo();
		try {
			if (temporaryFilesMap.containsKey(fileId)) {
				importWorker.importSchema(temporaryFilesMap.remove(fileId), schemaId, schemaRoot);
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
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/getHierarchy")
	public @ResponseBody Element getHierarchy(@PathVariable String schemaId, Model model, Locale locale) {
		return elementService.findRootBySchemaId(schemaId, true);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/getTerminals")
	public @ResponseBody List<? extends Terminal> getTerminals(@PathVariable String schemaId) {
		Schema s = schemaService.findSchemaById(schemaId);
		if (s instanceof XmlSchema) {	
			return ((XmlSchema)s).getTerminals();
		}
		return null;
	}
	
	@RequestMapping(method=POST, value={"/async/applySample"}, produces = "application/json; charset=utf-8")
	public @ResponseBody ModelActionPojo applySample(@PathVariable String schemaId, @RequestParam String sample, Model model, Locale locale) {
		// TODO: Persist this in db for the session or user and store only an id in the session
		model.addAttribute("sample", sample);
		this.addLogEntry(model, schemaId, LogType.SUCCESS, "~ Sample set for your current session");
		
		return new ModelActionPojo(true);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/getLog")
	public @ResponseBody Collection<LogEntryPojo> getLog(@PathVariable String schemaId, @RequestParam(defaultValue="10") Integer maxEntries, @RequestParam(required=false) Long tsMin, Model model) {
		List<LogEntryPojo> log = getLog(model, schemaId);
		if (tsMin!=null && log.size()>0 && log.get(0).getNumericTimestamp()<=tsMin) {
			return new ArrayList<LogEntryPojo>();
		}
		
		if (log.size() > maxEntries) {
			return log.subList(0, maxEntries);
		}
		return log;
	}
	
	private List<LogEntryPojo> getLog(Model model, String schemaId) {
		List<LogEntryPojo> result = this.checkLog(model, schemaId).get(schemaId);
		Collections.sort(result);
		Collections.reverse(result);
		return result;
	}
	
	private void addLogEntry(Model model, String schemaId, LogType type, String message) {
		LogEntryPojo entry = new LogEntryPojo();
		entry.setTimestamp(DateTime.now());
		entry.setLogType(type);
		entry.setMessage(message);
		
		this.checkLog(model, schemaId).get(schemaId).add(entry);
	}
	
	private Map<String, List<LogEntryPojo>> checkLog(Model model, String schemaId) {
		if (model.asMap().get("log")==null) {
			model.addAttribute("log", new HashMap<String, List<LogEntryPojo>>());
		}
		
		Map<String, List<LogEntryPojo>> log = (Map<String, List<LogEntryPojo>>)model.asMap().get("log");
		if (!log.containsKey(schemaId)) {
			log.put(schemaId, new ArrayList<LogEntryPojo>());
		}
		return log;
	}
		
	public static String humanReadableByteCount(long bytes, boolean si) {
	    int unit = si ? 1000 : 1024;
	    if (bytes < unit) return bytes + " B";
	    int exp = (int) (Math.log(bytes) / Math.log(unit));
	    String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
	    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}
}