package eu.dariah.de.minfba.schereg.controller.editors;

import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import de.dariah.samlsp.model.pojo.AuthPojo;
import eu.dariah.de.minfba.core.metamodel.Nonterminal;
import eu.dariah.de.minfba.core.metamodel.function.interfaces.DescriptionGrammar;
import eu.dariah.de.minfba.core.metamodel.interfaces.Element;
import eu.dariah.de.minfba.core.metamodel.interfaces.Identifiable;
import eu.dariah.de.minfba.core.metamodel.interfaces.MappedConcept;
import eu.dariah.de.minfba.core.metamodel.interfaces.Mapping;
import eu.dariah.de.minfba.core.metamodel.interfaces.Schema;
import eu.dariah.de.minfba.core.metamodel.xml.XmlSchema;
import eu.dariah.de.minfba.core.util.Stopwatch;
import eu.dariah.de.minfba.core.web.pojo.ModelActionPojo;
import eu.dariah.de.minfba.mapping.model.MappingExecGroup;
import eu.dariah.de.minfba.mapping.service.MappingExecutionService;
import eu.dariah.de.minfba.mapping.service.MappingExecutionServiceImpl;
import eu.dariah.de.minfba.processing.exception.ProcessingConfigException;
import eu.dariah.de.minfba.processing.model.base.Resource;
import eu.dariah.de.minfba.processing.service.xml.XmlStringProcessingService;
import eu.dariah.de.minfba.schereg.controller.base.BaseMainEditorController;
import eu.dariah.de.minfba.schereg.exception.GenericScheregException;
import eu.dariah.de.minfba.schereg.model.MappableElement;
import eu.dariah.de.minfba.schereg.model.PersistedSession;
import eu.dariah.de.minfba.schereg.pojo.AuthWrappedPojo;
import eu.dariah.de.minfba.schereg.pojo.LogEntryPojo.LogType;
import eu.dariah.de.minfba.schereg.pojo.converter.AuthWrappedPojoConverter;
import eu.dariah.de.minfba.schereg.processing.CollectingResourceConsumptionService;
import eu.dariah.de.minfba.schereg.service.ElementServiceImpl;
import eu.dariah.de.minfba.schereg.service.interfaces.GrammarService;
import eu.dariah.de.minfba.schereg.service.interfaces.MappedConceptService;
import eu.dariah.de.minfba.schereg.service.interfaces.MappingService;
import eu.dariah.de.minfba.schereg.service.interfaces.PersistedSessionService;
import eu.dariah.de.minfba.schereg.service.interfaces.SchemaService;

@Controller
@RequestMapping(value="/mapping/editor/{entityId}/")
public class MappingEditorController extends BaseMainEditorController {
	@Autowired private SchemaService schemaService;
	@Autowired private GrammarService grammarService;
	@Autowired private MappedConceptService mappedConceptService;
	@Autowired private AuthWrappedPojoConverter authPojoConverter;
	@Autowired private PersistedSessionService sessionService;
	
	public MappingEditorController() {
		super("mappingEditor");
	}
	
	@RequestMapping(value="", method=RequestMethod.GET)
	public String getEditor(@PathVariable String entityId, Model model, HttpServletRequest request, Locale locale) {
		AuthPojo auth = authInfoHelper.getAuth(request);
		
		AuthWrappedPojo<Mapping> mapping = authPojoConverter.convert(mappingService.findByIdAndAuth(entityId, auth), auth.getUserId()); 
		if (mapping==null) {
			return "redirect:/registry/";
		}
		
		model.addAttribute("mapping", mapping);
		model.addAttribute("source", authPojoConverter.convert(schemaService.findByIdAndAuth(mapping.getPojo().getSourceId(), auth), auth.getUserId()));
		model.addAttribute("target", authPojoConverter.convert(schemaService.findByIdAndAuth(mapping.getPojo().getTargetId(), auth), auth.getUserId()));
		
		try {
			model.addAttribute("session", sessionService.accessOrCreate(entityId, request.getSession().getId(), auth.getUserId(), messageSource, locale));
		} catch (GenericScheregException e) {
			logger.error("Failed to load/initialize persisted session", e);
		} 
		return "mappingEditor";
	}
	
	@RequestMapping(value="/async/getConcepts", method=RequestMethod.GET)
	public @ResponseBody List<MappedConcept> getMappedConcepts(@PathVariable String entityId, Model model) {
		return mappedConceptService.findAllByMappingId(entityId);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/executeSampleMapping")
	public @ResponseBody ModelActionPojo executeSampleMapping(@PathVariable String entityId, HttpServletRequest request, Locale locale) {
		Stopwatch sw = new Stopwatch();
		ModelActionPojo result = new ModelActionPojo(true);
		result.setPojo(0);
		
		PersistedSession session = sessionService.access(entityId, request.getSession().getId(), authInfoHelper.getUserId(request));

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
		for (DescriptionGrammar g : grammarService.findByEntityId(m.getEntityId(), true)) {
			mapExecGroup.addGrammar(g);
		}
		
		for (MappedConcept c : concepts) {
			mapExecGroup.addMappedConcept(c, mappedConceptService.getConceptFunction(c.getEntityId(), c.getId()));
		}
		
		
		mappingExecService.init(mapExecGroup, inputResources);
		mappingExecService.addConsumptionService(consumptionService);
		
		mappingExecService.run();
		
		session.setSampleMapped(consumptionService.getResources());

		if (session.getSampleMapped()!=null && session.getSampleMapped().size()>0) {
			result.setPojo(session.getSampleMapped().size());
			
			if (session.getSampleOutput().size()==1) {
				session.addLogEntry(LogType.SUCCESS, messageSource.getMessage("~eu.dariah.de.minfba.schereg.editor.sample.log.translated_1_results", new Object[]{sw.getElapsedTime()}, locale));
			} else {
				session.addLogEntry(LogType.SUCCESS, messageSource.getMessage("~eu.dariah.de.minfba.schereg.editor.sample.log.translated_n_results", new Object[]{sw.getElapsedTime(), consumptionService.getResources().size()}, locale));	
			}
		} else {
			session.addLogEntry(LogType.WARNING, messageSource.getMessage("~eu.dariah.de.minfba.schereg.editor.sample.log.translated_no_results", null, locale));
		}
		
		sessionService.saveSession(session);
		return result;
	}
}