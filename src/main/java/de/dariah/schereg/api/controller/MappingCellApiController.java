package de.dariah.schereg.api.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import de.dariah.federation.model.MappingCellPojo;
import de.dariah.schereg.base.model.Mapping;
import de.dariah.schereg.base.model.MappingCell;
import de.dariah.schereg.base.model.ReadOnlySchemaElement;
import de.dariah.schereg.base.model.Schema;
import de.dariah.schereg.base.service.MappingCellService;
import de.dariah.schereg.base.service.MappingService;
import de.dariah.schereg.base.service.SchemaElementService;
import de.dariah.schereg.base.service.SchemaService;

@Controller
@RequestMapping("/api/mappingcell")
public class MappingCellApiController {

protected static final Logger logger = LoggerFactory.getLogger(MappingApiController.class);
	
	@Autowired
	private MappingService mappingService;
		
	@Autowired
	private SchemaElementService schemaElementService;
	
	@Autowired
	private SchemaService schemaService;
	
	@Autowired
	private MappingCellService mappingCellService;
	
	/**
	 * Returns a List of MappingCellPojos reflecting the probable transformation targets of the 
	 * 	specified source schema element
	 * 
	 * @param id	The id of the source schema element
	 * @param targetSchemaIds	The id's of the target schemas
	 * @return
	 */	
	@RequestMapping(value="/{id}", method=RequestMethod.GET/*, headers ="Content-Type=application/json"*/)
	public @ResponseBody List<MappingCellPojo> getMappedTargets(@PathVariable int id, @RequestParam(value="tUuid") String[] targetSchemaUuids) {
		
		ReadOnlySchemaElement se = schemaElementService.getReadOnlySchemaElement(id);
		List<MappingCellPojo> result = new ArrayList<MappingCellPojo>();
		
		List<Mapping> ms;
		List<MappingCell> mcs;
		MappingCellPojo mcp;
		Schema sourceSchema = schemaService.getSchema(se.getSchemaId());
		Schema targetSchema;
		
		
		for (String uuid : targetSchemaUuids) {
			try {
				targetSchema = schemaService.getSchemaByUuid(uuid);
				ms = mappingService.getMappingsBySchema(se.getSchemaId(), targetSchema.getId());
				
				Map<Integer, MappingCellPojo> innerResult = new HashMap<Integer, MappingCellPojo>();
				
				for (Mapping m : ms) {
					mcs = mappingCellService.getMappingCellsForInput(m.getId(), se.getId());
					
					if (mcs!=null) {
						for (MappingCell mc : mcs) {
							if (!innerResult.containsKey(mc.getOutput())) {
								mcp = new MappingCellPojo();
								mcp.setSourceSchemaUuid(sourceSchema.getUuid());
								mcp.setTargetSchemaUuid(targetSchema.getUuid());
								mcp.setSourceElementId(se.getId());
								mcp.setTargetElementId(mc.getOutput());
								mcp.setConfidence(mc.getScore());
								
								innerResult.put(mc.getOutput(), mcp);
							}
							
							if (innerResult != null && innerResult.values().size()>0) {
								result.addAll(innerResult.values());
							}
						}
					}
				}
			} catch (Exception e) {
				logger.error("Exception while loading mapping cells", e);
			}
		}
		return result;
	}
	
}
