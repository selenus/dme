package eu.dariah.de.minfba.schereg.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import de.unibamberg.minf.dme.model.base.Element;
import de.unibamberg.minf.dme.model.base.Nonterminal;
import de.unibamberg.minf.dme.model.base.Terminal;
import de.unibamberg.minf.dme.model.datamodel.DatamodelImpl;
import de.unibamberg.minf.dme.model.datamodel.NonterminalImpl;
import de.unibamberg.minf.dme.model.datamodel.base.Datamodel;
import de.unibamberg.minf.dme.model.datamodel.base.DatamodelNature;
import de.unibamberg.minf.dme.model.datamodel.natures.XmlDatamodelNature;
import de.unibamberg.minf.dme.model.datamodel.natures.xml.XmlTerminal;
import de.unibamberg.minf.dme.model.tracking.ChangeSet;
import eu.dariah.de.dariahsp.model.web.AuthPojo;
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
	public List<Datamodel> findAllSchemas() {
		return schemaDao.findAllEnclosed();
	}

	@Override
	public void saveSchema(AuthWrappedPojo<? extends Datamodel> schema, AuthPojo auth) {		
		this.innerSaveSchema(schema.getPojo(), schema.isDraft(), schema.isReadOnly(), auth.getUserId(), auth.getSessionId());
	}
	
	@Override
	public void saveSchema(Datamodel schema, AuthPojo auth) {
		this.innerSaveSchema(schema, null, null, auth.getUserId(), auth.getSessionId());
	}
	
	@Override
	public void saveSchema(Datamodel schema, List<Reference> rootNonterminals, AuthPojo auth) {
		this.innerSaveSchema(schema, null, null, auth.getUserId(), auth.getSessionId());
		
		Reference root = this.findReferenceById(schema.getId());
	
		if (root.getChildReferences()==null) {
			root.setChildReferences(new HashMap<String, Reference[]>());
		}
		Reference[] childArray = new Reference[rootNonterminals.size()];
		for (int i=0; i<rootNonterminals.size(); i++) {
			childArray[i] = rootNonterminals.get(i);
		}		
		root.getChildReferences().put(NonterminalImpl.class.getName(), childArray);
		this.saveRootReference(root);
	}
	
	private void innerSaveSchema(Datamodel schema, Boolean draft, Boolean readOnly, String userId, String sessionId) {
		RightsContainer<Datamodel> container = null;
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
	
	private RightsContainer<Datamodel> createContainer(String userId) {
		RightsContainer<Datamodel> container = new RightsContainer<Datamodel>();
		container.setOwnerId(userId);
		container.setId(new ObjectId().toString());
		container.setDraft(true);
		return container;
	}

	@Override
	public Datamodel findSchemaById(String id) {
		return schemaDao.findEnclosedById(id);
	}

	@Override
	public void deleteSchemaById(String id, AuthPojo auth) {
		RightsContainer<Datamodel> s = schemaDao.findById(id);
		if (s != null) {
			if (this.getUserCanWriteEntity(s, auth.getUserId())) {
				elementService.clearElementTree(id, auth);
				referenceDao.delete(id);
				schemaDao.delete(s, auth.getUserId(), auth.getSessionId());
			}
		}
	}
	
	/*@Override
	public void upsertSchema(Query query, Update update) {
		schemaDao.upsert(query, update);
	}*/

	/*@Override
	public <T extends SchemaNature> T convertSchema(T newSchema, SchemaNature original) {
		newSchema.setId(original.getId());
		newSchema.setLabel(original.getLabel());
		newSchema.setDescription(original.getDescription());
		return newSchema;
	}*/
	
	@Override
	public Map<String, String> getAvailableTerminals(String schemaId) {
		Map<String,String> availableTerminals = new HashMap<String,String>();
		Datamodel s = this.findSchemaById(schemaId);
		
		XmlDatamodelNature sn = s.getNature(XmlDatamodelNature.class); 
		if (sn!=null) {	
			if (sn.getTerminals()!=null) {
				for (XmlTerminal t : sn.getTerminals()) {
					availableTerminals.put(t.getId(), t.getName() + " (" + t.getNamespace() + ")");
				}
			}
		}
		return availableTerminals;
	}

	@Override
	public List<RightsContainer<Datamodel>> findAllByAuth(AuthPojo auth) {
		return schemaDao.findAllByUserId(auth.getUserId());
	}

	@Override
	public RightsContainer<Datamodel> findByIdAndAuth(String schemaId, AuthPojo auth) {
		return schemaDao.findByIdAndUserId(schemaId, auth.getUserId());
	}
	
	@Override
	public List<ChangeSet> getChangeSetForAllSchemas() {
		Query q = new Query();
		q.fields().include("_id");
		List<RightsContainer<Datamodel>> schemas = schemaDao.find(q);
		List<String> ids = new ArrayList<String>();
		if (schemas!=null) {
			for (RightsContainer<Datamodel> s : schemas) {
				ids.add(s.getId());
			}
		}
	
		return super.getChangeSetForElements(ids);
	}

	@Override
	public void setProcessingRoot(String schemaId, String elementId, AuthPojo auth) {
		Nonterminal newRoot = Nonterminal.class.cast(elementService.findById(elementId));
		newRoot.setProcessingRoot(true);
		
		elementService.unsetSchemaProcessingRoot(schemaId);
		elementService.saveElement(newRoot, auth);
	}
	
	@Override
	public DatamodelImpl cloneSchemaForSubtree(Datamodel s, Element subtree) {
		DatamodelImpl expSchema = new DatamodelImpl();
		expSchema.setDescription(s.getDescription());
		expSchema.setId(s.getId());
		expSchema.setName(s.getId());
		
		List<Nonterminal> nonterminals = ElementServiceImpl.extractAllNonterminals(subtree);
		try {
			DatamodelNature expNature;
			for (DatamodelNature nature : s.getNatures()) {
				expNature = (DatamodelNature)nature.clone();
				
				List<String> remNonterminalIds = new ArrayList<String>();
				for (String nId : nature.getNonterminalTerminalIdMap().keySet()) {
					boolean remove = true;
					for (Nonterminal n : nonterminals) {
						if (n.getId().equals(nId)) {
							remove = false;
							break;
						}
					}
					if (remove) {
						remNonterminalIds.add(nId);
					}
				}				
				for (String rId : remNonterminalIds) {
					expNature.removeNonterminalBinding(rId);
				}
				expSchema.addOrReplaceNature(expNature);				
			}
		} catch (Exception e) {
			logger.error("Failed to clone schema nature", e);
		}
		return expSchema;
	}
}
