package eu.dariah.de.minfba.schereg.controller;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.dariah.de.minfba.core.metamodel.Nonterminal;
import eu.dariah.de.minfba.core.metamodel.interfaces.Element;
import eu.dariah.de.minfba.core.metamodel.interfaces.Schema;
import eu.dariah.de.minfba.core.metamodel.interfaces.Terminal;
import eu.dariah.de.minfba.core.metamodel.xml.XmlSchema;
import eu.dariah.de.minfba.core.metamodel.xml.XmlTerminal;
import eu.dariah.de.minfba.core.web.controller.BaseTranslationController;
import eu.dariah.de.minfba.core.web.pojo.ModelActionPojo;
import eu.dariah.de.minfba.schereg.service.ElementService;
import eu.dariah.de.minfba.schereg.service.SchemaService;

@Controller
@RequestMapping(value="/schema/editor/{schemaId}/element/{elementId}")
public class SchemaEditorElementController extends BaseTranslationController {
	@Autowired private ElementService elementService;
	@Autowired private SchemaService schemaService;
	
	public SchemaEditorElementController() {
		super("schemaEditor");
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/form/nonterminal")
	public String getElement(@PathVariable String schemaId, @PathVariable String elementId, Model model) {
		model.addAttribute("element", elementService.findById(elementId));
		model.addAttribute("actionPath", "/schema/editor/" + schemaId + "/element/" + elementId + "/async/saveNonterminal");
		return "schemaEditor/form/element/edit_nonterminal";
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/createSubelement")
	public @ResponseBody Element createSubelement(@PathVariable String schemaId, @PathVariable String elementId) {
		return elementService.createAndAppendElement(schemaId, elementId);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/get")
	public @ResponseBody Element getElement(@PathVariable String schemaId, @PathVariable String elementId) {
		return elementService.findById(elementId);
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/async/saveNonterminal")
	public @ResponseBody ModelActionPojo saveNonterminal(@Valid Nonterminal element, BindingResult bindingResult) {
		ModelActionPojo result = new ModelActionPojo(true); //this.getActionResult(bindingResult, locale);
		if (element.getId().isEmpty()) {
			element.setId(null);
		}
		elementService.saveElement(element);
		return result;
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
			if (terminalId==null) {
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