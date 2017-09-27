package de.unibamberg.minf.dme.controller.editors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
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

import de.unibamberg.minf.dme.controller.base.BaseScheregController;
import de.unibamberg.minf.dme.exception.GenericScheregException;
import de.unibamberg.minf.dme.model.PersistedSession;
import de.unibamberg.minf.dme.model.base.Element;
import de.unibamberg.minf.dme.model.base.Grammar;
import de.unibamberg.minf.dme.model.grammar.GrammarImpl;
import de.unibamberg.minf.dme.model.mapping.MappedConceptImpl;
import de.unibamberg.minf.dme.model.mapping.TargetElementGroup;
import de.unibamberg.minf.dme.model.mapping.base.MappedConcept;
import de.unibamberg.minf.dme.pojo.ModelElementPojo;
import de.unibamberg.minf.dme.pojo.converter.ModelElementPojoConverter;
import de.unibamberg.minf.dme.service.interfaces.ElementService;
import de.unibamberg.minf.dme.service.interfaces.GrammarService;
import de.unibamberg.minf.dme.service.interfaces.MappedConceptService;
import de.unibamberg.minf.dme.service.interfaces.MappingService;
import de.unibamberg.minf.dme.service.interfaces.PersistedSessionService;
import eu.dariah.de.dariahsp.model.web.AuthPojo;
import de.unibamberg.minf.core.web.pojo.ModelActionPojo;

@Controller
@RequestMapping(value="/mapping/editor/{mappingId}/mappedConcept/{mappedConceptId}")
public class MappedConceptEditorController extends BaseScheregController {
	@Autowired private MappedConceptService mappedConceptService;
	@Autowired private ElementService elementService;
	@Autowired private GrammarService grammarService;
	@Autowired private PersistedSessionService sessionService;
	
	
	public MappedConceptEditorController() {
		super("mappingEditor");
	}
	
	@RequestMapping(value="/async/get", method=RequestMethod.GET)
	public @ResponseBody MappedConcept getMappedConcept(@PathVariable String mappingId, @PathVariable String mappedConceptId, Model model, HttpServletRequest request) {
		return mappedConceptService.findById(mappingId, mappedConceptId, true);
	}
	

	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method = RequestMethod.POST, value = "/async/remove")
	public @ResponseBody ModelActionPojo removeConcept(@PathVariable String mappingId, @PathVariable String mappedConceptId, HttpServletRequest request, HttpServletResponse response) throws GenericScheregException {
		AuthPojo auth = authInfoHelper.getAuth(request);
		if(!mappingService.getUserCanWriteEntity(mappingId, auth.getUserId())) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return null;
		}
		mappedConceptService.removeMappedConcept(mappingId, mappedConceptId, authInfoHelper.getAuth(request));
		return new ModelActionPojo(true);
	}
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method = RequestMethod.POST, value = "/async/save")
	public @ResponseBody ModelActionPojo saveConcept(@PathVariable String mappingId, @PathVariable String mappedConceptId, @RequestParam(value="sourceElementId[]") List<String> sourceElementId, @RequestParam(value="targetElementId[]") List<String> targetElementIds,  HttpServletRequest request, HttpServletResponse response) {
		AuthPojo auth = authInfoHelper.getAuth(request);
		if(!mappingService.getUserCanWriteEntity(mappingId, auth.getUserId())) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return new ModelActionPojo(false);
		}
		
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
	public String getEditForm(@PathVariable String mappingId, @PathVariable String mappedConceptId, HttpServletRequest request, HttpServletResponse response, Model model, Locale locale) {
		PersistedSession s = sessionService.access(mappingId, request.getSession().getId(), authInfoHelper.getUserId(request));
		if (s==null) {
			response.setStatus(HttpServletResponse.SC_RESET_CONTENT);
			return null;
		}
				
		AuthPojo auth = authInfoHelper.getAuth(request);
		// Checks both entity types
		if (schemaService.getUserCanWriteEntity(mappingId, auth.getUserId())) {
			model.addAttribute("readonly", false);
		} else {
			model.addAttribute("readonly", true);
		}
		
		
		
		MappedConcept mc = mappedConceptService.findById(mappingId, mappedConceptId, true);
		Map<Element, String> sampleInputs = new LinkedHashMap<Element, String>();
		
		List<Object> inputElementIds = new ArrayList<Object>();
		inputElementIds.addAll(mc.getElementGrammarIdsMap().keySet());
		
		for (Element e : elementService.findByIds(inputElementIds) ){
			sampleInputs.put(e, sessionService.getSampleInputValue(s, e.getId()));
		}

		model.addAttribute("sampleInputMap", sampleInputs);		
		model.addAttribute("concept", mc);		
		
		return "conceptEditor/form/edit";
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/function")
	public @ResponseBody String getConceptFunction(@PathVariable String mappingId, @PathVariable String mappedConceptId, Model model, Locale locale, HttpServletResponse response) throws IOException {
		return mappedConceptService.findById(mappingId, mappedConceptId, false).getFunctionId();
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/source")
	public @ResponseBody List<ModelElementPojo> getRenderedSource(@PathVariable String mappingId, @PathVariable String mappedConceptId, Model model, Locale locale, HttpServletResponse response) throws IOException, GenericScheregException {
		MappedConcept mc = mappedConceptService.findById(mappingId, mappedConceptId, true);
		if (mc==null) {
			response.getWriter().print("null");
			return null;
		}
		
		List<Object> loadIds = new ArrayList<Object>();
		loadIds.addAll(mc.getElementGrammarIdsMap().keySet());
		
		List<Element> sourceElements = elementService.findByIds(loadIds);

		for (String sourceId : mc.getElementGrammarIdsMap().keySet()) {
			for (Element sourceElement : sourceElements) {
				if (sourceId.equals(sourceElement.getId())) {
					sourceElement.setGrammars(new ArrayList<Grammar>());
					sourceElement.getGrammars().add((GrammarImpl) grammarService.findById(mc.getElementGrammarIdsMap().get(sourceId)));
					
					break;
				}
			}
		}
		return ModelElementPojoConverter.convertModelElements(sourceElements, false);
	}
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method = RequestMethod.POST, value = "/source/{sourceId}/remove")
	public @ResponseBody ModelActionPojo removeSource(@PathVariable String mappingId, @PathVariable String mappedConceptId, @PathVariable String sourceId, HttpServletRequest request) throws GenericScheregException {
		mappedConceptService.removeSourceElementById(authInfoHelper.getAuth(request), mappingId, mappedConceptId, sourceId);
		return new ModelActionPojo(true);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/target")
	public @ResponseBody List<ModelElementPojo> getRenderedTargets(@PathVariable String mappingId, @PathVariable String mappedConceptId, Model model, Locale locale, HttpServletResponse response) throws IOException, GenericScheregException {
		MappedConcept mc = mappedConceptService.findById(mappingId, mappedConceptId, true);
		if (mc==null) {
			response.getWriter().print("null");
			return null;
		}
		List<Object> targetElementIds = new ArrayList<Object>();
		targetElementIds.addAll(mc.getTargetElementIds());
				
		return ModelElementPojoConverter.convertModelElements(elementService.findByIds(targetElementIds), false);
	}
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method = RequestMethod.POST, value = "/target/{targetId}/remove")
	public @ResponseBody ModelActionPojo removeTarget(@PathVariable String mappingId, @PathVariable String mappedConceptId, @PathVariable String targetId, HttpServletRequest request, HttpServletResponse response) throws GenericScheregException {
		AuthPojo auth = authInfoHelper.getAuth(request);
		if(!mappingService.getUserCanWriteEntity(mappingId, auth.getUserId())) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return new ModelActionPojo(false);
		}
		MappedConcept mc = mappedConceptService.findById(mappingId, mappedConceptId, true);
		List<TargetElementGroup> removeGroups = new ArrayList<TargetElementGroup>();
		
		for (TargetElementGroup teg : mc.getTargetElementGroups()) {
			if (teg.getTargetElementIds().contains(targetId)) {
				teg.getTargetElementIds().remove(targetId);
				if (teg.getTargetElementIds().size()==0) {
					removeGroups.add(teg);
				}
			}
		}
		
		if (removeGroups.size()>0) {
			mc.getTargetElementGroups().removeAll(removeGroups);
		}
		
		// Delete mapping if there are no remaining targets
		if (mc.getTargetElementGroups().size()==0) {
			mappedConceptService.removeMappedConcept(mappingId, mc.getId(), auth);
		} else {		
			mappedConceptService.saveMappedConcept(mc, mappingId, auth);
		}
		return new ModelActionPojo(true);
	}
}
