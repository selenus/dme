package eu.dariah.de.minfba.schereg.controller.editors;

import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.dariah.de.minfba.core.metamodel.function.DescriptionGrammarImpl;
import eu.dariah.de.minfba.core.metamodel.interfaces.Element;
import eu.dariah.de.minfba.core.metamodel.interfaces.Label;
import eu.dariah.de.minfba.core.metamodel.interfaces.Nonterminal;
import eu.dariah.de.minfba.core.metamodel.interfaces.SchemaNature;
import eu.dariah.de.minfba.core.metamodel.interfaces.Terminal;
import eu.dariah.de.minfba.core.metamodel.xml.XmlSchemaNature;
import eu.dariah.de.minfba.core.metamodel.xml.XmlTerminal;
import eu.dariah.de.minfba.core.web.pojo.ModelActionPojo;
import eu.dariah.de.minfba.schereg.controller.base.BaseScheregController;
import eu.dariah.de.minfba.schereg.serialization.Reference;
import eu.dariah.de.minfba.schereg.service.ElementServiceImpl;
import eu.dariah.de.minfba.schereg.service.interfaces.ElementService;
import eu.dariah.de.minfba.schereg.service.interfaces.GrammarService;
import eu.dariah.de.minfba.schereg.service.interfaces.SchemaService;

@Controller
@RequestMapping(value={"/schema/editor/{schemaId}/element/{elementId}", "/mapping/editor/{schemaId}/element/{elementId}"})
public class ElementEditorController extends BaseScheregController {
	@Autowired private ElementService elementService;
	@Autowired private SchemaService schemaService;
	@Autowired private GrammarService grammarService;
	
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
		model.addAttribute("actionPath", "/schema/editor/" + schemaId + "/element/" + elementId + "/assignChild");
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
			model.addAttribute("actionPath", "/schema/editor/" + schemaId + "/element/" + elementId + "/async/saveNonterminal");
			return "elementEditor/form/edit_nonterminal";
		} else {
			model.addAttribute("actionPath", "/schema/editor/" + schemaId + "/element/" + elementId + "/async/saveLabel");
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
		model.addAttribute("element", new Nonterminal(schemaId, null));
		model.addAttribute("availableTerminals", schemaService.getAvailableTerminals(schemaId));
		model.addAttribute("actionPath", "/schema/editor/" + schemaId + "/element/" + elementId + "/async/saveNewNonterminal");
		return "elementEditor/form/edit_nonterminal";
	}
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method = RequestMethod.GET, value = "/form/new_label")
	public String getNewLabelForm(@PathVariable String schemaId, @PathVariable String elementId, Model model, HttpServletRequest request, HttpServletResponse response, Locale locale) {
		if (!schemaService.getUserCanWriteEntity(schemaId, authInfoHelper.getAuth(request).getUserId())) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return null;
		}
		model.addAttribute("element", new Label(schemaId, null));
		model.addAttribute("actionPath", "/schema/editor/" + schemaId + "/element/" + elementId + "/async/saveNewLabel");
		return "elementEditor/form/edit_label";
	}
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method = RequestMethod.GET, value = "/form/new_grammar")
	public String getNewGrammarForm(@PathVariable String schemaId, @PathVariable String elementId, Model model, HttpServletRequest request, HttpServletResponse response, Locale locale) {
		if (!schemaService.getUserCanWriteEntity(schemaId, authInfoHelper.getAuth(request).getUserId())) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return null;
		}
		model.addAttribute("grammar", new DescriptionGrammarImpl(schemaId, null));
		model.addAttribute("actionPath", "/schema/editor/" + schemaId + "/element/" + elementId + "/async/saveNewGrammar");
		return "grammarEditor/form/new";
	}
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method = RequestMethod.POST, value = "/async/saveLabel")
	public @ResponseBody ModelActionPojo saveLabel(@PathVariable String schemaId, @Valid Label element, BindingResult bindingResult, Locale locale, HttpServletRequest request, HttpServletResponse response) {
		if (!schemaService.getUserCanWriteEntity(schemaId, authInfoHelper.getAuth(request).getUserId())) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return new ModelActionPojo(false);
		}
		ModelActionPojo result = this.getActionResult(bindingResult, locale);
		if (result.isSuccess()) {
			Label l = (Label)elementService.findById(element.getId());
			l.setTransient(element.isTransient());
			l.setName(ElementServiceImpl.getNormalizedName(element.getName()));
			
			elementService.saveElement(element, authInfoHelper.getAuth(request));
		}		
		return result;
	}
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method = RequestMethod.POST, value = "/async/saveNonterminal")
	public @ResponseBody ModelActionPojo saveNonterminal(@PathVariable String schemaId, @Valid Nonterminal element, BindingResult bindingResult, Locale locale, HttpServletRequest request, HttpServletResponse response) {
		if (!schemaService.getUserCanWriteEntity(schemaId, authInfoHelper.getAuth(request).getUserId())) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return new ModelActionPojo(false);
		}
		ModelActionPojo result = this.getActionResult(bindingResult, locale);
		if (result.isSuccess()) {
			Nonterminal n = (Nonterminal)elementService.findById(element.getId());
			n.setTerminalId(element.getTerminalId());
			n.setTransient(element.isTransient());
			n.setName(ElementServiceImpl.getNormalizedName(element.getName()));
			
			elementService.saveElement(n, authInfoHelper.getAuth(request));
		}		
		return result;
	}
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method = RequestMethod.POST, value = "/async/saveNewLabel")
	public @ResponseBody ModelActionPojo saveNewLabel(@PathVariable String schemaId, @PathVariable String elementId, @Valid Label element, BindingResult bindingResult, Locale locale, HttpServletRequest request, HttpServletResponse response) {
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
	public @ResponseBody ModelActionPojo saveNewNonterminal(@PathVariable String schemaId, @PathVariable String elementId, @Valid Nonterminal element, BindingResult bindingResult, Locale locale, HttpServletRequest request, HttpServletResponse response) {
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
	public @ResponseBody ModelActionPojo saveNewGrammar(@PathVariable String schemaId, @PathVariable String elementId, @Valid DescriptionGrammarImpl grammar, BindingResult bindingResult, Locale locale, HttpServletRequest request, HttpServletResponse response) {
		if (!schemaService.getUserCanWriteEntity(schemaId, authInfoHelper.getAuth(request).getUserId())) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return new ModelActionPojo(false);
		}
		ModelActionPojo result = this.getActionResult(bindingResult, locale);
		if (result.isSuccess()) {
			grammarService.createAndAppendGrammar(schemaId, elementId, grammar.getGrammarName(), authInfoHelper.getAuth(request));
		}
		return result;
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/get")
	public @ResponseBody Element getElement(@PathVariable String schemaId, @PathVariable String elementId) {
		return elementService.findById(elementId);
	}
		
	@RequestMapping(method = RequestMethod.GET, value = "/async/getTerminal")
	public @ResponseBody Terminal getTerminal(@PathVariable String schemaId, @PathVariable String elementId) throws Exception {
		Element e = elementService.findById(elementId);
		String terminalId = null;
		if (e instanceof Nonterminal) {
			terminalId = ((Nonterminal)e).getTerminalId();
		} else {
			logger.warn("GetTerminal called for object of type {}", e.getClass().getSimpleName());
			throw new Exception("Invalid call of getTerminal on non-nonterminal");
		}
		
		SchemaNature s = schemaService.findSchemaById(schemaId);
		if (s instanceof XmlSchemaNature) {
			if (terminalId==null || terminalId.isEmpty()) {
				// None assigned yet
				return null;
			} else {
				for (XmlTerminal t : ((XmlSchemaNature)s).getTerminals()) {
					if (t.getId().equals(terminalId)) {
						return t;
					}
				}
			}
		} else {
			logger.warn("GetTerminal called for BaseSchema");
			throw new Exception("Invalid call of getTerminal on BaseSchema");
		}
		return null;
	}
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method = RequestMethod.GET, value = "/async/remove")
	public @ResponseBody ModelActionPojo removeElement(@PathVariable String schemaId, @PathVariable String elementId, HttpServletRequest request, HttpServletResponse response) {
		if (!schemaService.getUserCanWriteEntity(schemaId, authInfoHelper.getAuth(request).getUserId())) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return new ModelActionPojo(false);
		}
		elementService.removeElement(schemaId, elementId, authInfoHelper.getAuth(request));
		return new ModelActionPojo(true);
	}
	

}