package de.unibamberg.minf.dme.controller.editors;

import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import de.unibamberg.minf.core.util.Stopwatch;
import de.unibamberg.minf.dme.controller.base.BaseMainEditorController;
import de.unibamberg.minf.dme.exception.GenericScheregException;
import de.unibamberg.minf.dme.model.PersistedSession;
import de.unibamberg.minf.dme.model.RightsContainer;
import de.unibamberg.minf.dme.model.base.Element;
import de.unibamberg.minf.dme.model.base.Grammar;
import de.unibamberg.minf.dme.model.mapping.base.MappedConcept;
import de.unibamberg.minf.dme.model.mapping.base.Mapping;
import de.unibamberg.minf.dme.pojo.AuthWrappedPojo;
import de.unibamberg.minf.dme.pojo.LogEntryPojo.LogType;
import de.unibamberg.minf.dme.pojo.converter.AuthWrappedPojoConverter;
import de.unibamberg.minf.dme.processing.CollectingResourceConsumptionService;
import de.unibamberg.minf.dme.service.interfaces.FunctionService;
import de.unibamberg.minf.dme.service.interfaces.GrammarService;
import de.unibamberg.minf.dme.service.interfaces.MappedConceptService;
import de.unibamberg.minf.dme.service.interfaces.PersistedSessionService;
import de.unibamberg.minf.dme.service.interfaces.SchemaService;
import de.unibamberg.minf.mapping.model.MappingExecGroup;
import de.unibamberg.minf.mapping.service.MappingExecutionService;
import de.unibamberg.minf.processing.model.base.Resource;
import eu.dariah.de.dariahsp.model.web.AuthPojo;
import de.unibamberg.minf.core.web.pojo.ModelActionPojo;

@Controller
@RequestMapping(value="/mapping/editor/{entityId}/")
public class MappingEditorController extends BaseMainEditorController {
	@Autowired private GrammarService grammarService;
	@Autowired private FunctionService functionService;
	@Autowired private MappedConceptService mappedConceptService;
	@Autowired private AuthWrappedPojoConverter authPojoConverter;
	@Autowired private PersistedSessionService sessionService;
	
	public MappingEditorController() {
		super("mappingEditor");
	}
	
	@RequestMapping(value="", method=RequestMethod.GET)
	public String getEditor(@PathVariable String entityId, Model model, HttpServletRequest request, Locale locale) {
		AuthPojo auth = authInfoHelper.getAuth(request);
		
		RightsContainer<Mapping> mapping = mappingService.findByIdAndAuth(entityId, auth);
		
		AuthWrappedPojo<Mapping> mappingPojo = authPojoConverter.convert(mapping, auth.getUserId()); 
		if (mapping==null) {
			return "redirect:/registry/";
		}
		
		model.addAttribute("mapping", mappingPojo);
		model.addAttribute("source", authPojoConverter.convert(schemaService.findByIdAndAuth(mappingPojo.getPojo().getSourceId(), auth), auth.getUserId()));
		model.addAttribute("target", authPojoConverter.convert(schemaService.findByIdAndAuth(mappingPojo.getPojo().getTargetId(), auth), auth.getUserId()));
		
		boolean oversized = false;
		try {
			PersistedSession s = sessionService.accessOrCreate(entityId, request.getSession().getId(), auth.getUserId(), messageSource, locale);
			model.addAttribute("session", s);
			
			if (s.getSampleInput()!=null) {
				if (s.getSampleInput().getBytes().length>this.maxTravelSize) {
					oversized = true;
				} else {
					model.addAttribute("sampleInput", s.getSampleInput());
				}
			}
			
		} catch (Exception e) {
			logger.error("Failed to load/initialize persisted session", e);
		}
		
		model.addAttribute("sampleInputOversize", oversized);
		
		return "mappingEditor";
	}
	
	@RequestMapping(value="/async/getConcepts", method=RequestMethod.GET)
	public @ResponseBody List<MappedConcept> getMappedConcepts(@PathVariable String entityId, Model model) {
		return mappedConceptService.findAllByMappingId(entityId);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/executeSampleMapping")
	public @ResponseBody ModelActionPojo executeSampleMapping(@PathVariable String entityId, HttpServletRequest request, HttpServletResponse response, Locale locale) {
		Stopwatch sw = new Stopwatch().start();
		ModelActionPojo result = new ModelActionPojo(true);
		result.setPojo(0);
		
		PersistedSession session = sessionService.access(entityId, request.getSession().getId(), authInfoHelper.getUserId(request));
		if (session==null) {
			response.setStatus(HttpServletResponse.SC_RESET_CONTENT);
			return null;
		}

		
		Mapping m = mappingService.findMappingById(entityId);
		List<Resource> inputResources = session.getSampleOutput();
		Element r = elementService.findRootBySchemaId(m.getTargetId(), true);
		List<MappedConcept> concepts = mappedConceptService.findAllByMappingId(m.getId(), true);
				
		MappingExecutionService mappingExecService = appContext.getBean(MappingExecutionService.class);
		CollectingResourceConsumptionService consumptionService = new CollectingResourceConsumptionService();
		
		// TODO Cache mapping execution group -> in ScheReg
		
		MappingExecGroup mapExecGroup = new MappingExecGroup();
		mapExecGroup.setMapping(m);
		mapExecGroup.setTargetElementTree(r);
		mapExecGroup.setTargetSchemaId(m.getTargetId());
		
		// TODO: Sources really needed?
		for (Grammar g : grammarService.findByEntityId(m.getId(), true)) {
			mapExecGroup.addGrammar(g);
		}
		
		for (MappedConcept c : concepts) {			
			mapExecGroup.addMappedConcept(c, functionService.findById(c.getFunctionId()));
		}
		
		
		
		
		mappingExecService.init(mapExecGroup, inputResources);
		mappingExecService.addConsumptionService(consumptionService);
		
		mappingExecService.run();
		
		session.setSampleMapped(consumptionService.getResources());

		if (session.getSampleMapped()!=null && session.getSampleMapped().size()>0) {
			result.setPojo(session.getSampleMapped().size());
			
			if (session.getSampleOutput().size()==1) {
				session.addLogEntry(LogType.SUCCESS, messageSource.getMessage("~de.unibamberg.minf.dme.editor.sample.log.translated_1_results", new Object[]{sw.getElapsedTime()}, locale));
			} else {
				session.addLogEntry(LogType.SUCCESS, messageSource.getMessage("~de.unibamberg.minf.dme.editor.sample.log.translated_n_results", new Object[]{sw.getElapsedTime(), consumptionService.getResources().size()}, locale));	
			}
		} else {
			session.addLogEntry(LogType.WARNING, messageSource.getMessage("~de.unibamberg.minf.dme.editor.sample.log.translated_no_results", null, locale));
		}
		
		sessionService.saveSession(session);
		return result;
	}
	
	@Override
	protected String getPrefix() {
		return "/mapping/editor/";
	}
}