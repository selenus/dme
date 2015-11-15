package eu.dariah.de.minfba.schereg.controller.mappingeditor;

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
import eu.dariah.de.minfba.core.metamodel.interfaces.Element;
import eu.dariah.de.minfba.core.metamodel.interfaces.Identifiable;
import eu.dariah.de.minfba.core.metamodel.interfaces.MappedConcept;
import eu.dariah.de.minfba.core.metamodel.interfaces.Mapping;
import eu.dariah.de.minfba.core.metamodel.interfaces.Schema;
import eu.dariah.de.minfba.core.metamodel.xml.XmlSchema;
import eu.dariah.de.minfba.core.util.Stopwatch;
import eu.dariah.de.minfba.core.web.pojo.ModelActionPojo;
import eu.dariah.de.minfba.mapping.MappingExecutionService;
import eu.dariah.de.minfba.mapping.MappingExecutionServiceImpl;
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
import eu.dariah.de.minfba.schereg.service.interfaces.MappedConceptService;
import eu.dariah.de.minfba.schereg.service.interfaces.MappingService;
import eu.dariah.de.minfba.schereg.service.interfaces.PersistedSessionService;
import eu.dariah.de.minfba.schereg.service.interfaces.SchemaService;

@Controller
@RequestMapping(value="/mapping/editor/{entityId}/")
public class MappingEditorController extends BaseMainEditorController {
	@Autowired private SchemaService schemaService;
	@Autowired private MappedConceptService mappedConceptService;
	@Autowired private AuthWrappedPojoConverter authPojoConverter;
	@Autowired private PersistedSessionService sessionService;
	
	public MappingEditorController() {
		super("mappingEditor");
	}
	
	@RequestMapping(value="", method=RequestMethod.GET)
	public String getEditor(@PathVariable String entityId, Model model, HttpServletRequest request) {
		AuthPojo auth = authInfoHelper.getAuth(request);
		
		AuthWrappedPojo<Mapping> mapping = authPojoConverter.convert(mappingService.findByIdAndAuth(entityId, auth), auth.getUserId()); 
		if (mapping==null) {
			return "redirect:/registry/";
		}
		
		model.addAttribute("mapping", mapping);
		model.addAttribute("source", authPojoConverter.convert(schemaService.findByIdAndAuth(mapping.getPojo().getSourceId(), auth), auth.getUserId()));
		model.addAttribute("target", authPojoConverter.convert(schemaService.findByIdAndAuth(mapping.getPojo().getTargetId(), auth), auth.getUserId()));
		
		try {
			model.addAttribute("session", sessionService.accessOrCreate(entityId, request.getSession().getId(), auth.getUserId()));
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
		Schema sTarget = schemaService.findSchemaById(m.getTargetId());
		List<Resource> inputResources = session.getSampleOutput();
		Element r = elementService.findRootBySchemaId(sTarget.getId(), true);
		List<MappedConcept> concepts = mappedConceptService.findAllByMappingId(m.getId(), true);
		
		if (concepts!=null) {
			for (MappedConcept c : concepts) {
				List<Identifiable> targetElements = elementService.getElementTrees(sTarget.getId(), c.getTargetElementIds());
				c.getGrammars().get(0).getTransformationFunctions().get(0).setOutputElements(elementService.convertToLabels(targetElements));
			}
		}
		
		MappingExecutionService mappingExecService = appContext.getBean(MappingExecutionService.class);
		CollectingResourceConsumptionService consumptionService = new CollectingResourceConsumptionService();
		
		try {
			mappingExecService.init(m, sTarget, inputResources, r, concepts);
			mappingExecService.addConsumptionService(consumptionService);
			
			mappingExecService.run();
			
			session.setSampleMapped(consumptionService.getResources());
		
			if (session.getSampleMapped()!=null && session.getSampleMapped().size()>0) {
				result.setPojo(session.getSampleMapped().size());
				
				if (session.getSampleOutput().size()==1) {
					session.addLogEntry(LogType.SUCCESS, String.format("~ Sample mapping executed (total %sms): 1 resource processed", sw.getElapsedTime(), consumptionService.getResources().size()));
				} else {
					session.addLogEntry(LogType.SUCCESS, String.format("~ Sample mapping executed (total %sms): %s resources processed", sw.getElapsedTime(), consumptionService.getResources().size()));	
				}
			} else {
				session.addLogEntry(LogType.WARNING, "~ Sample mapping executed: No resources found");
			}
			
			sessionService.saveSession(session);
			
		} catch (ProcessingConfigException e) {
			result.setSuccess(false);
			result.addObjectError(e.getMessage());
		}
		return result;
	}
}