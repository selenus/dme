package eu.dariah.de.minfba.schereg.service;

import java.util.List;

import eu.dariah.de.minfba.core.metamodel.BaseSchema;
import eu.dariah.de.minfba.core.metamodel.BaseTerminal;
import eu.dariah.de.minfba.core.metamodel.interfaces.Schema;

public interface SchemaService {
	public List<Schema> findAllSchemas();
	public void saveSchema(Schema schema);
}
