package eu.dariah.de.minfba.schereg.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import de.dariah.samlsp.model.pojo.AuthPojo;
import eu.dariah.de.minfba.core.metamodel.Nonterminal;
import eu.dariah.de.minfba.core.metamodel.interfaces.Schema;
import eu.dariah.de.minfba.core.metamodel.tracking.ChangeSet;
import eu.dariah.de.minfba.core.metamodel.xml.XmlSchema;
import eu.dariah.de.minfba.core.metamodel.xml.XmlTerminal;
import eu.dariah.de.minfba.schereg.dao.base.BaseDaoImpl;
import eu.dariah.de.minfba.schereg.dao.interfaces.SchemaDao;
import eu.dariah.de.minfba.schereg.model.RightsContainer;
import eu.dariah.de.minfba.schereg.pojo.AuthWrappedPojo;
import eu.dariah.de.minfba.schereg.serialization.Reference;
import eu.dariah.de.minfba.schereg.service.base.BaseReferenceServiceImpl;
import eu.dariah.de.minfba.schereg.service.interfaces.ElementService;
import eu.dariah.de.minfba.schereg.service.interfaces.SchemaService;

@Service
public class SchemaServiceImpl extends BaseReferenceServiceImpl implements SchemaService {
	@Autowired private ElementService elementService;
	@Autowired private SchemaDao schemaDao;
	
	@Override
	public List<Schema> findAllSchemas() {
		return schemaDao.findAllEnclosed();
	}

	@Override
	public void saveSchema(AuthWrappedPojo<? extends Schema> schema, AuthPojo auth) {		
		this.innerSaveSchema(schema.getPojo(), schema.isDraft(), schema.isReadOnly(), auth.getUserId(), auth.getSessionId());
	}
	
	@Override
	public void saveSchema(Schema schema, AuthPojo auth) {
		this.innerSaveSchema(schema, null, null, auth.getUserId(), auth.getSessionId());
	}
	
	@Override
	public void saveSchema(Schema schema, List<Reference> rootNonterminals, AuthPojo auth) {
		this.innerSaveSchema(schema, null, null, auth.getUserId(), auth.getSessionId());
		
		Reference root = this.findReferenceById(schema.getId());
	
		if (root.getChildReferences()==null) {
			root.setChildReferences(new HashMap<String, Reference[]>());
		}
		Reference[] childArray = new Reference[rootNonterminals.size()];
		for (int i=0; i<rootNonterminals.size(); i++) {
			childArray[i] = rootNonterminals.get(i);
		}		
		root.getChildReferences().put(Nonterminal.class.getName(), childArray);
		this.saveRootReference(root);
	}
	
	private void innerSaveSchema(Schema schema, Boolean draft, Boolean readOnly, String userId, String sessionId) {
		RightsContainer<Schema> container = null;
		boolean isNew = schema.getId()==null || schema.getId().equals("") || schema.getId().equals("undefined"); 
		if (isNew) {
			container = createContainer(userId);
		} else {
			container = schemaDao.findById(schema.getId());
		}
		container.setElement(schema);
		if (draft!=null) {
			container.setDraft(draft);
		}
		if (readOnly!=null) {
			container.setReadOnly(readOnly);
		}
		schemaDao.save(container, userId, sessionId);
		if (isNew) {
			this.saveRootReference(new Reference(container.getId()));
		}
	}
	
	private RightsContainer<Schema> createContainer(String userId) {
		RightsContainer<Schema> container = new RightsContainer<Schema>();
		container.setOwnerId(userId);
		container.setId(new ObjectId().toString());
		container.setDraft(true);
		return container;
	}

	@Override
	public Schema findSchemaById(String id) {
		return schemaDao.findEnclosedById(id);
	}

	@Override
	public void deleteSchemaById(String id, AuthPojo auth) {
		RightsContainer<Schema> s = schemaDao.findById(id);
		if (s != null) {
			if (this.getHasWriteAccess(s, auth.getUserId())) {
				Reference r = this.findReferenceById(s.getId());
				if (r.getChildReferences()!=null && r.getChildReferences().size()>0) {
					String rootNonterminalId = r.getChildReferences().get(Nonterminal.class.getName())[0].getId();
					if (BaseDaoImpl.isValidObjectId(rootNonterminalId)) {
						elementService.removeElement(s.getId(), rootNonterminalId, auth);
					}
				}
				schemaDao.delete(s, auth.getUserId(), auth.getSessionId());
			}
		}
	}
	
	/*@Override
	public void upsertSchema(Query query, Update update) {
		schemaDao.upsert(query, update);
	}*/

	@Override
	public <T extends Schema> T convertSchema(T newSchema, Schema original) {
		newSchema.setId(original.getId());
		newSchema.setLabel(original.getLabel());
		newSchema.setDescription(original.getDescription());
		return newSchema;
	}
	
	@Override
	public Map<String, String> getAvailableTerminals(String schemaId) {
		Map<String,String> availableTerminals = new HashMap<String,String>();
		Schema s = this.findSchemaById(schemaId);
		if (s instanceof XmlSchema) {	
			if (((XmlSchema)s).getTerminals()!=null) {
				for (XmlTerminal t : ((XmlSchema)s).getTerminals()) {
					availableTerminals.put(t.getId(), t.getName() + " (" + t.getNamespace() + ")");
				}
			}
		}
		return availableTerminals;
	}

	@Override
	public List<RightsContainer<Schema>> findAllByAuth(AuthPojo auth) {
		return schemaDao.findAllByUserId(auth.getUserId());
	}

	@Override
	public RightsContainer<Schema> findByIdAndAuth(String schemaId, AuthPojo auth) {
		return schemaDao.findByIdAndUserId(schemaId, auth.getUserId());
	}

	@Override
	public boolean getHasWriteAccess(String id, String userId) {
		/* User is logged in (has an ID) and creates a new schema (no ID) */
		if ( (id==null || id.isEmpty()) && (userId!=null && !userId.isEmpty()) ) {
			return true;
		}
		RightsContainer<Schema> s = schemaDao.findByIdAndUserId(id, userId, true);
		return this.getHasWriteAccess(s, userId);
	}
	
	@Override
	public boolean getHasWriteAccess(RightsContainer<Schema> s, String userId) {
		if (s!=null && ( 
				s.getOwnerId().equals(userId) || 
				( s.getWriteIds()==null || ( s.getWriteIds()!=null && s.getWriteIds().contains(userId)) ) ) 
			) {
			return true;
		}
		return false;
	}
		
	@Override
	public boolean getHasShareAccess(String id, String userId) {
		RightsContainer<Schema> s = schemaDao.findByIdAndUserId(id, userId, true);
		return this.getHasShareAccess(s, userId);
	}
	
	@Override
	public boolean getHasShareAccess(RightsContainer<Schema> s, String userId) {
		if (s!=null && ( 
				s.getOwnerId().equals(userId) || 
				(s.getShareIds()==null || ( s.getShareIds()!=null && s.getShareIds().contains(userId)) ) )) {
			return true;
		}
		return false;
	}

	@Override
	public List<ChangeSet> getChangeSetForAllSchemas() {
		Query q = new Query();
		q.fields().include("_id");
		List<RightsContainer<Schema>> schemas = schemaDao.find(q);
		List<String> ids = new ArrayList<String>();
		if (schemas!=null) {
			for (RightsContainer<Schema> s : schemas) {
				ids.add(s.getId());
			}
		}
	
		return super.getChangeSetForElements(ids);
	}
}
