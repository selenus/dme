package eu.dariah.de.minfba.schereg.service.interfaces;

import java.util.List;
import java.util.Map;

import eu.dariah.de.dariahsp.model.web.AuthPojo;
import eu.dariah.de.minfba.core.metamodel.SchemaImpl;
import eu.dariah.de.minfba.core.metamodel.interfaces.Element;
import eu.dariah.de.minfba.core.metamodel.interfaces.Schema;
import eu.dariah.de.minfba.core.metamodel.interfaces.SchemaNature;
import eu.dariah.de.minfba.core.metamodel.tracking.ChangeSet;
import eu.dariah.de.minfba.schereg.model.RightsContainer;
import eu.dariah.de.minfba.schereg.pojo.AuthWrappedPojo;
import eu.dariah.de.minfba.schereg.serialization.Reference;
import eu.dariah.de.minfba.schereg.service.base.BaseEntityService;
import eu.dariah.de.minfba.schereg.service.base.BaseService;

public interface SchemaService extends BaseEntityService {
	public List<Schema> findAllSchemas();
	public Schema findSchemaById(String id);
	public void deleteSchemaById(String id, AuthPojo auth);
	
	//public <T extends SchemaNature> T convertSchema(T newSchema, Schema original);
	//public void upsertSchema(Query query, Update update);
	
	public Map<String, String> getAvailableTerminals(String schemaId);
	public void saveSchema(Schema schema, AuthPojo auth);
	public void saveSchema(AuthWrappedPojo<? extends Schema> schema, AuthPojo auth);
	
	public List<RightsContainer<Schema>> findAllByAuth(AuthPojo auth);
	public RightsContainer<Schema> findByIdAndAuth(String schemaId, AuthPojo auth);
	
	public List<ChangeSet> getChangeSetForAllSchemas();
	public void saveSchema(Schema schema, List<Reference> rootNonterminals, AuthPojo auth);
	
	public void setProcessingRoot(String schemaId, String elementId, AuthPojo auth);
	public SchemaImpl cloneSchemaForSubtree(Schema s, Element subtree);	
}
