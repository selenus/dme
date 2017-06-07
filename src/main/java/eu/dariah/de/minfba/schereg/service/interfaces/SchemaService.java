package eu.dariah.de.minfba.schereg.service.interfaces;

import java.util.List;
import java.util.Map;

import eu.dariah.de.dariahsp.model.web.AuthPojo;
import eu.dariah.de.minfba.core.metamodel.interfaces.SchemaNature;
import eu.dariah.de.minfba.core.metamodel.tracking.ChangeSet;
import eu.dariah.de.minfba.schereg.model.RightsContainer;
import eu.dariah.de.minfba.schereg.pojo.AuthWrappedPojo;
import eu.dariah.de.minfba.schereg.serialization.Reference;
import eu.dariah.de.minfba.schereg.service.base.BaseEntityService;
import eu.dariah.de.minfba.schereg.service.base.BaseService;

public interface SchemaService extends BaseEntityService {
	public List<SchemaNature> findAllSchemas();
	public SchemaNature findSchemaById(String id);
	public void deleteSchemaById(String id, AuthPojo auth);
	
	public <T extends SchemaNature> T convertSchema(T newSchema, SchemaNature original);
	//public void upsertSchema(Query query, Update update);
	
	public Map<String, String> getAvailableTerminals(String schemaId);
	public void saveSchema(SchemaNature schema, AuthPojo auth);
	public void saveSchema(AuthWrappedPojo<? extends SchemaNature> schema, AuthPojo auth);
	
	public List<RightsContainer<SchemaNature>> findAllByAuth(AuthPojo auth);
	public RightsContainer<SchemaNature> findByIdAndAuth(String schemaId, AuthPojo auth);
	
	public List<ChangeSet> getChangeSetForAllSchemas();
	public void saveSchema(SchemaNature schema, List<Reference> rootNonterminals, AuthPojo auth);

	
}
