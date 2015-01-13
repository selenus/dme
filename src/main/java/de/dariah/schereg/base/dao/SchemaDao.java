package de.dariah.schereg.base.dao;

import java.util.Collection;
import java.util.List;

import de.dariah.base.dao.base.PersistedEntityDao;
import de.dariah.base.model.base.SchemaElement;
import de.dariah.schereg.base.model.Schema;

public interface SchemaDao extends PersistedEntityDao<Schema> {
	public Schema saveImportedSchema(Schema schema, Collection<SchemaElement> schemaElements);
	public Schema findById(int id, boolean fullyInitialized);
	public List<Schema> findByName(String name, boolean caseInsensitive);
	public int getFileBySchema(int schemaId);
}
