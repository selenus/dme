package de.dariah.schereg.api.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import de.dariah.schereg.base.model.Schema;
import de.dariah.schereg.base.service.MappingService;
import de.dariah.schereg.base.service.SchemaService;

@Controller
@RequestMapping("/api/schema")
public class SchemaApiController {
	
	public class SchemaPojo {
        private String name;
        private String description;
        private String uuid;
        
        private List<SchemaPojo> children;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public String getUuid() { return uuid; }
        public void setUuid(String uuid) { this.uuid = uuid; }
        public List<SchemaPojo> getChildren() {return children;}
        public void setChildren(List<SchemaPojo> children) {this.children = children;}
	}

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
			result[i] = toPojo(s.get(i));
		}

		return result;
	}
	
	
	@RequestMapping(value="/{uuid}", method=RequestMethod.GET/*, headers ="Content-Type=application/json"*/)
	public @ResponseBody SchemaPojo getSchema(@PathVariable String uuid) throws Exception {
		Schema s = schemaService.getSchemaByUuid(uuid);
		
		if (s == null) {
			return null;
		}
		return toPojo(s);
	}
	
	private SchemaPojo toPojo(Schema s) {
		SchemaPojo sp = new SchemaPojo();
		sp.setUuid(s.getUuid());
		sp.setName(s.getName());
		sp.setDescription(s.getDescription());
		
		return sp;
	}
}
