package eu.dariah.de.minfba.schereg.controller;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import eu.dariah.de.minfba.core.metamodel.BaseSchema;
import eu.dariah.de.minfba.core.metamodel.BaseTerminal;
import eu.dariah.de.minfba.core.metamodel.interfaces.Schema;
import eu.dariah.de.minfba.core.web.controller.BaseTranslationController;
import eu.dariah.de.minfba.core.web.controller.DataTableList;
import eu.dariah.de.minfba.core.web.pojo.ModelActionPojo;
import eu.dariah.de.minfba.schereg.service.ElementService;
import eu.dariah.de.minfba.schereg.service.SchemaService;

@Controller
@RequestMapping(value="/schema")
public class SchemaController extends BaseTranslationController {
	@Autowired private SchemaService schemaService;
	@Autowired private ElementService elementService;
	
	
	public SchemaController() {
		super("schema");
	}
	
	
	
	@RequestMapping(value = {"", "/"}, method = RequestMethod.GET)
	public String getList(Model model) {
		return "schema/home";
	}
	
	@RequestMapping(value = "/editor", method = RequestMethod.GET)
	public String getEditorRedirect(HttpServletResponse response) throws IOException {
		// Editor called without an id...
		response.sendRedirect("../");
		return null;
	}
	
	
	@RequestMapping(method=GET, value={"/forms/add"})
	public String getAddForm(Model model, Locale locale) {
		model.addAttribute("actionPath", "/schema/async/save");
		model.addAttribute("schema", new BaseSchema<BaseTerminal>());
		return "schema/form/edit";
	}
	
	@RequestMapping(method=GET, value={"/forms/edit/{id}"})
	public String getEditForm(@PathVariable String id, Model model, Locale locale) {
		model.addAttribute("actionPath", "/schema/async/save");
		model.addAttribute("schema", schemaService.findSchemaById(id));
		return "schema/form/edit";
	}
		
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/getData")
	public @ResponseBody DataTableList<Schema> getData(Model model, Locale locale) {
		List<Schema> schemas = schemaService.findAllSchemas();		
		return new DataTableList<Schema>(schemas);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/getData/{id}")
	public @ResponseBody Schema getSchema(@PathVariable String id, Model model, Locale locale) {
		return schemaService.findSchemaById(id);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/getElements/{id}")
	public @ResponseBody Schema getElements(@PathVariable String id, Model model, Locale locale) {
		// TODO: This is where we need the elements
		return schemaService.findSchemaById(id);
	}
		
	@RequestMapping(method=POST, value={"/async/save"}, produces = "application/json; charset=utf-8")
	public @ResponseBody ModelActionPojo saveSchema(@Valid BaseSchema<BaseTerminal> schema, BindingResult bindingResult) {
		ModelActionPojo result = new ModelActionPojo(true); //this.getActionResult(bindingResult, locale);
		if (schema.getId().isEmpty()) {
			schema.setId(null);
		}
		schemaService.saveSchema(schema);
		return result;
	}
		
	@RequestMapping(method=GET, value={"/async/delete/{id}"}, produces = "application/json; charset=utf-8")
	public @ResponseBody ModelActionPojo deleteSchema(@PathVariable String id) {
		ModelActionPojo result;
		if (id!=null && !id.isEmpty()) {
			schemaService.deleteSchemaById(id);
			result = new ModelActionPojo(true);
		} else {
			result = new ModelActionPojo(false);
		}		
		return result;
	}
}