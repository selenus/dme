package de.dariah.base.dao.base;

import java.util.Collection;

import de.dariah.base.model.base.SchemaElement;

public interface PersistedSchemaElementDao<T extends SchemaElement> extends PersistedEntityDao<T> {
	public Collection<T> findBySchemaId(int schemaId);
}
