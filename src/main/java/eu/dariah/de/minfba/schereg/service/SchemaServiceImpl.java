package eu.dariah.de.minfba.schereg.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import eu.dariah.de.dariahsp.model.web.AuthPojo;
import eu.dariah.de.minfba.core.metamodel.interfaces.Nonterminal;
import eu.dariah.de.minfba.core.metamodel.interfaces.SchemaNature;
import eu.dariah.de.minfba.core.metamodel.tracking.ChangeSet;
import eu.dariah.de.minfba.core.metamodel.xml.XmlSchemaNature;
import eu.dariah.de.minfba.core.metamodel.xml.XmlTerminal;
import eu.dariah.de.minfba.schereg.dao.base.BaseDaoImpl;
import eu.dariah.de.minfba.schereg.dao.interfaces.SchemaDao;
import eu.dariah.de.minfba.schereg.model.RightsContainer;
import eu.dariah.de.minfba.schereg.pojo.AuthWrappedPojo;
import eu.dariah.de.minfba.schereg.serialization.Reference;
import eu.dariah.de.minfba.schereg.service.base.BaseEntityServiceImpl;
import eu.dariah.de.minfba.schereg.service.base.BaseReferenceServiceImpl;
import eu.dariah.de.minfba.schereg.service.interfaces.ElementService;
import eu.dariah.de.minfba.schereg.service.interfaces.SchemaService;

@Service
public class SchemaServiceImpl extends BaseEntityServiceImpl implements SchemaService {
	@Autowired private ElementService elementService;
	
	@Override
	public List<SchemaNature> findAllSchemas() {
		return schemaDao.findAllEnclosed();
	}

	@Override
	public void saveSchema(AuthWrappedPojo<? extends SchemaNature> schema, AuthPojo auth) {		
		this.innerSaveSchema(schema.getPojo(), schema.isDraft(), schema.isReadOnly(), auth.getUserId(), auth.getSessionId());
	}
	
	@Override
	public void saveSchema(SchemaNature schema, AuthPojo auth) {
		this.innerSaveSchema(schema, null, null, auth.getUserId(), auth.getSessionId());
	}
	
	@Override
	public void saveSchema(SchemaNature schema, List<Reference> rootNonterminals, AuthPojo auth) {
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
	
	private void innerSaveSchema(SchemaNature schema, Boolean draft, Boolean readOnly, String userId, String sessionId) {
		RightsContainer<SchemaNature> container = null;
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
	
	private RightsContainer<SchemaNature> createContainer(String userId) {
		RightsContainer<SchemaNature> container = new RightsContainer<SchemaNature>();
		container.setOwnerId(userId);
		container.setId(new ObjectId().toString());
		container.setDraft(true);
		return container;
	}

	@Override
	public SchemaNature findSchemaById(String id) {
		return schemaDao.findEnclosedById(id);
	}

	@Override
	public void deleteSchemaById(String id, AuthPojo auth) {
		RightsContainer<SchemaNature> s = schemaDao.findById(id);
		if (s != null) {
			if (this.getUserCanWriteEntity(s, auth.getUserId())) {
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
	public <T extends SchemaNature> T convertSchema(T newSchema, SchemaNature original) {
		newSchema.setId(original.getId());
		newSchema.setLabel(original.getLabel());
		newSchema.setDescription(original.getDescription());
		return newSchema;
	}
	
	@Override
	public Map<String, String> getAvailableTerminals(String schemaId) {
		Map<String,String> availableTerminals = new HashMap<String,String>();
		SchemaNature s = this.findSchemaById(schemaId);
		if (s instanceof XmlSchemaNature) {	
			if (((XmlSchemaNature)s).getTerminals()!=null) {
				for (XmlTerminal t : ((XmlSchemaNature)s).getTerminals()) {
					availableTerminals.put(t.getId(), t.getName() + " (" + t.getNamespace() + ")");
				}
			}
		}
		return availableTerminals;
	}

	@Override
	public List<RightsContainer<SchemaNature>> findAllByAuth(AuthPojo auth) {
		return schemaDao.findAllByUserId(auth.getUserId());
	}

	@Override
	public RightsContainer<SchemaNature> findByIdAndAuth(String schemaId, AuthPojo auth) {
		return schemaDao.findByIdAndUserId(schemaId, auth.getUserId());
	}
	
	@Override
	public List<ChangeSet> getChangeSetForAllSchemas() {
		Query q = new Query();
		q.fields().include("_id");
		List<RightsContainer<SchemaNature>> schemas = schemaDao.find(q);
		List<String> ids = new ArrayList<String>();
		if (schemas!=null) {
			for (RightsContainer<SchemaNature> s : schemas) {
				ids.add(s.getId());
			}
		}
	
		return super.getChangeSetForElements(ids);
	}
}
