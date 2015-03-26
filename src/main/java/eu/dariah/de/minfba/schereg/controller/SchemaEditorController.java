package eu.dariah.de.minfba.schereg.controller;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.dariah.de.minfba.core.metamodel.interfaces.Element;
import eu.dariah.de.minfba.core.web.controller.BaseTranslationController;
import eu.dariah.de.minfba.schereg.service.ElementService;
import eu.dariah.de.minfba.schereg.service.SchemaService;

@Controller
@RequestMapping(value="/schema/editor/{schemaId}")
public class SchemaEditorController extends BaseTranslationController {
	@Autowired private SchemaService schemaService;
	@Autowired private ElementService elementService;
	
	public SchemaEditorController() {
		super("schemaEditor");
	}
	
	@RequestMapping(method=GET, value={"/", ""})
	public String getEditor(@PathVariable String schemaId, Model model, Locale locale) {
		model.addAttribute("schema", schemaService.findSchemaById(schemaId));
		return "schemaEditor";
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/getHierarchy")
	public @ResponseBody Element getHierarchy(@PathVariable String schemaId, Model model, Locale locale) {
		return elementService.findRootBySchemaId(schemaId);
	}
}
