package de.dariah.schereg.api.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import de.dariah.federation.model.SchemaPojo;
import de.dariah.schereg.base.model.Schema;
import de.dariah.schereg.base.service.MappingService;
import de.dariah.schereg.base.service.SchemaService;

@Controller
@RequestMapping("/api/detailed/schema")
public class DetailedSchemaApiController {

	@Autowired
	private MappingService mappingService;
	
	@Autowired
	private SchemaService schemaService;

	@RequestMapping(value={"/", ""}, method=RequestMethod.GET/*, headers ="Content-Type=application/json"*/)
	public @ResponseBody SchemaPojo[] getSchemas() {
		List<Schema> s = schemaService.getSchemas();
		
		if (s == null || s.size()==0) {
			return null;
		}
		
		SchemaPojo[] result = new SchemaPojo[s.size()];
		for (int i=0; i<s.size(); i++) {
			result[i] = s.get(i).toPojo();
		}

		return result;
	}
	
	
	@RequestMapping(value="/{uuid}", method=RequestMethod.GET/*, headers ="Content-Type=application/json"*/)
	public @ResponseBody SchemaPojo getSchema(@PathVariable String uuid) throws Exception {
		Schema s = schemaService.getSchemaByUuid(uuid);
		
		if (s == null) {
			return null;
		}
		return s.toPojo();
	}
}
