package eu.dariah.de.minfba.schereg.controller.mappingeditor;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import de.dariah.samlsp.model.pojo.AuthPojo;
import eu.dariah.de.minfba.core.metamodel.interfaces.MappedConcept;
import eu.dariah.de.minfba.core.metamodel.interfaces.Mapping;
import eu.dariah.de.minfba.schereg.controller.base.BaseMainEditorController;
import eu.dariah.de.minfba.schereg.exception.GenericScheregException;
import eu.dariah.de.minfba.schereg.pojo.AuthWrappedPojo;
import eu.dariah.de.minfba.schereg.pojo.converter.AuthWrappedPojoConverter;
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
}
