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
import org.springframework.context.ApplicationContextAware;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import eu.dariah.de.minfba.core.metamodel.Nonterminal;
import eu.dariah.de.minfba.core.metamodel.interfaces.Element;
import eu.dariah.de.minfba.core.metamodel.interfaces.Schema;
import eu.dariah.de.minfba.core.metamodel.interfaces.Terminal;
import eu.dariah.de.minfba.core.metamodel.serialization.SerializableSchemaContainer;
import eu.dariah.de.minfba.core.metamodel.xml.XmlSchema;
import eu.dariah.de.minfba.core.web.controller.BaseTranslationController;
import eu.dariah.de.minfba.core.web.pojo.ModelActionPojo;
import eu.dariah.de.minfba.processing.exception.ProcessingConfigException;
import eu.dariah.de.minfba.processing.model.base.Resource;
import eu.dariah.de.minfba.processing.service.xml.XmlStringProcessingService;
import eu.dariah.de.minfba.core.web.pojo.MessagePojo;
import eu.dariah.de.minfba.schereg.exception.SchemaImportException;
import eu.dariah.de.minfba.schereg.importer.SchemaImportWorker;
import eu.dariah.de.minfba.schereg.pojo.LogEntryPojo;
import eu.dariah.de.minfba.schereg.pojo.LogEntryPojo.LogType;
import eu.dariah.de.minfba.schereg.processing.CollectingResourceConsumptionService;
import eu.dariah.de.minfba.schereg.service.interfaces.ElementService;
import eu.dariah.de.minfba.schereg.service.interfaces.SchemaService;

@Controller
@RequestMapping(value="/schema/editor/{schemaId}")
@SessionAttributes({"sample", "sampleResources", "log", "valueMap"})
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
		
		if (!model.asMap().containsKey("sample")) {
			model.addAttribute("sample", "<?xml version=\"1.0\" encoding=\"UTF-8\"?><?xml-stylesheet type=\"text/xsl\" href=\"oai2.xsl\"?><OAI-PMH xmlns=\"http://www.openarchives.org/OAI/2.0/\" xsi:schemaLocation=\"http://www.openarchives.org/OAI/2.0/ http://www.openarchives.org/OAI/2.0/OAI-PMH.xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><responseDate>2015-08-26T15:28:08Z</responseDate><request verb=\"GetRecord\" metadataPrefix=\"oai_dc\" identifier=\"oai:pangaea.de:doi:10.1594/PANGAEA.678311\">http://ws.pangaea.de/oai/provider</request><GetRecord><record><header><identifier>oai:pangaea.de:doi:10.1594/PANGAEA.678311</identifier><datestamp>2015-05-06T15:34:51Z</datestamp></header><metadata><oai_dc:dc xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:oai_dc=\"http://www.openarchives.org/OAI/2.0/oai_dc/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.openarchives.org/OAI/2.0/oai_dc/ http://www.openarchives.org/OAI/2.0/oai_dc.xsd\"><dc:title>Physical properties measured on 21 sediment cores from METEOR cruise M23/1</dc:title><dc:creator>von Dobeneck, Tilo</dc:creator><dc:creator>Grigel, Jens</dc:creator><dc:creator>Richter, Monika</dc:creator><dc:creator>Schmidt, Andrea</dc:creator><dc:creator>Spieß, Volkhard</dc:creator><dc:publisher>PANGAEA</dc:publisher><dc:date>1994-01-18</dc:date><dc:type>Dataset</dc:type><dc:format>application/zip, 21 datasets</dc:format><dc:identifier>http://doi.pangaea.de/10.1594/PANGAEA.678311</dc:identifier><dc:identifier>doi:10.1594/PANGAEA.678311</dc:identifier><dc:language>en</dc:language><dc:rights>CC-BY: Creative Commons Attribution 3.0 Unported</dc:rights><dc:rights>Access constraints: unrestricted</dc:rights><dc:relation>Spieß, Volkhard; Abelmann, Andrea; Bickert, Torsten; Brehme, Isa; Cordes, Rainer; Dehning, Klaus; von Dobeneck, Tilo; Donner, Barbara; Ehrhardt, Isabel; Giese, Martina; Grigel, Jens; Haese, Ralf R; Hale, Walter; Hinrichs, Sigrid; Kasten, Sabine; Cavalcanti de C Laier, Ana P; Teixeira de Oliveira, Maria E; Petermann, Harald; Rapp, Robert; Richter, Martina; Rogers, John; Schmidt, Andrea; Scholz, Maike; Skowronek, Frank; Zabel, Matthias (1994): Bericht und erste Ergebnisse der Meteor-Fahrt M23/1 Kapstadt-Rio de Janeiro, 4.2.-25.2.1993. Berichte aus dem Fachbereich Geowissenschaften der Universität Bremen, 42, 139 pp, urn:nbn:de:gbv:46-ep000101819</dc:relation><dc:coverage>MEDIAN LATITUDE: -34.336190 * MEDIAN LONGITUDE: -4.195000 * SOUTH-BOUND LATITUDE: -36.833333 * WEST-BOUND LONGITUDE: -20.923333 * NORTH-BOUND LATITUDE: -30.870000 * EAST-BOUND LONGITUDE: 14.343333 * DATE/TIME START: 1993-02-05T00:00:00 * DATE/TIME END: 1993-02-19T00:00:00</dc:coverage><dc:subject>Angola Basin; automated full waveform logging system; Bartington MS2C coil sensor; Cape Basin; CTD, SEA-BIRD SBE 19 SEACAT; Density, wet bulk; Depth; DEPTH, sediment/rock; GeoB; GeoB2004-2; GeoB2011-2; GeoB2016-1; GeoB2018-3; GeoB2019-1; GeoB2021-5; GeoB2022-2; Geosciences, University of Bremen; Gravity corer (Kiel type); kappa; KOL; M23/1; Meteor (1986); Multi-Sensor Core Logger; Multi-Sensor Core Logger 14, GEOTEK; Piston corer (Kiel type); SL; South African margin; Southwest Walvis Ridge; Susceptibility, volume; Velocity, compressional wave; Vp; WBD</dc:subject></oai_dc:dc></metadata></record></GetRecord></OAI-PMH>");
		}
		
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
	
	@RequestMapping(method = RequestMethod.POST, value = "/async/applySample")
	public @ResponseBody ModelActionPojo applySample(@PathVariable String schemaId, @RequestParam String sample, Model model, Locale locale) {
		// TODO: Persist this in db for the session or user and store only an id in the session
		model.addAttribute("sample", sample);
		this.addLogEntry(model, schemaId, LogType.INFO, "~ Sample set for your current session");
		
		return new ModelActionPojo(true);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/executeSample")
	public @ResponseBody ModelActionPojo executeSample(@PathVariable String schemaId, Model model, Locale locale) {
		String sample = (String)model.asMap().get("sample");
		
		XmlSchema s = (XmlSchema)schemaService.findSchemaById(schemaId);
		Nonterminal r = (Nonterminal)elementService.findRootBySchemaId(schemaId, true);
		
		XmlStringProcessingService processingSvc = appContext.getBean(XmlStringProcessingService.class);
		CollectingResourceConsumptionService consumptionService = new CollectingResourceConsumptionService();
		
		processingSvc.setXmlString(sample);
		processingSvc.setSchema(s);
		processingSvc.addConsumptionService(consumptionService);
		try {
			processingSvc.init(r);
			processingSvc.run();
			
			if (consumptionService.getResources()!=null && consumptionService.getResources().size()>0) {
				model.addAttribute("sampleResources", consumptionService.getResources());
				
				Map<String, String> valueMap = new HashMap<String, String>();
				this.fillValueMap(valueMap, consumptionService.getResources().get(0));
				
				model.addAttribute("valueMap", valueMap);
				
				for (Resource res : consumptionService.getResources()) {
					this.addLogEntry(model, schemaId, LogType.SUCCESS, 
							"~ Resource generated: \n" + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(res));
				}
				this.addLogEntry(model, schemaId, LogType.INFO, String.format("~ Sample input processed: %s resource(s) found; the first is used as session sample", consumptionService.getResources().size()));
			} else {
				this.addLogEntry(model, schemaId, LogType.INFO, "~ Sample input processed: No resources found");
			}
		} catch (Exception e) {
			logger.error("Error parsing XML string", e);
		}
		return new ModelActionPojo(true);
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