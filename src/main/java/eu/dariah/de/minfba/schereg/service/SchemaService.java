package eu.dariah.de.minfba.schereg.service;

import java.util.List;

import eu.dariah.de.minfba.core.metamodel.interfaces.Schema;
import eu.dariah.de.minfba.core.metamodel.xml.XmlSchema;

public interface SchemaService {
	public List<Schema> findAllSchemata();
	public void saveSchema(Schema s);
	public void deleteSchema(Schema sDel);
}
