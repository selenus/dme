package eu.dariah.de.minfba.schereg.controller.base;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.dariah.de.minfba.core.metamodel.Nonterminal;
import eu.dariah.de.minfba.core.metamodel.interfaces.Mapping;
import eu.dariah.de.minfba.core.metamodel.xml.XmlSchema;
import eu.dariah.de.minfba.core.util.Stopwatch;
import eu.dariah.de.minfba.core.web.pojo.ModelActionPojo;
import eu.dariah.de.minfba.processing.model.base.Resource;
import eu.dariah.de.minfba.processing.service.xml.XmlStringProcessingService;
import eu.dariah.de.minfba.schereg.model.PersistedSession;
import eu.dariah.de.minfba.schereg.pojo.LogEntryPojo.LogType;
import eu.dariah.de.minfba.schereg.processing.CollectingResourceConsumptionService;
import eu.dariah.de.minfba.schereg.service.interfaces.ElementService;
import eu.dariah.de.minfba.schereg.service.interfaces.MappingService;
import eu.dariah.de.minfba.schereg.service.interfaces.PersistedSessionService;

public abstract class BaseMainEditorController extends BaseScheregController {
	@Autowired protected MappingService mappingService;
	@Autowired protected PersistedSessionService sessionService;
	@Autowired protected ElementService elementService;
	
	public BaseMainEditorController(String mainNavId) {
		super(mainNavId);
	}

	@RequestMapping(method = RequestMethod.POST, value = "/async/applySample")
	public @ResponseBody ModelActionPojo applySample(@PathVariable String entityId, @RequestParam String sample, HttpServletRequest request, Locale locale) {
		PersistedSession s = sessionService.access(entityId, request.getSession().getId(), authInfoHelper.getUserId(request));
		s.setSampleInput(sample);
		s.addLogEntry(LogType.INFO, messageSource.getMessage("~eu.dariah.de.minfba.schereg.editor.sample.log.session_sample_set", null, locale));
		
		sessionService.saveSession(s);
		
		return new ModelActionPojo(true);
	}
		
	@RequestMapping(method = RequestMethod.GET, value = "/async/getSampleResource")
	public @ResponseBody Resource getSampleResource(@PathVariable String entityId, @RequestParam(defaultValue="0") int index, HttpServletRequest request, Locale locale) {
		PersistedSession s = sessionService.access(entityId, request.getSession().getId(), authInfoHelper.getUserId(request));
		
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
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/getTransformedResource")
	public @ResponseBody Resource getTransformedResource(@PathVariable String entityId, @RequestParam(defaultValue="0") int index, HttpServletRequest request, Locale locale) {
		PersistedSession s = sessionService.access(entityId, request.getSession().getId(), authInfoHelper.getUserId(request));
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
		return null;
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/executeSample")
	public @ResponseBody ModelActionPojo executeSample(@PathVariable String entityId, HttpServletRequest request, Locale locale) {
		Stopwatch sw = new Stopwatch();
		ModelActionPojo result = new ModelActionPojo(true);
		result.setPojo(0);
		
		PersistedSession session = sessionService.access(entityId, request.getSession().getId(), authInfoHelper.getUserId(request));
		
		XmlSchema s = (XmlSchema)schemaService.findSchemaById(entityId);
		if (s==null) {
			Mapping m = mappingService.findMappingById(entityId);
			s = (XmlSchema)schemaService.findSchemaById(m.getSourceId());
		}		
		
		Nonterminal r = (Nonterminal)elementService.findRootBySchemaId(s.getId(), true);
		
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
					session.addLogEntry(LogType.SUCCESS, messageSource.getMessage("~eu.dariah.de.minfba.schereg.editor.sample.log.processed_1_results", new Object[]{sw.getElapsedTime()}, locale));
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
