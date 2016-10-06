package eu.dariah.de.minfba.schereg.controller.editors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.dariah.samlsp.model.pojo.AuthPojo;
import eu.dariah.de.minfba.core.metamodel.function.DescriptionGrammarImpl;
import eu.dariah.de.minfba.core.metamodel.function.GrammarContainer;
import eu.dariah.de.minfba.core.metamodel.interfaces.Element;
import eu.dariah.de.minfba.core.metamodel.interfaces.MappedConcept;
import eu.dariah.de.minfba.core.metamodel.mapping.MappedConceptImpl;
import eu.dariah.de.minfba.core.metamodel.mapping.TargetElementGroup;
import eu.dariah.de.minfba.core.web.pojo.ModelActionPojo;
import eu.dariah.de.minfba.schereg.controller.base.BaseScheregController;
import eu.dariah.de.minfba.schereg.exception.GenericScheregException;
import eu.dariah.de.minfba.schereg.model.MappableElement;
import eu.dariah.de.minfba.schereg.model.PersistedSession;
import eu.dariah.de.minfba.schereg.service.interfaces.ElementService;
import eu.dariah.de.minfba.schereg.service.interfaces.MappedConceptService;
import eu.dariah.de.minfba.schereg.service.interfaces.MappingService;

@Controller
@RequestMapping(value="/mapping/editor/{mappingId}/mappedConcept/{mappedConceptId}")
public class MappedConceptEditorController extends BaseScheregController {
	@Autowired protected MappingService mappingService;
	@Autowired private MappedConceptService mappedConceptService;
	@Autowired private ElementService elementService;
	
	@Autowired private ObjectMapper objMapper;
	
	public MappedConceptEditorController() {
		super("mappingEditor");
	}
	
	@RequestMapping(value="/async/get", method=RequestMethod.GET)
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
	public @ResponseBody ModelActionPojo saveConcept(@PathVariable String mappingId, @PathVariable String mappedConceptId, @RequestParam(value="sourceElementId[]") List<String> sourceElementId, @RequestParam(value="targetElementId[]") List<String> targetElementIds,  HttpServletRequest request) {
		AuthPojo auth = authInfoHelper.getAuth(request);
		
		MappedConcept c = null;
		if (mappedConceptId!=null && !mappedConceptId.equals("") && !mappedConceptId.equals("undefined")) {
			c = mappedConceptService.findById(mappedConceptId);
		}
		if (c==null) {
			c = new MappedConceptImpl();
			c.setElementGrammarIdsMap(new HashMap<String, String>());
		}
		c.setEntityId(mappingId);
		
		for (String sourceId : sourceElementId) {
			if (!c.getElementGrammarIdsMap().keySet().contains(sourceId)) {
				c.getElementGrammarIdsMap().put(sourceId, null);
			}
		}

		for (String targetElementId : targetElementIds) {
			if (c.getTargetElementIds()==null || !c.getTargetElementIds().contains(targetElementId)) {
				TargetElementGroup g = new TargetElementGroup();
				g.addTargetElementId(targetElementId);
				c.addTargetElementGroup(g);
			}
		}
		
		mappedConceptService.saveMappedConcept(c, mappingId, auth);
		
		ModelActionPojo result = new ModelActionPojo(true);
		result.setPojo(c);
		
		return result;
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/form/edit")
	public String getEditForm(@PathVariable String mappingId, @PathVariable String mappedConceptId, HttpServletRequest request, Model model, Locale locale) {
		AuthPojo auth = authInfoHelper.getAuth(request);
		if (mappingService.findMappingById(mappingId)!=null && mappingService.getHasWriteAccess(mappingId, auth.getUserId())) {
			model.addAttribute("readonly", false);
		} else if (schemaService.findSchemaById(mappingId)!=null && schemaService.getHasWriteAccess(mappingId, auth.getUserId())) {
			model.addAttribute("readonly", false);
		} else {
			model.addAttribute("readonly", true);
		}
		
		/*
		 * This will be required later
		 
			if (s.getSelectedValueMap()!=null) {
				String elementId = referenceService.findReferenceBySchemaAndChildId(schemaId, grammarId).getId();			
				if (s.getSelectedValueMap().containsKey(elementId)) {
					model.addAttribute("elementSample", s.getSelectedValueMap().get(elementId));
				}
			} 
		*/
		
		MappedConcept mc = mappedConceptService.findById(mappingId, mappedConceptId, true);
		model.addAttribute("concept", mc);		
		
		return "mappingEditor/form/concept/edit";
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/getRendered")
	public @ResponseBody String getRenderedHierarchy(@PathVariable String mappingId, @PathVariable String mappedConceptId, Model model, Locale locale, HttpServletResponse response) throws IOException {
		MappedConcept mc = mappedConceptService.findById(mappingId, mappedConceptId, true);
		List<Object> sourceElementIds = new ArrayList<Object>();
		sourceElementIds.addAll(mc.getElementGrammarIdsMap().keySet());
				
		// Prepare easier-to-use object-based map
		List<Element> sourceElements = elementService.findByIds(sourceElementIds);
		Map<Element, DescriptionGrammarImpl> sourceElementMap = new HashMap<Element, DescriptionGrammarImpl>();
		for (String sourceId : mc.getSourceElementMap().keySet()) {
			for (Element sourceElement : sourceElements) {
				if (sourceId.equals(sourceElement.getId())) {
					sourceElementMap.put(sourceElement, mc.getSourceElementMap().get(sourceId));
					break;
				}
			}
		}
	
		
		
		return objMapper.writeValueAsString(sourceElementMap);
	}
}
