package eu.dariah.de.minfba.schereg.service;

import java.util.List;

import eu.dariah.de.minfba.core.metamodel.interfaces.Schema;

public interface SchemaService {
	public List<Schema> findAllSchemas();
	public void saveSchema(Schema schema);
	public Schema findSchemaById(String id);
	public void deleteSchemaById(String id);
}
