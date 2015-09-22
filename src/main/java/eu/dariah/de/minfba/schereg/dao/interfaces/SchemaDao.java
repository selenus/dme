package eu.dariah.de.minfba.schereg.dao.interfaces;

import java.util.List;

import eu.dariah.de.minfba.core.metamodel.interfaces.Schema;
import eu.dariah.de.minfba.core.metamodel.xml.XmlNamespace;
import eu.dariah.de.minfba.schereg.dao.base.TrackedEntityDao;
import eu.dariah.de.minfba.schereg.exception.GenericScheregException;
import eu.dariah.de.minfba.schereg.model.RightsContainer;

public interface SchemaDao extends TrackedEntityDao<RightsContainer<Schema>> {
	public XmlNamespace findNamespaceByPrefix(String string);
	//public void updateNamespaceByPrefix(Schema s, String string, String string2);
	
	/* Direct access to Schema for convenience -> no editing, no drafts */
	public List<Schema> findAllSchemas();
	public Schema findSchemaById(String id);
	public void updateContained(Schema s, String userId, String sessionId) throws GenericScheregException;

	public List<RightsContainer<Schema>> findAllByUserId(String userId);
	public RightsContainer<Schema> findByIdAndUserId(String schemaId, String userId);
	public RightsContainer<Schema> findByIdAndUserId(String schemaId, String userId, boolean excludePojo);
	
}