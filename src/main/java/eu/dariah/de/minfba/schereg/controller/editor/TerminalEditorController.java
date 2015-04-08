package eu.dariah.de.minfba.schereg.controller.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.validation.Valid;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.dariah.de.minfba.core.metamodel.interfaces.Schema;
import eu.dariah.de.minfba.core.metamodel.interfaces.Terminal;
import eu.dariah.de.minfba.core.metamodel.xml.XmlNamespace;
import eu.dariah.de.minfba.core.metamodel.xml.XmlSchema;
import eu.dariah.de.minfba.core.metamodel.xml.XmlTerminal;
import eu.dariah.de.minfba.core.web.controller.BaseTranslationController;
import eu.dariah.de.minfba.core.web.pojo.ModelActionPojo;
import eu.dariah.de.minfba.schereg.service.ElementService;
import eu.dariah.de.minfba.schereg.service.SchemaService;

@Controller
@RequestMapping(value="/schema/editor/{schemaId}/terminal/{terminalId}")
public class TerminalEditorController extends BaseTranslationController {
	@Autowired private ElementService elementService;
	@Autowired private SchemaService schemaService;
	
	public TerminalEditorController() {
		super("schemaEditor");
	}
	
	
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/remove")
	public @ResponseBody Terminal removeElement(@PathVariable String schemaId, @PathVariable String terminalId) {
		return elementService.removeTerminal(schemaId, terminalId);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/form/edit")
	public String getElement(@PathVariable String schemaId, @PathVariable String terminalId, Model model, Locale locale) {
		Schema s = schemaService.findSchemaById(schemaId);
		XmlTerminal terminal = null;
		if (terminalId != "-1") {
			if (s.getTerminals()!=null) {
				for (Terminal t : s.getTerminals()) {
					if (t.getId().equals(terminalId)) {
						terminal = (XmlTerminal)t;
						break;
					}
				}
			}
		} 
		if (terminal==null) {
			terminal = new XmlTerminal();
			terminal.setId("-1");
		}
		model.addAttribute("terminal", terminal);
		
		
		List<String> availableNamespaces = new ArrayList<String>();
		if (s instanceof XmlSchema) {	
			if (((XmlSchema)s).getNamespaces()!=null) {
				for (XmlNamespace ns : ((XmlSchema)s).getNamespaces()) {
					availableNamespaces.add(ns.getUrl());
				}
			}
		}
		model.addAttribute("availableNamespaces", availableNamespaces);
		
		model.addAttribute("actionPath", "/schema/editor/" + schemaId + "/terminal/" + terminal.getId() + "/async/saveTerminal");
		return "schemaEditor/form/element/edit_terminal";
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/async/saveTerminal")
	public @ResponseBody ModelActionPojo saveTerminal(@PathVariable String schemaId, @Valid XmlTerminal element, BindingResult bindingResult, Locale locale) {
		ModelActionPojo result = this.getActionResult(bindingResult, locale);
		if (result.isSuccess()) {	
			if (element.getId().isEmpty() || element.getId().trim().equals("-1")) {
				element.setId(null);
			}
			
			XmlSchema s = (XmlSchema)schemaService.findSchemaById(schemaId);
			if (s.getTerminals()!=null) {
				for (int i=0; i<s.getTerminals().size(); i++) {
					XmlTerminal t = s.getTerminals().get(i);
					if (t.getName().equals(element.getName()) && t.getNamespace().equals(element.getNamespace()) &&
							!t.getId().equals(element.getId())) {
						result.setSuccess(false);
						result.setObjectErrors(new ArrayList<String>());
						result.getObjectErrors().add("~Duplicate");
						return result;
					}
				}
			}
			if (s.getTerminals()==null) {
				s.setTerminals(new ArrayList<XmlTerminal>());
			}
			if (element.getId()==null) {
				element.setId(new ObjectId().toString());
				s.getTerminals().add(element);
			} else {
				for (int i=0; i<s.getTerminals().size(); i++) {
					if (s.getTerminals().get(i).getId().equals(element.getId())) {
						s.getTerminals().set(i, element);
						break;
					}
				}
			}
			
			schemaService.saveSchema(s);			
		}
		return result;
	}
}