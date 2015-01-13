package de.dariah.schereg.api.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import de.dariah.federation.model.SchemaElementPojo;
import de.dariah.schereg.base.model.Schema;
import de.dariah.schereg.base.service.SchemaElementService;
import de.dariah.schereg.base.service.SchemaService;
import de.dariah.schereg.util.json.SchemaElementConverter;

@Controller
@RequestMapping("/api/schema/{schemaUuid}/element")
public class SchemaElementApiController {
		
	@Autowired
	private SchemaService schemaService;
	
	@Autowired
	private SchemaElementService schemaElementService;
	
	
	@RequestMapping(value={"/", ""}, method=RequestMethod.GET/*, headers ="Content-Type=application/json"*/)
	public @ResponseBody List<SchemaElementPojo> getSchemaElements(@PathVariable String schemaUuid) throws Exception {
		Schema s = schemaService.getSchemaByUuid(schemaUuid);
		SchemaElementConverter converter = new SchemaElementConverter(schemaElementService.getSchemaElements(s.getId()).getClassLookupTable());
		
		return converter.getTree();
		
		/*// This is just a temporary hack for the missing root elements in the SR 
		SchemaElementPojo rootElement = new SchemaElementPojo();
		rootElement.setChildren(converter.getTree());
		rootElement.setName(s.getName());
		
		List<SchemaElementPojo> result = new ArrayList<SchemaElementPojo>();
		result.add(rootElement);
		
		return result;*/
	}
}
