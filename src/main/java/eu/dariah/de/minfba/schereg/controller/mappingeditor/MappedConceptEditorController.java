package eu.dariah.de.minfba.schereg.controller.mappingeditor;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import de.dariah.samlsp.model.pojo.AuthPojo;
import eu.dariah.de.minfba.core.metamodel.interfaces.MappedConcept;
import eu.dariah.de.minfba.core.metamodel.mapping.MappedConceptImpl;
import eu.dariah.de.minfba.core.web.pojo.ModelActionPojo;
import eu.dariah.de.minfba.schereg.controller.base.BaseScheregController;
import eu.dariah.de.minfba.schereg.exception.GenericScheregException;
import eu.dariah.de.minfba.schereg.service.interfaces.MappedConceptService;

@Controller
@RequestMapping(value="/mapping/editor/{mappingId}/mappedConcept/{mappedConceptId}")
public class MappedConceptEditorController extends BaseScheregController {
	@Autowired private MappedConceptService mappedConceptService;
	
	
	public MappedConceptEditorController() {
		super("mappingEditor");
	}
	
	@RequestMapping(value="/get", method=RequestMethod.GET)
	public @ResponseBody MappedConcept getMappedConcept(@PathVariable String mappingId, @PathVariable String mappedConceptId, Model model, HttpServletRequest request) {
		return mappedConceptService.findById(mappingId, mappedConceptId, true);
	}
	

	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method = RequestMethod.POST, value = "/async/remove")
	public @ResponseBody ModelActionPojo removeConcept(@PathVariable String mappingId, @PathVariable String mappedConceptId, HttpServletRequest request) throws GenericScheregException {
		mappedConceptService.removeMappedConcept(mappingId, mappedConceptId, authInfoHelper.getAuth(request));
		return new ModelActionPojo(true);
	}
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method = RequestMethod.POST, value = "/async/save")
	public @ResponseBody ModelActionPojo saveConcept(@PathVariable String mappingId, @PathVariable String mappedConceptId, @RequestParam String sourceElementId, @RequestParam(value="targetElementId[]") List<String> targetElementIds,  HttpServletRequest request) {
		AuthPojo auth = authInfoHelper.getAuth(request);
		
		MappedConcept c = null;
		if (mappedConceptId!=null && !mappedConceptId.equals("") && !mappedConceptId.equals("undefined")) {
			c = mappedConceptService.findById(mappedConceptId);
		}
		if (c==null) {
			c = new MappedConceptImpl();
			c.setTargetElementIds(new ArrayList<String>(1));
		}
		c.setEntityId(mappingId);
		c.setSourceElementId(sourceElementId);
		for (String targetElementId : targetElementIds) {
			if (!c.getTargetElementIds().contains(targetElementId)) {
				c.getTargetElementIds().add(targetElementId);
			}
		}
		
		mappedConceptService.saveMappedConcept(c, mappingId, auth);
		
		ModelActionPojo result = new ModelActionPojo(true);
		result.setPojo(c);
		
		return result;
	}
}
