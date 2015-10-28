package eu.dariah.de.minfba.schereg.controller.schemaeditor;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
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
import eu.dariah.de.minfba.core.metamodel.interfaces.Element;
import eu.dariah.de.minfba.core.metamodel.interfaces.Schema;
import eu.dariah.de.minfba.core.metamodel.interfaces.Terminal;
import eu.dariah.de.minfba.core.metamodel.serialization.SerializableSchemaContainer;
import eu.dariah.de.minfba.core.metamodel.xml.XmlSchema;
import eu.dariah.de.minfba.core.util.Stopwatch;
import eu.dariah.de.minfba.core.web.pojo.ModelActionPojo;
import eu.dariah.de.minfba.processing.model.base.Resource;
import eu.dariah.de.minfba.processing.service.xml.XmlStringProcessingService;
import eu.dariah.de.minfba.core.web.pojo.MessagePojo;
import eu.dariah.de.minfba.schereg.controller.base.BaseScheregController;
import eu.dariah.de.minfba.schereg.exception.GenericScheregException;
import eu.dariah.de.minfba.schereg.exception.SchemaImportException;
import eu.dariah.de.minfba.schereg.importer.SchemaImportWorker;
import eu.dariah.de.minfba.schereg.model.MappableElement;
import eu.dariah.de.minfba.schereg.model.PersistedSession;
import eu.dariah.de.minfba.schereg.pojo.LogEntryPojo;
import eu.dariah.de.minfba.schereg.pojo.LogEntryPojo.LogType;
import eu.dariah.de.minfba.schereg.pojo.converter.AuthWrappedPojoConverter;
import eu.dariah.de.minfba.schereg.processing.CollectingResourceConsumptionService;
import eu.dariah.de.minfba.schereg.service.ElementServiceImpl;
import eu.dariah.de.minfba.schereg.service.interfaces.ElementService;
import eu.dariah.de.minfba.schereg.service.interfaces.PersistedSessionService;
import eu.dariah.de.minfba.schereg.service.interfaces.SchemaService;

@Controller
@RequestMapping(value="/schema/editor/{schemaId}/")
public class MainEditorController extends BaseScheregController implements InitializingBean {
	private static Map<String, String> temporaryFilesMap = new HashMap<String, String>();
	
	@Autowired private SchemaService schemaService;
	@Autowired private ElementService elementService;
	@Autowired private SchemaImportWorker importWorker;
	@Autowired private AuthWrappedPojoConverter authPojoConverter;
	
	@Autowired private PersistedSessionService sessionService;
	
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
	
	@RequestMapping(method=GET, value="")
	public String getEditor(@PathVariable String schemaId, Model model, @ModelAttribute String sample, Locale locale, HttpServletRequest request) {
		AuthPojo auth = authInfoHelper.getAuth(request);
		model.addAttribute("schema", authPojoConverter.convert(schemaService.findByIdAndAuth(schemaId, auth), auth.getUserId()));
		try {
			PersistedSession s = sessionService.accessOrCreate(schemaId, request.getSession().getId(), auth.getUserId());
			model.addAttribute("session", s);
		} catch (GenericScheregException e) {
			logger.error("Failed to load/initialize persisted session", e);
		} 
		return "schemaEditor";
	}
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method=GET, value={"/forms/import"})
	public String getImportForm(@PathVariable String schemaId, Model model, Locale locale, HttpServletRequest request, HttpServletResponse response) {
		AuthPojo auth = authInfoHelper.getAuth(request);
		if(!schemaService.getHasWriteAccess(schemaId, auth.getUserId())) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return null;
		}
		model.addAttribute("actionPath", "/schema/editor/" + schemaId + "/async/import");
		model.addAttribute("schema", schemaService.findSchemaById(schemaId));
		return "schemaEditor/form/import";
	}
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method=GET, value={"/forms/fileupload"})
	public String getImportForm(Model model, Locale locale) {
		return "common/fileupload";
	}
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method = RequestMethod.GET, value = "/form/createRoot")
	public String getNewNonterminalForm(@PathVariable String schemaId, Model model, Locale locale, HttpServletRequest request, HttpServletResponse response) {
		AuthPojo auth = authInfoHelper.getAuth(request);
		if(!schemaService.getHasWriteAccess(schemaId, auth.getUserId())) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return null;
		}
		model.addAttribute("element", new Nonterminal());
		model.addAttribute("availableTerminals", schemaService.getAvailableTerminals(schemaId));
		model.addAttribute("actionPath", "/schema/editor/" + schemaId + "/async/saveNewRoot");
		return "schemaEditor/form/element/edit_nonterminal";
	}
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method = RequestMethod.POST, value = "/async/saveNewRoot")
	public @ResponseBody ModelActionPojo saveNonterminal(@PathVariable String schemaId, @Valid Nonterminal element, BindingResult bindingResult, Locale locale, HttpServletRequest request, HttpServletResponse response) {
		AuthPojo auth = authInfoHelper.getAuth(request);
		if(!schemaService.getHasWriteAccess(schemaId, auth.getUserId())) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return new ModelActionPojo(false);
		}
		ModelActionPojo result = this.getActionResult(bindingResult, locale);
		if (result.isSuccess()) {
			elementService.saveOrReplaceRoot(schemaId, element, authInfoHelper.getAuth(request));
		}		
		return result;
	}
	
	@PreAuthorize("isAuthenticated()")
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
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method=GET, value={"/async/file/delete/{fileId}"})
	public @ResponseBody ModelActionPojo deleteImportedFile(@PathVariable String fileId, Model model, Locale locale) {
		if (temporaryFilesMap.containsKey(fileId)) {
			temporaryFilesMap.remove(fileId);
		}
		return new ModelActionPojo(true);
	}
	
	@PreAuthorize("isAuthenticated()")
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
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method=POST, value={"/async/import"}, produces = "application/json; charset=utf-8")
	public @ResponseBody ModelActionPojo importSchemaElements(@RequestParam String schemaId, @RequestParam(value="file.id") String fileId, 
			@RequestParam(value="schema_root") Integer schemaRoot, Locale locale, HttpServletRequest request, HttpServletResponse response) {
		AuthPojo auth = authInfoHelper.getAuth(request);
		if(!schemaService.getHasWriteAccess(schemaId, auth.getUserId())) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return new ModelActionPojo(false);
		}
		ModelActionPojo result = new ModelActionPojo();
		try {
			if (temporaryFilesMap.containsKey(fileId)) {
				importWorker.importSchema(temporaryFilesMap.remove(fileId), schemaId, schemaRoot, authInfoHelper.getAuth(request));
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
	public @ResponseBody ModelActionPojo exportSchema(@PathVariable String schemaId, Model model, Locale locale) {
		Schema s = schemaService.findSchemaById(schemaId);
		Element r = elementService.findRootBySchemaId(schemaId, true);
		
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
	public @ResponseBody Element getHierarchy(@PathVariable String schemaId, Model model, Locale locale, HttpServletResponse response) throws IOException {
		Element result = elementService.findRootBySchemaId(schemaId, true);
		if (result==null) {
			response.getWriter().print("null");
			response.setContentType("application/json");
		}
		return result;
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/getRendered")
	public @ResponseBody MappableElement getRenderedHierarchy(@PathVariable String schemaId, Model model, Locale locale, HttpServletResponse response) throws IOException {
		Element e = elementService.findRootBySchemaId(schemaId, true);
		MappableElement result =  ElementServiceImpl.convertElement(e, true);
		if (result==null) {
			response.getWriter().print("null");
			response.setContentType("application/json");
		}
		return result;
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/getTerminals")
	public @ResponseBody List<? extends Terminal> getTerminals(@PathVariable String schemaId) {
		Schema s = schemaService.findSchemaById(schemaId);
		if (s instanceof XmlSchema) {	
			return ((XmlSchema)s).getTerminals();
		}
		return null;
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/async/applySample")
	public @ResponseBody ModelActionPojo applySample(@PathVariable String schemaId, @RequestParam String sample, HttpServletRequest request, Locale locale) {
		PersistedSession s = sessionService.access(schemaId, request.getSession().getId(), authInfoHelper.getUserId(request));
		s.setSampleInput(sample);
		s.addLogEntry(LogType.INFO, "~ Sample set for your current session");
		sessionService.saveSession(s);
		
		return new ModelActionPojo(true);
	}
		
	@RequestMapping(method = RequestMethod.GET, value = "/async/getSampleResource")
	public @ResponseBody Resource getSampleResource(@PathVariable String schemaId, @RequestParam(defaultValue="0") int index, HttpServletRequest request, Locale locale) {
		PersistedSession s = sessionService.access(schemaId, request.getSession().getId(), authInfoHelper.getUserId(request));
		
		if (s.getSampleOutput()!=null && s.getSampleOutput().size()>0) {
			
			if (s.getSampleOutput().size()>index) {
				Map<String, String> valueMap = new HashMap<String, String>();
				this.fillValueMap(valueMap, s.getSampleOutput().get(index));
				
				s.setSelectedValueMap(valueMap);
				s.setSelectedOutputIndex(index);
				
				sessionService.saveSession(s);
				
				return s.getSampleOutput().get(index);
			} 
		}
		return null;
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/executeSample")
	public @ResponseBody ModelActionPojo executeSample(@PathVariable String schemaId, HttpServletRequest request, Locale locale) {
		Stopwatch sw = new Stopwatch();
		ModelActionPojo result = new ModelActionPojo(true);
		result.setPojo(0);
		
		PersistedSession session = sessionService.access(schemaId, request.getSession().getId(), authInfoHelper.getUserId(request));
				
		XmlSchema s = (XmlSchema)schemaService.findSchemaById(schemaId);
		Nonterminal r = (Nonterminal)elementService.findRootBySchemaId(schemaId, true);
		
		XmlStringProcessingService processingSvc = appContext.getBean(XmlStringProcessingService.class);
		CollectingResourceConsumptionService consumptionService = new CollectingResourceConsumptionService();
		
		processingSvc.setXmlString(session.getSampleInput());
		processingSvc.setSchema(s);
		processingSvc.addConsumptionService(consumptionService);
		try {
			processingSvc.init(r);
			
			sw.start();
			processingSvc.run();
			
			session.setSampleOutput(consumptionService.getResources());
			session.setSelectedOutputIndex(0);
			
			if (session.getSampleOutput()!=null && session.getSampleOutput().size()>0) {
				result.setPojo(session.getSampleOutput().size());
				
				if (session.getSampleOutput().size()==1) {
					session.addLogEntry(LogType.SUCCESS, String.format("~ Sample input processed (total %sms): 1 resource found", sw.getElapsedTime(), consumptionService.getResources().size()));
				} else {
					session.addLogEntry(LogType.SUCCESS, String.format("~ Sample input processed (total %sms): %s resources found", sw.getElapsedTime(), consumptionService.getResources().size()));	
				}
			} else {
				session.addLogEntry(LogType.WARNING, "~ Sample input processed: No resources found");
			}
			
			sessionService.saveSession(session);
		} catch (Exception e) {
			logger.error("Error parsing XML string", e);
		}
		
		return result;
	}
	
	
	
	private void fillValueMap(Map<String, String> valueMap, Resource r) {
		if (!valueMap.containsKey(r.getElementId())) {
			valueMap.put(r.getElementId(), r.getValue()==null ? "" : r.getValue().toString());
		}
		if (r.getChildResources()!=null) {
			for (Resource rChild : r.getChildResources()) {
				this.fillValueMap(valueMap, rChild);
			}
		}
	}
			
	public static String humanReadableByteCount(long bytes, boolean si) {
	    int unit = si ? 1000 : 1024;
	    if (bytes < unit) return bytes + " B";
	    int exp = (int) (Math.log(bytes) / Math.log(unit));
	    String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
	    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}
}