package eu.dariah.de.minfba.schereg.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import eu.dariah.de.minfba.schereg.service.SchemaService;

@Controller
@RequestMapping(value="/schema")
public class SchemaController {
	
	@Autowired private SchemaService schemaService;
	
	@RequestMapping(value = {"", "/"}, method = RequestMethod.GET)
	public String getHome(Model model) {
		return "schema/home";
	}
}
