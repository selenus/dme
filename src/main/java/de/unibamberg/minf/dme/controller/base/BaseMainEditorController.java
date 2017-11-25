package de.unibamberg.minf.dme.controller.base;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import de.unibamberg.minf.core.util.Stopwatch;
import de.unibamberg.minf.dme.exception.GenericScheregException;
import de.unibamberg.minf.dme.exception.SchemaImportException;
import de.unibamberg.minf.dme.importer.BaseImportWorker;
import de.unibamberg.minf.dme.importer.Importer;
import de.unibamberg.minf.dme.model.PersistedSession;
import de.unibamberg.minf.dme.model.SessionSampleFile;
import de.unibamberg.minf.dme.model.LogEntry.LogType;
import de.unibamberg.minf.dme.model.SessionSampleFile.FileTypes;
import de.unibamberg.minf.dme.model.base.Identifiable;
import de.unibamberg.minf.dme.model.base.Nonterminal;
import de.unibamberg.minf.dme.model.datamodel.base.Datamodel;
import de.unibamberg.minf.dme.model.mapping.base.MappedConcept;
import de.unibamberg.minf.dme.model.mapping.base.Mapping;
import de.unibamberg.minf.dme.processing.CollectingResourceConsumptionService;
import de.unibamberg.minf.dme.service.base.BaseEntityService;
import de.unibamberg.minf.dme.service.interfaces.ElementService;
import de.unibamberg.minf.dme.service.interfaces.MappedConceptService;
import de.unibamberg.minf.dme.service.interfaces.MappingService;
import de.unibamberg.minf.dme.service.interfaces.PersistedSessionService;
import de.unibamberg.minf.processing.exception.ProcessingConfigException;
import de.unibamberg.minf.processing.model.base.Resource;
import de.unibamberg.minf.processing.output.FileOutputService;
import de.unibamberg.minf.processing.output.json.JsonFileOutputService;
import de.unibamberg.minf.processing.output.xml.XmlFileOutputService;
import de.unibamberg.minf.processing.service.xml.XmlStringProcessingService;
import eu.dariah.de.dariahsp.model.web.AuthPojo;
import de.unibamberg.minf.core.web.pojo.MessagePojo;
import de.unibamberg.minf.core.web.pojo.ModelActionPojo;

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
	
	@RequestMapping(method=GET, value={"/state"}, produces = "application/json; charset=utf-8")
	public @ResponseBody ModelActionPojo getEntityState(@PathVariable String entityId, HttpServletRequest request) {
		if (entityId==null || entityId.isEmpty()) {
			return new ModelActionPojo(false);
		}
		boolean processing = this.getImportWorker().isBeingProcessed(entityId);
		
		ModelActionPojo result = new ModelActionPojo(true);
		ObjectNode jsonState = objectMapper.createObjectNode();
		jsonState.put("processing", processing);
		jsonState.put("ready", !processing);
		jsonState.put("error", false);
		result.setPojo(jsonState);
		return result;
	}

	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method=GET, value={"/forms/fileupload"})
	public String getImportForm(Model model, Locale locale) {
		return "common/fileupload";
	}
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method = RequestMethod.POST, value = {"/async/upload", "/async/upload/{elementId}"}, produces = "application/json; charset=utf-8")
	public @ResponseBody JsonNode uploadFile(@PathVariable String entityId, @PathVariable(required=false) String elementId, MultipartHttpServletRequest request, Model model, Locale locale, HttpServletResponse response) throws IOException {
		AuthPojo auth = authInfoHelper.getAuth(request);
		if(!this.getMainEntityService().getUserCanWriteEntity(entityId, auth.getUserId())) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return null;
		}
		ObjectNode result = objectMapper.createObjectNode();
		result.put("success", true);
		result.set("files", this.uploadFile(request, "validate/%s" + (elementId!=null ? (elementId + "/") : "")));
		return result;
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/async/uploadSample", produces = "application/json; charset=utf-8")
	public @ResponseBody JsonNode uploadSample(@PathVariable String entityId, MultipartHttpServletRequest request, Model model, Locale locale, HttpServletResponse response) throws IOException {
		ObjectNode result = objectMapper.createObjectNode();
		result.put("success", true);
		result.set("files", this.uploadFile(request, null));
		return result;
	}
	
	private JsonNode uploadFile(MultipartHttpServletRequest request, String validationUrl) throws IOException {
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
		if (validationUrl!=null) {
			fileNode.put("validateLink", String.format("/async/file/%s/", String.format(validationUrl, tmpId)));
		}
		filesNode.add(fileNode);
		return filesNode;
	}
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method=GET, value={"/async/file/delete/{fileId}"})
	public @ResponseBody ModelActionPojo deleteImportedFile(@PathVariable String entityId, @PathVariable String fileId, Model model, Locale locale, HttpServletRequest request, HttpServletResponse response) {
		if (!this.getMainEntityService().getUserCanWriteEntity(entityId, authInfoHelper.getAuth(request).getUserId())) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return null;
		}
		if (temporaryFilesMap.containsKey(fileId)) {
			temporaryFilesMap.remove(fileId);
		}
		return new ModelActionPojo(true);
	}
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method=GET, value={"/async/file/validate/{fileId}", "/async/file/validate/{fileId}/{elementId}"})
	public @ResponseBody ModelActionPojo validateImportedFile(@PathVariable String entityId, @PathVariable String fileId, @PathVariable(required=false) String elementId, Model model, Locale locale, HttpServletRequest request, HttpServletResponse response) throws SchemaImportException {
		if (!schemaService.getUserCanWriteEntity(entityId, authInfoHelper.getAuth(request).getUserId())) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return new ModelActionPojo(false);
		}		
		if (temporaryFilesMap.containsKey(fileId)) {
			return this.validateImportedFile(entityId, fileId, elementId, locale);
		}
		ModelActionPojo result = new ModelActionPojo(false);
		// TODO: Error message
		MessagePojo msg = new MessagePojo("danger", 
				messageSource.getMessage("~de.unibamberg.minf.common.view.forms.file.validationfailed.head", null, locale), 
				messageSource.getMessage("~de.unibamberg.minf.common.view.forms.file.validationfailed.body", null, locale));
		result.setMessage(msg);
		return result;
	}
	
	
	
	@RequestMapping(method=GET, value="/forms/uploadSample")
	public String getUploadSampleForm(@PathVariable String entityId, Model model, Locale locale, HttpServletRequest request, HttpServletResponse response) {
		
		model.addAttribute("actionPath", this.getPrefix() + entityId + "/async/executeUploadedSample");
		//model.addAttribute("schema", schemaService.findSchemaById(entityId));
		return "editor/form/upload_sample";
	}
		
	
	
	@RequestMapping(method=RequestMethod.POST, value={"/async/executeUploadedSample"})
	public @ResponseBody ModelActionPojo executeUploadedSample(@PathVariable String entityId, @RequestParam(value="file.id") String fileId, Model model, Locale locale, HttpServletRequest request, HttpServletResponse response) throws SchemaImportException, IOException {
		ModelActionPojo result = new ModelActionPojo();
		
		if (temporaryFilesMap.containsKey(fileId)) {
			String sample = new String(Files.readAllBytes(Paths.get(  new File(temporaryFilesMap.get(fileId)).toURI()  )), Charset.forName("UTF-8"));
			
			this.applySample(entityId, sample, request, response, locale);
			
			result.setSuccess(true);
			MessagePojo msg = new MessagePojo("success", 
					messageSource.getMessage("~de.unibamberg.minf.common.view.forms.file.validationsucceeded.head", null, locale), 
					messageSource.getMessage("~de.unibamberg.minf.common.view.forms.file.validationsucceeded.body", null, locale));
			result.setMessage(msg);
			
			return result;
		}
		result.setSuccess(false);
		// TODO: Error message
		MessagePojo msg = new MessagePojo("danger", 
				messageSource.getMessage("~de.unibamberg.minf.common.view.forms.file.validationfailed.head", null, locale), 
				messageSource.getMessage("~de.unibamberg.minf.common.view.forms.file.validationfailed.body", null, locale));
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
	public String getAssignChildForm(@PathVariable String entityId, Model model, HttpServletRequest request) throws GenericScheregException {
		Identifiable entity = this.getEntity(entityId);
		if (Datamodel.class.isAssignableFrom(entity.getClass())) {
			model.addAttribute("sourceModel", this.getLimitedString(((Datamodel)entity).getName(), 50));
		} else {
			Mapping m = (Mapping)entity;
			model.addAttribute("sourceModel", this.getLimitedString(schemaService.findSchemaById(m.getSourceId()).getName(), 50));
			model.addAttribute("targetModel", this.getLimitedString(schemaService.findSchemaById(m.getTargetId()).getName(), 50));
		}
		
		PersistedSession s = sessionService.access(entityId, request.getSession().getId(), authInfoHelper.getUserId(request));
		if (s==null) {
			throw new GenericScheregException("Session not available. Try to re-login.");
		}
		
		if (s.getSampleOutput()!=null && s.getSampleOutput().size()>0) {
			model.addAttribute("datasetCount", s.getSampleOutput().size());
			model.addAttribute("datasetCurrent", s.getSelectedOutputIndex());
		} else {
			model.addAttribute("datasetCount", 0);
		}
		return "editor/form/download_output";
	}
	
	@RequestMapping(method=RequestMethod.GET, value={"/async/download_link"})
	public @ResponseBody String getDownloadLink(@PathVariable String entityId, @RequestParam(defaultValue="single") String data, @RequestParam(defaultValue="source") String model, @RequestParam(defaultValue="xml") String format, Locale locale, HttpServletRequest request, HttpServletResponse response) throws SchemaImportException, IOException, ProcessingConfigException {
		ModelActionPojo result = new ModelActionPojo();
		
		PersistedSession s = sessionService.access(entityId, request.getSession().getId(), authInfoHelper.getUserId(request));
		if (s==null) {
			response.setStatus(HttpServletResponse.SC_RESET_CONTENT);
			return null;
		}		

		FileOutputService fos;
		if (format.equals("json")) {
			fos = appContext.getBean(JsonFileOutputService.class);
		} else {
			fos = appContext.getBean(XmlFileOutputService.class);
		}
		
		String schemaId = this.getModelId(entityId, model.equals("target"));
		fos.setSchema(schemaService.findSchemaById(schemaId));
		fos.setRoot(elementService.findRootBySchemaId(schemaId, true));
		
		String fileName = s.getId() + File.separator + s.getId();
		File outDir = new File(fos.getOutputPath(fileName, 0)).getParentFile();
		
		if (outDir.exists()) {
			FileUtils.deleteDirectory(outDir);
		}
		
		fos.writeOutput(this.getResource(s, data.equals("single"), model.equals("target")), fileName);
		
		ObjectNode pojo = objectMapper.createObjectNode();
		pojo.set("count", new IntNode(outDir.listFiles().length));
		
		SessionSampleFile sampleFile = new SessionSampleFile();
		sampleFile.setFileCount(outDir.listFiles().length);
		
		if (outDir.listFiles().length > 1) {
			logger.debug("Zip compressing " + outDir.listFiles().length + " output files");
			fos.compressOutput(fileName);

			sampleFile.setType(FileTypes.ZIP);
			sampleFile.setPath(fos.getOutputBaseDirectory() + File.separator + s.getId() + File.separator + s.getId() + ".zip");

			pojo.set("link", new TextNode(s.getId() + "/zip"));
			

		} else {

			sampleFile.setType(fos instanceof XmlFileOutputService ? FileTypes.XML : FileTypes.JSON);
			sampleFile.setPath(fos.getOutputBaseDirectory() + File.separator + s.getId() + File.separator + s.getId() + "." + fos.getFileExtension());

			
		}
		
		s.setSampleFile(sampleFile);
		sessionService.saveSession(s);

		result.setPojo(pojo);
		
		return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(pojo);
	}
	
	@RequestMapping(method=RequestMethod.GET, value={"/async/download_output/"})
	public ResponseEntity<byte[]> getFile(@PathVariable String entityId, HttpServletRequest request, HttpServletResponse response) throws FileNotFoundException, IOException {
		PersistedSession s = sessionService.access(entityId, request.getSession().getId(), authInfoHelper.getUserId(request));
		if (s==null) {
			response.setStatus(HttpServletResponse.SC_RESET_CONTENT);
			return null;
		}
		
		HttpHeaders headers = new HttpHeaders();

		File downloadFile = new File(s.getSampleFile().getPath());
		byte[] contents = IOUtils.toByteArray(new FileInputStream(downloadFile));
		
		if (s.getSampleFile().getType().equals(FileTypes.XML)) {
			headers.setContentType(MediaType.APPLICATION_XML);
		} else if (s.getSampleFile().getType().equals(FileTypes.ZIP)) {
			headers.setContentType(MediaType.MULTIPART_FORM_DATA);
		} else {
			headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
		}

	    headers.setContentDispositionFormData(downloadFile.getName(), downloadFile.getName());
	    headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

	    return new ResponseEntity<byte[]>(contents, headers, HttpStatus.OK);
	}
	
	private Resource[] getResource(PersistedSession s, boolean single, boolean target) {
		if (target) {
			if (single) {
				return new Resource[] {s.getSampleMapped().get(s.getSelectedOutputIndex())};
			} else {
				return s.getSampleMapped().toArray(new Resource[0]);
			}
		} else {
			if (single) {
				return new Resource[] {s.getSampleOutput().get(s.getSelectedOutputIndex())};
			} else {
				return s.getSampleOutput().toArray(new Resource[0]);
			}
		}
	}
	
	private String getModelId(String entityId, boolean target) {
		Identifiable entity = this.getEntity(entityId);
		if (Datamodel.class.isAssignableFrom(entity.getClass())) {
			return entity.getId();
		} else {
			Mapping m = (Mapping)entity;
			if (target) {
				return m.getTargetId();
			} else {
				return m.getSourceId();
			}
		}
	}
	
	@RequestMapping(method=RequestMethod.GET, value={"/async/download_sample_input"})
	public @ResponseBody String downloadSampleInput(@PathVariable String entityId, Model model, Locale locale, HttpServletRequest request, HttpServletResponse response) throws SchemaImportException, IOException {
		ModelActionPojo result = new ModelActionPojo();
		
		PersistedSession s = sessionService.access(entityId, request.getSession().getId(), authInfoHelper.getUserId(request));
		if (s==null) {
			response.setStatus(HttpServletResponse.SC_RESET_CONTENT);
			return null;
		}
		
		ObjectNode pojo = objectMapper.createObjectNode();
		
		pojo.set("content", new TextNode(s.getSampleInput()));
		pojo.set("mime", new TextNode("application/xml; charset=utf-8"));
		pojo.set("extension", new TextNode("xml"));
	
		
		result.setPojo(pojo);
		
		return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(pojo);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/async/applySample")
	public @ResponseBody ModelActionPojo applySample(@PathVariable String entityId, @RequestParam String sample, HttpServletRequest request, HttpServletResponse response, Locale locale) {
		PersistedSession s = sessionService.access(entityId, request.getSession().getId(), authInfoHelper.getUserId(request));
		if (s==null) {
			response.setStatus(HttpServletResponse.SC_RESET_CONTENT);
			return new ModelActionPojo(false);
		}
		s.setSampleInput(sample);
		s.addLogEntry(LogType.INFO, messageSource.getMessage("~de.unibamberg.minf.dme.editor.sample.log.session_sample_set", null, locale));
		
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
	public @ResponseBody ModelActionPojo getTransformedResource(@PathVariable String entityId, @RequestParam(defaultValue="0") int index, @RequestParam(defaultValue="false") boolean force, HttpServletRequest request, HttpServletResponse response, Locale locale) throws IOException {
		PersistedSession s = sessionService.access(entityId, request.getSession().getId(), authInfoHelper.getUserId(request));
		if (s==null) {
			response.setStatus(HttpServletResponse.SC_RESET_CONTENT);
			return null;
		}
		
		ModelActionPojo result = new ModelActionPojo();
		ObjectNode statusPojo = objectMapper.createObjectNode();
		result.setStatusInfo(statusPojo);
		
		if (s.getSampleMapped()!=null && s.getSampleMapped().size()>0) {
			
			if (s.getSampleMapped().size()>index) {
				/*Map<String, String> valueMap = new HashMap<String, String>();
				this.fillValueMap(valueMap, s.getSampleOutput().get(index));
				
				s.setSelectedValueMap(valueMap);*/
				s.setSelectedOutputIndex(index);
				
				sessionService.saveSession(s);
				
				result.setSuccess(true);
				statusPojo.set("available", BooleanNode.TRUE);
				
				if (!force && objectMapper.writeValueAsString(s.getSampleMapped().get(index)).getBytes().length > this.maxTravelSize) {
					statusPojo.set("oversize", BooleanNode.TRUE);
				} else {
					statusPojo.set("oversize", BooleanNode.FALSE);
					result.setPojo(s.getSampleMapped().get(index));
				}
				return result;
			} 
		}
		statusPojo.set("available", BooleanNode.FALSE);
		return result;
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/executeSample")
	public @ResponseBody ModelActionPojo executeSample(@PathVariable String entityId, HttpServletRequest request, HttpServletResponse response, Locale locale) {
		Stopwatch sw = new Stopwatch().start();
		Stopwatch swTotal = new Stopwatch().start();
		ModelActionPojo result = new ModelActionPojo(true);
		result.setPojo(0);
		
		logger.debug("Start executing sample against datamodel [{}]", entityId);
		
		PersistedSession session = sessionService.access(entityId, request.getSession().getId(), authInfoHelper.getUserId(request));
		if (session==null) {
			response.setStatus(HttpServletResponse.SC_RESET_CONTENT);
			return null;
		}
		
		logger.debug("Session for transformation loaded [{}] took {}ms", entityId, sw.getElapsedTime());
		sw.reset();
		
		Datamodel s = schemaService.findSchemaById(entityId);
		if (s==null) {
			Mapping m = mappingService.findMappingById(entityId);
			s = schemaService.findSchemaById(m.getSourceId());
		}		
		
		logger.debug("Datamodel loaded [{}] took {}ms", entityId, sw.getElapsedTime());
		sw.reset();
		
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
			
			logger.debug("Preparation of sample for datamodel [{}] took {}ms", entityId, sw.getElapsedTime());
			
			sw.reset();
			processingSvc.run();
			
			logger.debug("Parse of sample against datamodel [{}] took {}ms", entityId, sw.getElapsedTime());
			
			sw.reset();
			
			session.setSampleOutput(consumptionService.getResources());
			session.setSelectedOutputIndex(0);
			
			if (session.getSampleOutput()!=null && session.getSampleOutput().size()>0) {
				result.setPojo(session.getSampleOutput().size());
								
				if (session.getSampleOutput().size()==1) {				
					session.addLogEntry(LogType.SUCCESS, messageSource.getMessage("~de.unibamberg.minf.dme.editor.sample.log.processed_1_result", new Object[]{swTotal.getElapsedTime()}, locale));
				} else {
					session.addLogEntry(LogType.SUCCESS, messageSource.getMessage("~de.unibamberg.minf.dme.editor.sample.log.processed_n_results", new Object[]{swTotal.getElapsedTime(), consumptionService.getResources().size()}, locale));	
				}
			} else {
				session.addLogEntry(LogType.WARNING, messageSource.getMessage("~de.unibamberg.minf.dme.editor.sample.log.processed_no_results", null, locale));
			}
			
			sessionService.saveSession(session);
			
			logger.debug("Post-parse session handling for datamodel [{}] took {}ms", entityId, sw.getElapsedTime());
			
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
	
	protected abstract BaseEntityService getMainEntityService();
	protected abstract String getPrefix();
	protected abstract BaseImportWorker<? extends Importer> getImportWorker();
	protected abstract ModelActionPojo validateImportedFile(String entityId, String fileId, String elementId, Locale locale);
		
			
	public static String humanReadableByteCount(long bytes, boolean si) {
	    int unit = si ? 1000 : 1024;
	    if (bytes < unit) return bytes + " B";
	    int exp = (int) (Math.log(bytes) / Math.log(unit));
	    String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
	    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}
}
