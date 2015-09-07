package eu.dariah.de.minfba.schereg.service.interfaces;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import eu.dariah.de.minfba.core.metamodel.interfaces.Schema;
import eu.dariah.de.minfba.core.metamodel.xml.XmlSchema;
import eu.dariah.de.minfba.core.metamodel.xml.XmlTerminal;

public interface SchemaService {
	public List<Schema> findAllSchemas();
	public void saveSchema(Schema schema);
	public Schema findSchemaById(String id);
	public void deleteSchemaById(String id);
	
	public <T extends Schema> T convertSchema(T newSchema, Schema original);
	public void upsertSchema(Query query, Update update);
	
	public Map<String, String> getAvailableTerminals(String schemaId);
}
