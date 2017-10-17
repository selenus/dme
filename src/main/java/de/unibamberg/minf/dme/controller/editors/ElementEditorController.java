package de.unibamberg.minf.dme.controller.editors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import de.unibamberg.minf.dme.controller.base.BaseScheregController;
import de.unibamberg.minf.dme.exception.GenericScheregException;
import de.unibamberg.minf.dme.model.base.Element;
import de.unibamberg.minf.dme.model.base.Label;
import de.unibamberg.minf.dme.model.base.Nonterminal;
import de.unibamberg.minf.dme.model.base.Terminal;
import de.unibamberg.minf.dme.model.datamodel.LabelImpl;
import de.unibamberg.minf.dme.model.datamodel.NonterminalImpl;
import de.unibamberg.minf.dme.model.datamodel.base.Datamodel;
import de.unibamberg.minf.dme.model.datamodel.base.DatamodelNature;
import de.unibamberg.minf.dme.model.datamodel.natures.XmlDatamodelNature;
import de.unibamberg.minf.dme.model.datamodel.natures.xml.XmlTerminal;
import de.unibamberg.minf.dme.model.grammar.GrammarImpl;
import de.unibamberg.minf.dme.pojo.ModelElementPojo;
import de.unibamberg.minf.dme.pojo.converter.ModelElementPojoConverter;
import de.unibamberg.minf.dme.serialization.Reference;
import de.unibamberg.minf.dme.service.ElementServiceImpl;
import de.unibamberg.minf.dme.service.interfaces.ElementService;
import de.unibamberg.minf.dme.service.interfaces.GrammarService;
import de.unibamberg.minf.dme.service.interfaces.MappedConceptService;
import eu.dariah.de.dariahsp.model.web.AuthPojo;
import de.unibamberg.minf.core.web.pojo.ModelActionPojo;

@Controller
@RequestMapping(value={"/model/editor/{schemaId}/element/{elementId}", "/mapping/editor/{schemaId}/element/{elementId}"})
public class ElementEditorController extends BaseScheregController {
	@Autowired private ElementService elementService;
	@Autowired private GrammarService grammarService;
	
	@Autowired private MappedConceptService mappedConceptService;
	
	public ElementEditorController() {
		super("schemaEditor");
	}
		
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method = RequestMethod.POST, value = "/assignChild")
	public @ResponseBody ModelActionPojo assignChild(@PathVariable String schemaId, @PathVariable String elementId, @RequestParam(value="element-id") String childId, Model model, Locale locale, HttpServletRequest request, HttpServletResponse response) {		
		if (!schemaService.getUserCanWriteEntity(schemaId, authInfoHelper.getAuth(request).getUserId())) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return new ModelActionPojo(false);
		}
		
		Reference parentReference = elementService.assignChildTreeToParent(schemaId, elementId, childId);		
		if (parentReference!=null) {
			return new ModelActionPojo(true);
		} else {
			return new ModelActionPojo(false);
		}
	}
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method = RequestMethod.GET, value = "/form/assignChild")
	public String getAssignChildForm(@PathVariable String schemaId, @PathVariable String elementId, Model model, Locale locale, HttpServletRequest request, HttpServletResponse response) {	
		if (!schemaService.getUserCanWriteEntity(schemaId, authInfoHelper.getAuth(request).getUserId())) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return null;
		}		
		model.addAttribute("actionPath", "/model/editor/" + schemaId + "/element/" + elementId + "/assignChild");
		return "elementEditor/form/assign_child";
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/form/element")
	public String getEditElementForm(@PathVariable String schemaId, @PathVariable String elementId, Model model, Locale locale, HttpServletRequest request) {		
		Element elem = elementService.findById(elementId);
		model.addAttribute("element", elem);
		
		if (!schemaService.getUserCanWriteEntity(schemaId, authInfoHelper.getAuth(request).getUserId())) {
			model.addAttribute("readonly", true);
		} else {
			model.addAttribute("readonly", false);
		}
		if (elem instanceof Nonterminal) {
			model.addAttribute("availableTerminals", schemaService.getAvailableTerminals(schemaId));
			model.addAttribute("actionPath", "/model/editor/" + schemaId + "/element/" + elementId + "/async/saveNonterminal");
			return "elementEditor/form/edit_nonterminal";
		} else {
			model.addAttribute("actionPath", "/model/editor/" + schemaId + "/element/" + elementId + "/async/saveLabel");
			return "elementEditor/form/edit_label";
		}
	}
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method = RequestMethod.GET, value = "/form/new_nonterminal")
	public String getNewNonterminalForm(@PathVariable String schemaId, @PathVariable String elementId, HttpServletRequest request, HttpServletResponse response, Model model, Locale locale) {
		if (!schemaService.getUserCanWriteEntity(schemaId, authInfoHelper.getAuth(request).getUserId())) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return null;
		}
		model.addAttribute("element", new NonterminalImpl(schemaId, null));
		model.addAttribute("availableTerminals", schemaService.getAvailableTerminals(schemaId));
		model.addAttribute("actionPath", "/model/editor/" + schemaId + "/element/" + elementId + "/async/saveNewNonterminal");
		return "elementEditor/form/edit_nonterminal";
	}
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method = RequestMethod.GET, value = "/form/new_label")
	public String getNewLabelForm(@PathVariable String schemaId, @PathVariable String elementId, Model model, HttpServletRequest request, HttpServletResponse response, Locale locale) {
		if (!schemaService.getUserCanWriteEntity(schemaId, authInfoHelper.getAuth(request).getUserId())) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return null;
		}
		model.addAttribute("element", new LabelImpl(schemaId, null));
		model.addAttribute("actionPath", "/model/editor/" + schemaId + "/element/" + elementId + "/async/saveNewLabel");
		return "elementEditor/form/edit_label";
	}
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method = RequestMethod.GET, value = "/form/new_grammar")
	public String getNewGrammarForm(@PathVariable String schemaId, @PathVariable String elementId, Model model, HttpServletRequest request, HttpServletResponse response, Locale locale) {
		if (!schemaService.getUserCanWriteEntity(schemaId, authInfoHelper.getAuth(request).getUserId())) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return null;
		}
		model.addAttribute("grammar", new GrammarImpl(schemaId, null));
		model.addAttribute("actionPath", "/model/editor/" + schemaId + "/element/" + elementId + "/async/saveNewGrammar");
		return "grammarEditor/form/new";
	}
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method = RequestMethod.POST, value = "/async/saveLabel")
	public @ResponseBody ModelActionPojo saveLabel(@PathVariable String schemaId, @Valid LabelImpl element, BindingResult bindingResult, Locale locale, HttpServletRequest request, HttpServletResponse response) {
		if (!schemaService.getUserCanWriteEntity(schemaId, authInfoHelper.getAuth(request).getUserId())) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return new ModelActionPojo(false);
		}
		ModelActionPojo result = this.getActionResult(bindingResult, locale);
		if (result.isSuccess()) {
			Label l = (Label)elementService.findById(element.getId());
			l.setTransient(element.isTransient());
			l.setName(ElementServiceImpl.getNormalizedName(element.getName()));
			l.setEntityId(schemaId);
			
			elementService.saveElement(element, authInfoHelper.getAuth(request));
		}		
		return result;
	}
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method = RequestMethod.POST, value = "/async/saveNonterminal")
	public @ResponseBody ModelActionPojo saveNonterminal(@PathVariable String schemaId, @Valid NonterminalImpl element, BindingResult bindingResult, Locale locale, HttpServletRequest request, HttpServletResponse response) {
		if (!schemaService.getUserCanWriteEntity(schemaId, authInfoHelper.getAuth(request).getUserId())) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return new ModelActionPojo(false);
		}
		ModelActionPojo result = this.getActionResult(bindingResult, locale);
		if (result.isSuccess()) {
			Nonterminal n = (Nonterminal)elementService.findById(element.getId());
			// Not changeable here
			//n.setTerminalId(element.getTerminalId());
			n.setTransient(element.isTransient());
			n.setName(ElementServiceImpl.getNormalizedName(element.getName()));
			n.setEntityId(schemaId);
			
			elementService.saveElement(n, authInfoHelper.getAuth(request));
		}		
		return result;
	}
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method = RequestMethod.POST, value = "/async/saveNewLabel")
	public @ResponseBody ModelActionPojo saveNewLabel(@PathVariable String schemaId, @PathVariable String elementId, @Valid LabelImpl element, BindingResult bindingResult, Locale locale, HttpServletRequest request, HttpServletResponse response) {
		if (!schemaService.getUserCanWriteEntity(schemaId, authInfoHelper.getAuth(request).getUserId())) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return new ModelActionPojo(false);
		}
		ModelActionPojo result = this.getActionResult(bindingResult, locale);
		if (result.isSuccess()) {
			elementService.createAndAppendElement(schemaId, elementId, element.getName(), authInfoHelper.getAuth(request));
		}
		return result;
	}
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method = RequestMethod.POST, value = "/async/saveNewNonterminal")
	public @ResponseBody ModelActionPojo saveNewNonterminal(@PathVariable String schemaId, @PathVariable String elementId, @Valid NonterminalImpl element, BindingResult bindingResult, Locale locale, HttpServletRequest request, HttpServletResponse response) {
		if (!schemaService.getUserCanWriteEntity(schemaId, authInfoHelper.getAuth(request).getUserId())) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return new ModelActionPojo(false);
		}
		ModelActionPojo result = this.getActionResult(bindingResult, locale);
		if (result.isSuccess()) {
			element.setEntityId(schemaId);
			elementService.createAndAppendElement(schemaId, elementId, element.getName(), authInfoHelper.getAuth(request));
		}
		return result;
	}

	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method = RequestMethod.POST, value = "/async/saveNewGrammar")
	public @ResponseBody ModelActionPojo saveNewGrammar(@PathVariable String schemaId, @PathVariable String elementId, @Valid GrammarImpl grammar, BindingResult bindingResult, Locale locale, HttpServletRequest request, HttpServletResponse response) {
		if (!schemaService.getUserCanWriteEntity(schemaId, authInfoHelper.getAuth(request).getUserId())) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return new ModelActionPojo(false);
		}
		ModelActionPojo result = this.getActionResult(bindingResult, locale);
		if (result.isSuccess()) {
			grammarService.createAndAppendGrammar(schemaId, elementId, grammar.getName(), authInfoHelper.getAuth(request));
		}
		return result;
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/get")
	public @ResponseBody ModelElementPojo getElement(@PathVariable String schemaId, @PathVariable String elementId, HttpServletRequest request, HttpServletResponse response) throws IOException, GenericScheregException {
		AuthPojo auth = authInfoHelper.getAuth(request);
		Element result = elementService.findById(elementId);
		if (result==null) {
			response.getWriter().print("null");
			response.setContentType("application/json");
		}
		
		Map<String, List<String>> nonterminalNatureClassesMap = new HashMap<String, List<String>>();
		Datamodel m = schemaService.findByIdAndAuth(schemaId, auth).getElement();
		
		if (m.getNatures()!=null) {
			for (DatamodelNature n : m.getNatures()) {
				if (n.getNonterminalTerminalIdMap()!=null) {
					for (String nId : n.getNonterminalTerminalIdMap().keySet()) {
						List<String> natureClasses = nonterminalNatureClassesMap.get(nId);
						if (natureClasses==null) {
							natureClasses = new ArrayList<String>();
						}
						natureClasses.add(n.getClass().getName());
						nonterminalNatureClassesMap.put(nId, natureClasses);
					}
				}
			}
		}
		
		return ModelElementPojoConverter.convertModelElement(result, nonterminalNatureClassesMap, false, true);
	}
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method = RequestMethod.GET, value = "/async/remove")
	public @ResponseBody ModelActionPojo removeElement(@PathVariable String schemaId, @PathVariable String elementId, HttpServletRequest request, HttpServletResponse response) {
		if (!schemaService.getUserCanWriteEntity(schemaId, authInfoHelper.getAuth(request).getUserId())) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return new ModelActionPojo(false);
		}
		mappedConceptService.removeElementReferences(schemaId, elementId);
		elementService.removeElement(schemaId, elementId, authInfoHelper.getAuth(request));
		
		return new ModelActionPojo(true);
	}
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method = RequestMethod.GET, value = "/async/disable")
	public @ResponseBody ModelActionPojo disableElement(@PathVariable String schemaId, @PathVariable String elementId, @RequestParam boolean disabled, HttpServletRequest request, HttpServletResponse response) {
		if (!schemaService.getUserCanWriteEntity(schemaId, authInfoHelper.getAuth(request).getUserId())) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return new ModelActionPojo(false);
		}
		
		Element e = elementService.findById(elementId);
		e.setDisabled(disabled);
		
		elementService.saveElement(e, authInfoHelper.getAuth(request));
		return new ModelActionPojo(true);
	}
	
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method = RequestMethod.GET, value = "/async/setProcessingRoot")
	public @ResponseBody ModelActionPojo setProcessingRoot(@PathVariable String schemaId, @PathVariable String elementId, HttpServletRequest request, HttpServletResponse response) throws GenericScheregException {
		AuthPojo auth = authInfoHelper.getAuth(request);
		if (!schemaService.getUserCanWriteEntity(schemaId, auth.getUserId())) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return new ModelActionPojo(false);
		}
		
		Element e = elementService.findById(elementId);
		if (e instanceof Nonterminal && e.getEntityId().equals(schemaId)) {
			schemaService.setProcessingRoot(schemaId, elementId, auth);
		} else {
			throw new GenericScheregException("Failed to set processing root. Must be part of current schema and nonterminal node.");
		}
		return new ModelActionPojo(true);
	}
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method = RequestMethod.POST, value = "/async/clone", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
	public @ResponseBody ModelActionPojo cloneElement(@PathVariable String schemaId, @PathVariable String elementId, @RequestParam(value = "path[]") String[] path, HttpServletRequest request, HttpServletResponse response) throws GenericScheregException {
		AuthPojo auth = authInfoHelper.getAuth(request);
		elementService.cloneElement(elementId, path, auth);
		return new ModelActionPojo(true);
	}
	

}