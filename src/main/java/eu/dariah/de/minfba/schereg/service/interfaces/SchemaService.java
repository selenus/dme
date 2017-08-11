package eu.dariah.de.minfba.schereg.service.interfaces;

import java.util.List;
import java.util.Map;

import de.unibamberg.minf.dme.model.base.Element;
import de.unibamberg.minf.dme.model.datamodel.DatamodelImpl;
import de.unibamberg.minf.dme.model.datamodel.base.Datamodel;
import de.unibamberg.minf.dme.model.datamodel.base.DatamodelNature;
import de.unibamberg.minf.dme.model.tracking.ChangeSet;
import eu.dariah.de.dariahsp.model.web.AuthPojo;
import eu.dariah.de.minfba.schereg.model.RightsContainer;
import eu.dariah.de.minfba.schereg.pojo.AuthWrappedPojo;
import eu.dariah.de.minfba.schereg.serialization.Reference;
import eu.dariah.de.minfba.schereg.service.base.BaseEntityService;
import eu.dariah.de.minfba.schereg.service.base.BaseService;

public interface SchemaService extends BaseEntityService {
	public List<Datamodel> findAllSchemas();
	public Datamodel findSchemaById(String id);
	public void deleteSchemaById(String id, AuthPojo auth);
	
	//public <T extends SchemaNature> T convertSchema(T newSchema, Schema original);
	//public void upsertSchema(Query query, Update update);
	
	public Map<String, String> getAvailableTerminals(String schemaId);
	public void saveSchema(Datamodel schema, AuthPojo auth);
	public void saveSchema(AuthWrappedPojo<? extends Datamodel> schema, AuthPojo auth);
	
	public List<RightsContainer<Datamodel>> findAllByAuth(AuthPojo auth);
	public RightsContainer<Datamodel> findByIdAndAuth(String schemaId, AuthPojo auth);
	
	public List<ChangeSet> getChangeSetForAllSchemas();
	public void saveSchema(Datamodel schema, List<Reference> rootNonterminals, AuthPojo auth);
	
	public void setProcessingRoot(String schemaId, String elementId, AuthPojo auth);
	public DatamodelImpl cloneSchemaForSubtree(Datamodel s, Element subtree);	
}
