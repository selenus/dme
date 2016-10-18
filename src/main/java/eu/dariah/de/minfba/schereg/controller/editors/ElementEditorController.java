package eu.dariah.de.minfba.schereg.controller.editors;

import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
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

import de.dariah.aai.javasp.web.helper.AuthInfoHelper;
import eu.dariah.de.minfba.core.metamodel.Label;
import eu.dariah.de.minfba.core.metamodel.Nonterminal;
import eu.dariah.de.minfba.core.metamodel.function.DescriptionGrammarImpl;
import eu.dariah.de.minfba.core.metamodel.interfaces.Element;
import eu.dariah.de.minfba.core.metamodel.interfaces.Schema;
import eu.dariah.de.minfba.core.metamodel.interfaces.Terminal;
import eu.dariah.de.minfba.core.metamodel.xml.XmlSchema;
import eu.dariah.de.minfba.core.metamodel.xml.XmlTerminal;
import eu.dariah.de.minfba.core.web.controller.BaseTranslationController;
import eu.dariah.de.minfba.core.web.pojo.ModelActionPojo;
import eu.dariah.de.minfba.schereg.controller.CommonController;
import eu.dariah.de.minfba.schereg.controller.base.BaseScheregController;
import eu.dariah.de.minfba.schereg.model.RightsContainer;
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
		
	@RequestMapping(method = RequestMethod.POST, value = "/assignChild")
	public @ResponseBody ModelActionPojo assignChild(@PathVariable String schemaId, @PathVariable String elementId, @RequestParam(value="element-id") String childId, Model model, Locale locale, HttpServletRequest request) {		
		Reference parentReference = elementService.assignChildTreeToParent(schemaId, elementId, childId);		
		if (parentReference!=null) {
			return new ModelActionPojo(true);
		} else {
			return new ModelActionPojo(false);
		}
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/form/assignChild")
	public String getAssignChildForm(@PathVariable String schemaId, @PathVariable String elementId, Model model, Locale locale, HttpServletRequest request) {		
		model.addAttribute("actionPath", "/schema/editor/" + schemaId + "/element/" + elementId + "/assignChild");
		return "schemaEditor/form/element/assign_child";
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/form/element")
	public String getEditElementForm(@PathVariable String schemaId, @PathVariable String elementId, Model model, Locale locale, HttpServletRequest request) {		
		Element elem = elementService.findById(elementId);
		model.addAttribute("element", elem);
		
		if (!schemaService.getHasWriteAccess(schemaId, authInfoHelper.getAuth(request).getUserId())) {
			model.addAttribute("readonly", true);
		} else {
			model.addAttribute("readonly", false);
		}
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
		model.addAttribute("element", new Nonterminal(schemaId, null));
		model.addAttribute("availableTerminals", schemaService.getAvailableTerminals(schemaId));
		model.addAttribute("actionPath", "/schema/editor/" + schemaId + "/element/" + elementId + "/async/saveNewNonterminal");
		return "schemaEditor/form/element/edit_nonterminal";
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/form/new_label")
	public String getNewLabelForm(@PathVariable String schemaId, @PathVariable String elementId, Model model, Locale locale) {
		model.addAttribute("element", new Label(schemaId, null));
		model.addAttribute("actionPath", "/schema/editor/" + schemaId + "/element/" + elementId + "/async/saveNewLabel");
		return "schemaEditor/form/element/edit_label";
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/form/new_grammar")
	public String getNewGrammarForm(@PathVariable String schemaId, @PathVariable String elementId, Model model, Locale locale) {
		model.addAttribute("grammar", new DescriptionGrammarImpl(schemaId, null));
		model.addAttribute("actionPath", "/schema/editor/" + schemaId + "/element/" + elementId + "/async/saveNewGrammar");
		return "schemaEditor/form/grammar/new";
	}
	
	
	@RequestMapping(method = RequestMethod.POST, value = "/async/saveLabel")
	public @ResponseBody ModelActionPojo saveLabel(@Valid Label element, BindingResult bindingResult, Locale locale, HttpServletRequest request) {
		ModelActionPojo result = this.getActionResult(bindingResult, locale);
		if (result.isSuccess()) {
			Label l = (Label)elementService.findById(element.getId());
			l.setTransient(element.isTransient());
			l.setName(ElementServiceImpl.getNormalizedName(element.getName()));
			
			elementService.saveElement(element, authInfoHelper.getAuth(request));
		}		
		return result;
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/async/saveNonterminal")
	public @ResponseBody ModelActionPojo saveNonterminal(@Valid Nonterminal element, BindingResult bindingResult, Locale locale, HttpServletRequest request) {
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
	
	@RequestMapping(method = RequestMethod.POST, value = "/async/saveNewLabel")
	public @ResponseBody ModelActionPojo saveNewLabel(@PathVariable String schemaId, @PathVariable String elementId, @Valid Label element, BindingResult bindingResult, Locale locale, HttpServletRequest request) {
		ModelActionPojo result = this.getActionResult(bindingResult, locale);
		if (result.isSuccess()) {
			elementService.createAndAppendElement(schemaId, elementId, element.getName(), authInfoHelper.getAuth(request));
		}
		return result;
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/async/saveNewNonterminal")
	public @ResponseBody ModelActionPojo saveNewNonterminal(@PathVariable String schemaId, @PathVariable String elementId, @Valid Nonterminal element, BindingResult bindingResult, Locale locale, HttpServletRequest request) {
		ModelActionPojo result = this.getActionResult(bindingResult, locale);
		if (result.isSuccess()) {
			element.setEntityId(schemaId);
			elementService.createAndAppendElement(schemaId, elementId, element.getName(), authInfoHelper.getAuth(request));
		}
		return result;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/async/saveNewGrammar")
	public @ResponseBody ModelActionPojo saveNewGrammar(@PathVariable String schemaId, @PathVariable String elementId, @Valid DescriptionGrammarImpl grammar, BindingResult bindingResult, Locale locale, HttpServletRequest request) {
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
	public @ResponseBody ModelActionPojo removeElement(@PathVariable String schemaId, @PathVariable String elementId, HttpServletRequest request) {
		elementService.removeElement(schemaId, elementId, authInfoHelper.getAuth(request));
		return new ModelActionPojo(true);
	}
	

}