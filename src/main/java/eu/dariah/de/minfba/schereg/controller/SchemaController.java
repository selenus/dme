package eu.dariah.de.minfba.schereg.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import eu.dariah.de.minfba.schereg.dao.SchemaDao;
import eu.dariah.de.minfba.schereg.dao.SchemaDaoImpl;

@Controller
@RequestMapping(value="/schema")
public class SchemaController {
	
	@Autowired private SchemaDao schemaDao;
	
	@RequestMapping(value = {"", "/"}, method = RequestMethod.GET)
	public String getHome(Model model) {
		schemaDao.loadAllSchemas();
		
		return "schema/home";
	}
}
