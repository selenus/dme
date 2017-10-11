package de.unibamberg.minf.dme.service.interfaces;

import java.util.List;
import java.util.Map;

import de.unibamberg.minf.dme.model.RightsContainer;
import de.unibamberg.minf.dme.model.base.Element;
import de.unibamberg.minf.dme.model.datamodel.DatamodelImpl;
import de.unibamberg.minf.dme.model.datamodel.base.Datamodel;
import de.unibamberg.minf.dme.model.datamodel.base.DatamodelNature;
import de.unibamberg.minf.dme.model.datamodel.natures.XmlDatamodelNature;
import de.unibamberg.minf.dme.model.tracking.ChangeSet;
import de.unibamberg.minf.dme.pojo.AuthWrappedPojo;
import de.unibamberg.minf.dme.serialization.Reference;
import de.unibamberg.minf.dme.service.base.BaseEntityService;
import eu.dariah.de.dariahsp.model.web.AuthPojo;

public interface SchemaService extends BaseEntityService {
	public List<Datamodel> findAllSchemas();
	public Datamodel findSchemaById(String id);
	public void deleteSchemaById(String id, AuthPojo auth);
	
	public Map<String, String> getAvailableTerminals(String schemaId);
	public void saveSchema(Datamodel schema, AuthPojo auth);
	public void saveSchema(AuthWrappedPojo<? extends Datamodel> schema, AuthPojo auth);
	
	public List<RightsContainer<Datamodel>> findAllByAuth(AuthPojo auth);
	public RightsContainer<Datamodel> findByIdAndAuth(String schemaId, AuthPojo auth);
	
	public List<ChangeSet> getChangeSetForAllSchemas();
	public void saveSchema(Datamodel schema, List<Reference> rootNonterminals, AuthPojo auth);
	
	public void setProcessingRoot(String schemaId, String elementId, AuthPojo auth);
	public DatamodelImpl cloneSchemaForSubtree(Datamodel s, Element subtree);
	
	public boolean changeId(String currentId, String id);
	
	public void removeNature(String entityId, String natureClass, AuthPojo auth);
	public void addNature(String entityId, String natureClass, AuthPojo auth);
	
	public List<Class<? extends DatamodelNature>> getMissingNatures(String entityId);
	public void updateNature(String entityId, XmlDatamodelNature xmlNature, AuthPojo auth);	
}
