package de.dariah.schereg.base.service;

import java.util.HashSet;

import de.dariah.base.model.base.ConfigurableSchemaElementImpl;
import de.dariah.base.model.base.SchemaElement;
import de.dariah.schereg.base.model.ReadOnlySchemaElement;
import de.dariah.schereg.util.SchemaElementContainer;

public interface SchemaElementService {
	public HashSet<SchemaElement> getAllSchemaElements(Integer schemaId);
	public SchemaElementContainer getSchemaElements(Integer schemaId);
	public ReadOnlySchemaElement getReadOnlySchemaElement(int output);
	public String getPath(int id);
	public <T> T getSchemaElement(int id, Class<?> elementType);
	public void saveOrUpdate(ConfigurableSchemaElementImpl se);
}
