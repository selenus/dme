package eu.dariah.de.minfba.schereg.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.dariah.de.minfba.core.metamodel.interfaces.Element;
import eu.dariah.de.minfba.core.web.controller.BaseTranslationController;
import eu.dariah.de.minfba.schereg.service.ElementService;

@Controller
@RequestMapping(value="/schema/editor/{schemaId}/element/{elementId}")
public class SchemaEditorElementController extends BaseTranslationController {
	@Autowired private ElementService elementService;
	
	public SchemaEditorElementController() {
		super("schemaEditor");
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/createSubelement")
	public @ResponseBody Element createSubelement(@PathVariable String schemaId, @PathVariable String elementId) {
		return elementService.createAndAppendElement(schemaId, elementId);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/remove")
	public @ResponseBody Element removeElement(@PathVariable String schemaId, @PathVariable String elementId) {
		return elementService.removeElement(schemaId, elementId);
	}
}