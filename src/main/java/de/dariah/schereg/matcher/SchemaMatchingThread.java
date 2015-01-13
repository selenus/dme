package de.dariah.schereg.matcher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import de.dariah.schereg.base.model.Mapping;
import de.dariah.schereg.base.model.Schema;
import de.dariah.schereg.base.service.MappingService;
import de.dariah.schereg.base.service.SchemaService;

@Component
@Scope(value="prototype")
public class SchemaMatchingThread extends Thread {

	@Autowired private MappingService mappingSvc;
	@Autowired private SchemaService schemaSvc;
	private Mapping mapping;

	public Mapping getMapping() { return mapping; }
	public void setMapping(Mapping mapping) { this.mapping = mapping; }

	@Override
	public void run() {
		
		Schema source = schemaSvc.getSchema(mapping.getSourceId());
		Schema target = schemaSvc.getSchema(mapping.getTargetId());
		
		executeMatching(source, target);
	}

	private void executeMatching(Schema source, Schema target) {
	
		
		
	}
}
