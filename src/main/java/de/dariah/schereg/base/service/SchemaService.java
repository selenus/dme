package de.dariah.schereg.base.service;

import java.util.Collection;
import java.util.List;

import org.joda.time.DateTime;

import de.dariah.base.model.base.SchemaElement;
import de.dariah.schereg.base.model.Domain;
import de.dariah.schereg.base.model.Schema;

public interface SchemaService {
	//public Schema importAndSaveSchema(Schema schema);
	public void saveSchema(Schema schema);
	public List<Schema> listSchemas();
	public void removeSchema(Integer id);
	public Schema getSchema(Integer id);
	public List<Schema> getSchemas();
	public Schema getSchema(Integer id, boolean fullyInitialized);
	public List<String> getSupportedSchemaTypes();
	
	public Collection<Schema> getSchemasCreatedAfter(DateTime created);
	public long getSchemaCount();
	public DateTime getLastModified();
	
	//public void importAsync(Schema schema, byte[] bytes);
	
	public List<Domain> getGlobalDomains();
	public void saveImportedSchema(Schema schema, Collection<SchemaElement> schemaElements) throws Exception;
	public List<Schema> findByName(String name, boolean caseInsensitive);
	public int getFileBySchema(int schemaId);
	public Schema getSchemaByUuid(String uuid) throws Exception;
}
