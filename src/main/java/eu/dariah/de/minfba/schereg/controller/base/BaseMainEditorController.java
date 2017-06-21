package eu.dariah.de.minfba.schereg.controller.base;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
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
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import eu.dariah.de.dariahsp.model.web.AuthPojo;
import eu.dariah.de.minfba.core.metamodel.interfaces.Element;
import eu.dariah.de.minfba.core.metamodel.interfaces.MappedConcept;
import eu.dariah.de.minfba.core.metamodel.interfaces.Mapping;
import eu.dariah.de.minfba.core.metamodel.interfaces.Nonterminal;
import eu.dariah.de.minfba.core.metamodel.interfaces.Schema;
import eu.dariah.de.minfba.core.metamodel.serialization.SerializableSchemaContainer;
import eu.dariah.de.minfba.core.metamodel.xml.XmlSchemaNature;
import eu.dariah.de.minfba.core.util.Stopwatch;
import eu.dariah.de.minfba.core.web.pojo.MessagePojo;
import eu.dariah.de.minfba.core.web.pojo.ModelActionPojo;
import eu.dariah.de.minfba.processing.model.base.Resource;
import eu.dariah.de.minfba.processing.service.text.TextStringProcessingService;
import eu.dariah.de.minfba.processing.service.xml.XmlStringProcessingService;
import eu.dariah.de.minfba.schereg.exception.SchemaImportException;
import eu.dariah.de.minfba.schereg.model.PersistedSession;
import eu.dariah.de.minfba.schereg.pojo.LogEntryPojo.LogType;
import eu.dariah.de.minfba.schereg.processing.CollectingResourceConsumptionService;
import eu.dariah.de.minfba.schereg.service.interfaces.ElementService;
import eu.dariah.de.minfba.schereg.service.interfaces.MappedConceptService;
import eu.dariah.de.minfba.schereg.service.interfaces.MappingService;
import eu.dariah.de.minfba.schereg.service.interfaces.PersistedSessionService;

public abstract class BaseMainEditorController extends BaseScheregController {
	protected static Map<String, String> temporaryFilesMap = new HashMap<String, String>();
	
	@Autowired protected MappingService mappingService;
	@Autowired protected MappedConceptService mappedConceptService;
	@Autowired protected PersistedSessionService sessionService;
	@Autowired protected ElementService elementService;
	
	@Value(value="${paths.tmpUploadDir:/tmp}")
	protected String tmpUploadDirPath;
	
	@Value(value="${editors.samples.maxTravelSize:10000}")
	protected int maxTravelSize;
	
	public BaseMainEditorController(String mainNavId) {
		super(mainNavId);
	}
	
	protected abstract String getPrefix();
	
	@RequestMapping(method=GET, value="/forms/uploadSample")
	public String getUploadSampleForm(@PathVariable String entityId, Model model, Locale locale, HttpServletRequest request, HttpServletResponse response) {
		
		model.addAttribute("actionPath", this.getPrefix() + entityId + "/async/executeUploadedSample");
		//model.addAttribute("schema", schemaService.findSchemaById(entityId));
		return "editor/form/upload_sample";
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/async/uploadSample", produces = "application/json; charset=utf-8")
	public @ResponseBody JsonNode uploadSample(@PathVariable String entityId, MultipartHttpServletRequest request, Model model, Locale locale, HttpServletResponse response) throws IOException {
				
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
		//fileNode.put("validateLink", "/async/file/validateSample/" + tmpId);

		filesNode.add(fileNode);
		
		ObjectNode result = objectMapper.createObjectNode();
		result.put("success", true);
		result.set("files", filesNode);
		
		return result;
	}
	
	@RequestMapping(method=RequestMethod.POST, value={"/async/executeUploadedSample"})
	public @ResponseBody ModelActionPojo executeUploadedSample(@PathVariable String entityId, @RequestParam(value="file.id") String fileId, Model model, Locale locale, HttpServletRequest request, HttpServletResponse response) throws SchemaImportException, IOException {
		ModelActionPojo result = new ModelActionPojo();
		
		if (temporaryFilesMap.containsKey(fileId)) {
			String sample = new String(Files.readAllBytes(Paths.get(  new File(temporaryFilesMap.get(fileId)).toURI()  )), Charset.forName("UTF-8"));
			
			this.applySample(entityId, sample, request, response, locale);
			
			result.setSuccess(true);
			MessagePojo msg = new MessagePojo("success", 
					messageSource.getMessage("~eu.dariah.de.minfba.common.view.forms.file.validationsucceeded.head", null, locale), 
					messageSource.getMessage("~eu.dariah.de.minfba.common.view.forms.file.validationsucceeded.body", null, locale));
			result.setMessage(msg);
			
			return result;
		}
		result.setSuccess(false);
		// TODO: Error message
		MessagePojo msg = new MessagePojo("danger", 
				messageSource.getMessage("~eu.dariah.de.minfba.common.view.forms.file.validationfailed.head", null, locale), 
				messageSource.getMessage("~eu.dariah.de.minfba.common.view.forms.file.validationfailed.body", null, locale));
		result.setMessage(msg);
		return result;
	}
	
	@RequestMapping(method=RequestMethod.GET, value={"/async/load_sample"})
	public @ResponseBody String loadSample(@PathVariable String entityId, @RequestParam(name="t", defaultValue="input") String type, Model model, Locale locale, HttpServletRequest request, HttpServletResponse response) throws SchemaImportException, IOException {
		
		ModelActionPojo result = new ModelActionPojo();
		
		PersistedSession s = sessionService.access(entityId, request.getSession().getId(), authInfoHelper.getUserId(request));
		if (s==null) {
			response.setStatus(HttpServletResponse.SC_RESET_CONTENT);
			return null;
		}
		
		if (type.equals("output")) {
			result.setPojo(objectMapper.convertValue(s.getSampleOutput(), JsonNode.class));
		} else if (type.equals("transformed")) { 
			
		} else {
			result.setPojo(new TextNode(s.getSampleInput()));
		}
		
		return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/forms/download_output")
	public String getAssignChildForm(@PathVariable String schemaId, @PathVariable String elementId, @RequestParam(name="t", defaultValue="input") String type, @RequestParam(name="i", defaultValue="-1") int index, Model model, Locale locale, HttpServletRequest request, HttpServletResponse response) {	
		//model.addAttribute("actionPath", "/schema/editor/" + schemaId + "/element/" + elementId + "/assignChild");
		return "editor/form/download_output";
	}
	
	@RequestMapping(method=RequestMethod.GET, value={"/async/download_sample"})
	public @ResponseBody String downloadSample(@PathVariable String entityId, @RequestParam(name="t", defaultValue="input") String type, @RequestParam(name="i", defaultValue="-1") int index, Model model, Locale locale, HttpServletRequest request, HttpServletResponse response) throws SchemaImportException, IOException {
		ModelActionPojo result = new ModelActionPojo();
		
		PersistedSession s = sessionService.access(entityId, request.getSession().getId(), authInfoHelper.getUserId(request));
		if (s==null) {
			response.setStatus(HttpServletResponse.SC_RESET_CONTENT);
			return null;
		}
		
		ObjectNode pojo = objectMapper.createObjectNode();
		
		if (type.equals("output") || type.equals("transformed")) {
			if (type.equals("output") && s.getSampleOutput()!=null && s.getSampleOutput().size()>0) {
				if (index>=0) {
					pojo.set("content", objectMapper.convertValue(s.getSampleOutput().get(index), JsonNode.class));
				} else {
					pojo.set("content", objectMapper.convertValue(s.getSampleOutput(), JsonNode.class));
				}
			} else if (type.equals("transformed") && s.getSampleMapped()!=null && s.getSampleMapped().size()>0) {
				
			}
			pojo.set("mime", new TextNode("application/json; charset=utf-8"));
			pojo.set("extension", new TextNode("json"));
		} else if (type.equals("transformed")) { 
			
		} else {
			pojo.set("content", new TextNode(s.getSampleInput()));
			pojo.set("mime", new TextNode("application/xml; charset=utf-8"));
			pojo.set("extension", new TextNode("xml"));
		}
		
		result.setPojo(pojo);
		
		return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(pojo);
	}

	@RequestMapping(method=GET, value={"/forms/fileupload"})
	public String getImportForm(Model model, Locale locale) {
		return "common/fileupload";
	}

	@RequestMapping(method = RequestMethod.POST, value = "/async/applySample")
	public @ResponseBody ModelActionPojo applySample(@PathVariable String entityId, @RequestParam String sample, HttpServletRequest request, HttpServletResponse response, Locale locale) {
		PersistedSession s = sessionService.access(entityId, request.getSession().getId(), authInfoHelper.getUserId(request));
		if (s==null) {
			response.setStatus(HttpServletResponse.SC_RESET_CONTENT);
			return new ModelActionPojo(false);
		}
		s.setSampleInput(sample);
		s.addLogEntry(LogType.INFO, messageSource.getMessage("~eu.dariah.de.minfba.schereg.editor.sample.log.session_sample_set", null, locale));
		
		sessionService.saveSession(s);
		
		return new ModelActionPojo(true);
	}
		
	@RequestMapping(method = RequestMethod.GET, value = "/async/getSampleResource")
	public @ResponseBody ModelActionPojo getSampleResource(@PathVariable String entityId, @RequestParam(defaultValue="0") int index, @RequestParam(defaultValue="false") boolean force, HttpServletRequest request, HttpServletResponse response, Locale locale) throws IOException {
		PersistedSession s = sessionService.access(entityId, request.getSession().getId(), authInfoHelper.getUserId(request));
		if (s==null) {
			response.setStatus(HttpServletResponse.SC_RESET_CONTENT);
			return null;
		}
		
		ModelActionPojo result = new ModelActionPojo();
		ObjectNode statusPojo = objectMapper.createObjectNode();
		result.setStatusInfo(statusPojo);
		
		if (s.getSampleOutput()!=null && s.getSampleOutput().size()>0) {
			if (s.getSampleOutput().size()>index) {
				Map<String, String> valueMap = new HashMap<String, String>();
				this.fillValueMap(valueMap, s.getSampleOutput().get(index));
				
				// Also add values for our mapped concepts 
				if (mappingService.findMappingById(entityId) != null) {
					List<MappedConcept> concepts = mappedConceptService.findAllByMappingId(entityId);
					if (concepts != null) {
						// TODO: Adapt
						/*for (MappedConcept c : concepts) {
							if (valueMap.containsKey(c.getSourceElementId())) {
								valueMap.put(c.getId(), valueMap.get(c.getSourceElementId()));
							}
						}*/
					}
				}
				
				s.setSelectedValueMap(valueMap);
				s.setSelectedOutputIndex(index);
				
				sessionService.saveSession(s);
				
				result.setSuccess(true);
				statusPojo.set("available", BooleanNode.TRUE);
				
				if (!force && objectMapper.writeValueAsString(s.getSampleOutput().get(index)).getBytes().length > this.maxTravelSize) {
					statusPojo.set("oversize", BooleanNode.TRUE);
				} else {
					statusPojo.set("oversize", BooleanNode.FALSE);
					result.setPojo(s.getSampleOutput().get(index));
				}
				return result;
			} 
		}
		
		statusPojo.set("available", BooleanNode.FALSE);
		
		
		return result;
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/getTransformedResource")
	public @ResponseBody Resource getTransformedResource(@PathVariable String entityId, @RequestParam(defaultValue="0") int index, HttpServletRequest request, HttpServletResponse response, Locale locale) throws IOException {
		PersistedSession s = sessionService.access(entityId, request.getSession().getId(), authInfoHelper.getUserId(request));
		if (s==null) {
			response.setStatus(HttpServletResponse.SC_RESET_CONTENT);
			return null;
		}
		if (s.getSampleMapped()!=null && s.getSampleMapped().size()>0) {
			
			if (s.getSampleMapped().size()>index) {
				/*Map<String, String> valueMap = new HashMap<String, String>();
				this.fillValueMap(valueMap, s.getSampleOutput().get(index));
				
				s.setSelectedValueMap(valueMap);*/
				s.setSelectedOutputIndex(index);
				
				sessionService.saveSession(s);
				
				return s.getSampleMapped().get(index);
			} 
		}
		response.getWriter().print("null");
		return null;
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/executeSample")
	public @ResponseBody ModelActionPojo executeSample(@PathVariable String entityId, HttpServletRequest request, HttpServletResponse response, Locale locale) {
		Stopwatch sw = new Stopwatch();
		ModelActionPojo result = new ModelActionPojo(true);
		result.setPojo(0);
		
		PersistedSession session = sessionService.access(entityId, request.getSession().getId(), authInfoHelper.getUserId(request));
		if (session==null) {
			response.setStatus(HttpServletResponse.SC_RESET_CONTENT);
			return null;
		}
		
		Schema s = schemaService.findSchemaById(entityId);
		if (s==null) {
			Mapping m = mappingService.findMappingById(entityId);
			s = schemaService.findSchemaById(m.getSourceId());
		}		
		
		Nonterminal r = (Nonterminal)elementService.findRootBySchemaId(s.getId(), true);
		
		XmlStringProcessingService processingSvc = appContext.getBean(XmlStringProcessingService.class);
		
		//TextStringProcessingService processingSvc = appContext.getBean(TextStringProcessingService.class);
		
		CollectingResourceConsumptionService consumptionService = new CollectingResourceConsumptionService();
		
		processingSvc.setXmlString(session.getSampleInput());
		processingSvc.setSchema(s);
		processingSvc.addConsumptionService(consumptionService);
		try {
			processingSvc.setRoot(r);
			processingSvc.init();
			
			sw.start();
			processingSvc.run();
			
			session.setSampleOutput(consumptionService.getResources());
			session.setSelectedOutputIndex(0);
			
			if (session.getSampleOutput()!=null && session.getSampleOutput().size()>0) {
				result.setPojo(session.getSampleOutput().size());
								
				if (session.getSampleOutput().size()==1) {				
					session.addLogEntry(LogType.SUCCESS, messageSource.getMessage("~eu.dariah.de.minfba.schereg.editor.sample.log.processed_1_result", new Object[]{sw.getElapsedTime()}, locale));
				} else {
					session.addLogEntry(LogType.SUCCESS, messageSource.getMessage("~eu.dariah.de.minfba.schereg.editor.sample.log.processed_n_results", new Object[]{sw.getElapsedTime(), consumptionService.getResources().size()}, locale));	
				}
			} else {
				session.addLogEntry(LogType.WARNING, messageSource.getMessage("~eu.dariah.de.minfba.schereg.editor.sample.log.processed_no_results", null, locale));
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
