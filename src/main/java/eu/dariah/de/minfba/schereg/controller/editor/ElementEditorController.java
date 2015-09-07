package eu.dariah.de.minfba.schereg.controller.editor;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.dariah.de.minfba.core.metamodel.Label;
import eu.dariah.de.minfba.core.metamodel.Nonterminal;
import eu.dariah.de.minfba.core.metamodel.function.DescriptionGrammarImpl;
import eu.dariah.de.minfba.core.metamodel.function.interfaces.DescriptionGrammar;
import eu.dariah.de.minfba.core.metamodel.interfaces.Element;
import eu.dariah.de.minfba.core.metamodel.interfaces.Schema;
import eu.dariah.de.minfba.core.metamodel.interfaces.Terminal;
import eu.dariah.de.minfba.core.metamodel.xml.XmlSchema;
import eu.dariah.de.minfba.core.metamodel.xml.XmlTerminal;
import eu.dariah.de.minfba.core.web.controller.BaseTranslationController;
import eu.dariah.de.minfba.core.web.pojo.ModelActionPojo;
import eu.dariah.de.minfba.schereg.service.interfaces.ElementService;
import eu.dariah.de.minfba.schereg.service.interfaces.GrammarService;
import eu.dariah.de.minfba.schereg.service.interfaces.SchemaService;

@Controller
@RequestMapping(value="/schema/editor/{schemaId}/element/{elementId}")
public class ElementEditorController extends BaseTranslationController {
	@Autowired private ElementService elementService;
	@Autowired private SchemaService schemaService;
	@Autowired private GrammarService grammarService;
	
	public ElementEditorController() {
		super("schemaEditor");
	}
		
	@RequestMapping(method = RequestMethod.GET, value = "/form/element")
	public String getEditElementForm(@PathVariable String schemaId, @PathVariable String elementId, Model model, Locale locale) {
		Element elem = elementService.findById(elementId);
		model.addAttribute("element", elem);
		
		if (elem instanceof Nonterminal) {
			model.addAttribute("availableTerminals", schemaService.getAvailableTerminals(schemaId));
			model.addAttribute("actionPath", "/schema/editor/" + schemaId + "/element/" + elementId + "/async/saveNonterminal");
			return "schemaEditor/form/element/edit_nonterminal";
		} else {
			model.addAttribute("actionPath", "/schema/editor/" + schemaId + "/element/" + elementId + "/async/saveLabel");
			return "schemaEditor/form/element/edit_label";
		}
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/form/new_nonterminal")
	public String getNewNonterminalForm(@PathVariable String schemaId, @PathVariable String elementId, Model model, Locale locale) {
		model.addAttribute("element", new Nonterminal());
		model.addAttribute("availableTerminals", schemaService.getAvailableTerminals(schemaId));
		model.addAttribute("actionPath", "/schema/editor/" + schemaId + "/element/" + elementId + "/async/saveNewNonterminal");
		return "schemaEditor/form/element/edit_nonterminal";
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/form/new_label")
	public String getNewLabelForm(@PathVariable String schemaId, @PathVariable String elementId, Model model, Locale locale) {
		model.addAttribute("element", new Label());
		model.addAttribute("actionPath", "/schema/editor/" + schemaId + "/element/" + elementId + "/async/saveNewLabel");
		return "schemaEditor/form/element/edit_label";
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/form/new_grammar")
	public String getNewGrammarForm(@PathVariable String schemaId, @PathVariable String elementId, Model model, Locale locale) {
		model.addAttribute("grammar", new DescriptionGrammarImpl());
		model.addAttribute("actionPath", "/schema/editor/" + schemaId + "/element/" + elementId + "/async/saveNewGrammar");
		return "schemaEditor/form/grammar/new";
	}
	
	
	@RequestMapping(method = RequestMethod.POST, value = "/async/saveLabel")
	public @ResponseBody ModelActionPojo saveLabel(@Valid Label element, BindingResult bindingResult, Locale locale) {
		ModelActionPojo result = this.getActionResult(bindingResult, locale);
		if (result.isSuccess()) {
			elementService.saveElement(element);
		}		
		return result;
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/async/saveNonterminal")
	public @ResponseBody ModelActionPojo saveNonterminal(@Valid Nonterminal element, BindingResult bindingResult, Locale locale) {
		ModelActionPojo result = this.getActionResult(bindingResult, locale);
		if (result.isSuccess()) {
			elementService.saveElement(element);
		}		
		return result;
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/async/saveNewLabel")
	public @ResponseBody ModelActionPojo saveNewLabel(@PathVariable String schemaId, @PathVariable String elementId, @Valid Label element, BindingResult bindingResult, Locale locale) {
		ModelActionPojo result = this.getActionResult(bindingResult, locale);
		if (result.isSuccess()) {
			elementService.createAndAppendElement(schemaId, elementId, element.getName());
		}
		return result;
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/async/saveNewNonterminal")
	public @ResponseBody ModelActionPojo saveNewNonterminal(@PathVariable String schemaId, @PathVariable String elementId, @Valid Nonterminal element, BindingResult bindingResult, Locale locale) {
		ModelActionPojo result = this.getActionResult(bindingResult, locale);
		if (result.isSuccess()) {
			elementService.createAndAppendElement(schemaId, elementId, element.getName());
		}
		return result;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/async/saveNewGrammar")
	public @ResponseBody ModelActionPojo saveNewGrammar(@PathVariable String schemaId, @PathVariable String elementId, @Valid DescriptionGrammarImpl grammar, BindingResult bindingResult, Locale locale) {
		ModelActionPojo result = this.getActionResult(bindingResult, locale);
		if (result.isSuccess()) {
			grammarService.createAndAppendGrammar(schemaId, elementId, grammar.getGrammarName());
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
		
		Schema s = schemaService.findSchemaById(schemaId);
		if (s instanceof XmlSchema) {
			if (terminalId==null || terminalId.isEmpty()) {
				// None assigned yet
				return null;
			} else {
				for (XmlTerminal t : ((XmlSchema)s).getTerminals()) {
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
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/remove")
	public @ResponseBody Element removeElement(@PathVariable String schemaId, @PathVariable String elementId) {
		return elementService.removeElement(schemaId, elementId);
	}
	

}