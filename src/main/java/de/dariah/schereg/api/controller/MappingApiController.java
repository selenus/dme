package de.dariah.schereg.api.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import de.dariah.federation.model.MappingPojo;
import de.dariah.schereg.base.model.Mapping;
import de.dariah.schereg.base.model.Schema;
import de.dariah.schereg.base.service.MappingService;
import de.dariah.schereg.base.service.SchemaService;

@Controller
@RequestMapping("/api/mapping")
public class MappingApiController {
	
	@Autowired MappingService mappingService;
	@Autowired SchemaService schemaService;
	
	@RequestMapping(value={"/", ""}, method=RequestMethod.GET)
	public @ResponseBody List<MappingPojo> getAllMappingPojos() throws Exception {
		
		List<Schema> schemas = schemaService.getSchemas();
		MappingPojo mp;
		String targetSchemaUuid;
		
		if (schemas!=null && schemas.size()>0) {
			List<MappingPojo> result = new ArrayList<MappingPojo>();
			
			for (Schema schema : schemas) {
				mp = new MappingPojo();
				mp.setSourceSchemaUuid(schema.getUuid());
				mp.setTargetSchemaUuid(new ArrayList<String>());
				
				Collection<Mapping> mappings = mappingService.getMappingsBySchema(schema.getId());
				if (mappings != null) {
					for (Mapping mapping : mappings) {
						targetSchemaUuid = mapping.getTarget().getUuid();
						if (!targetSchemaUuid.equals(schema.getUuid())) {
							mp.getTargetSchemaUuid().add(targetSchemaUuid);
						}
					}
				}
				result.add(mp);
			}
			return result;
		}
		return null;
	}
}
