package de.unibamberg.minf.dme.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import de.unibamberg.minf.dme.dao.base.BaseDaoImpl;
import de.unibamberg.minf.dme.dao.interfaces.ElementDao;
import de.unibamberg.minf.dme.dao.interfaces.FunctionDao;
import de.unibamberg.minf.dme.dao.interfaces.GrammarDao;
import de.unibamberg.minf.dme.dao.interfaces.ReferenceDao;
import de.unibamberg.minf.dme.model.RightsContainer;
import de.unibamberg.minf.dme.model.base.Element;
import de.unibamberg.minf.dme.model.base.Nonterminal;
import de.unibamberg.minf.dme.model.datamodel.DatamodelImpl;
import de.unibamberg.minf.dme.model.datamodel.NonterminalImpl;
import de.unibamberg.minf.dme.model.datamodel.TerminalImpl;
import de.unibamberg.minf.dme.model.datamodel.base.Datamodel;
import de.unibamberg.minf.dme.model.datamodel.base.DatamodelNature;
import de.unibamberg.minf.dme.model.datamodel.natures.CsvDatamodelNature;
import de.unibamberg.minf.dme.model.datamodel.natures.JsonDatamodelNature;
import de.unibamberg.minf.dme.model.datamodel.natures.TextDatamodelNature;
import de.unibamberg.minf.dme.model.datamodel.natures.XmlDatamodelNature;
import de.unibamberg.minf.dme.model.datamodel.natures.xml.XmlNamespace;
import de.unibamberg.minf.dme.model.datamodel.natures.xml.XmlTerminal;
import de.unibamberg.minf.dme.model.exception.MetamodelConsistencyException;
import de.unibamberg.minf.dme.model.reference.Reference;
import de.unibamberg.minf.dme.model.tracking.ChangeSet;
import de.unibamberg.minf.dme.pojo.AuthWrappedPojo;
import de.unibamberg.minf.dme.service.base.BaseEntityServiceImpl;
import de.unibamberg.minf.dme.service.interfaces.ElementService;
import de.unibamberg.minf.dme.service.interfaces.SchemaService;
import eu.dariah.de.dariahsp.model.web.AuthPojo;

@Service
public class SchemaServiceImpl extends BaseEntityServiceImpl implements SchemaService {
	@Autowired private ElementService elementService;
	
	@Autowired private ElementDao elementDao;
	@Autowired private FunctionDao functionDao;
	@Autowired private GrammarDao grammarDao;
	
	@Autowired private ReferenceDao referenceDao;
	
	
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

	@Override
	public boolean changeId(String currentId, String id) {
		RightsContainer<Datamodel> dm = schemaDao.findById(currentId);
		dm.setId(id);
		schemaDao.save(dm);
		
		Reference rootR = referenceDao.findById(currentId);
		rootR.setId(id);
		referenceDao.save(rootR);
		
		elementDao.updateEntityId(currentId, id);
		functionDao.updateEntityId(currentId, id);
		grammarDao.updateEntityId(currentId, id);
		
		schemaDao.delete(currentId);
		
		return true;
	}

	@Override
	public void removeNature(String entityId, String natureClass, AuthPojo auth) {
		Datamodel m = this.findSchemaById(entityId);
		for (DatamodelNature n : m.getNatures()) {
			if (n.getClass().getName().equals(natureClass)) {
				m.getNatures().remove(n);
				break;
			}
		}
		this.saveSchema(m, auth);
	}

	@Override
	public void addNature(String entityId, String natureClass, AuthPojo auth) {
		Datamodel m = this.findSchemaById(entityId);
		DatamodelNature n = this.createNature(natureClass);
		if (n!=null && m.getNature(n.getClass())==null) {
			m.addOrReplaceNature(n);
		}
		this.saveSchema(m, auth);
	}

	private DatamodelNature createNature(String natureClass) {
		if (natureClass==null) {
			return null;
		} else if (XmlDatamodelNature.class.getName().equals(natureClass)) {
			return new XmlDatamodelNature();
		} else if (JsonDatamodelNature.class.getName().equals(natureClass)) {
			return new JsonDatamodelNature();
		} else if (CsvDatamodelNature.class.getName().equals(natureClass)) {
			return new CsvDatamodelNature();
		} else if (TextDatamodelNature.class.getName().equals(natureClass)) {
			return new TextDatamodelNature();
		}
		logger.error("Failed to create DatamodelNature of type: " + natureClass);
		return null;
	}

	@Override
	public List<Class<? extends DatamodelNature>> getMissingNatures(String entityId) {
		List<Class<? extends DatamodelNature>> classes = getAllSuportedNatures();
		Datamodel m = this.findSchemaById(entityId);
		if (m==null) {
			return null;
		}
		if (m.getNatures()!=null) {
			for (DatamodelNature n : m.getNatures()) {
				classes.remove(n.getClass());
			}
		}
		return classes;
	}
	
	public static List<Class<? extends DatamodelNature>> getAllSuportedNatures() {
		List<Class<? extends DatamodelNature>> classes = new ArrayList<Class<? extends DatamodelNature>>();
		classes.add(XmlDatamodelNature.class);
		classes.add(JsonDatamodelNature.class);
		classes.add(CsvDatamodelNature.class);
		classes.add(TextDatamodelNature.class);
		return classes;
	}

	@Override
	public void updateNature(String entityId, XmlDatamodelNature xmlNature, AuthPojo auth) {
		Datamodel m = this.findSchemaById(entityId);
		XmlDatamodelNature existingXmlNature = m.getNature(XmlDatamodelNature.class);
		existingXmlNature.setRecordPath(xmlNature.getRecordPath());
		
		// To prevent duplicate urls
		Map<String, String> urlPrefixMap = new HashMap<String, String>();
		List<String> removeNamespaces = new ArrayList<String>();
		if (existingXmlNature.getNamespaces()!=null) {
			for (XmlNamespace existNs : existingXmlNature.getNamespaces()) {
				boolean remove = true;
				if (xmlNature.getNamespaces()!=null) {
					for (XmlNamespace updateNs : xmlNature.getNamespaces()) {
						if (updateNs.getUrl().trim().isEmpty()) {
							continue;
						}
						if (existNs.getUrl().equals(updateNs.getUrl().trim())) {
							urlPrefixMap.put(updateNs.getUrl().trim(), updateNs.getPrefix().trim());
							remove = false;
						}
					}
				}
				if (remove) {
					removeNamespaces.add(existNs.getUrl());
				}
			}
			}
		for (XmlNamespace updateNs : xmlNature.getNamespaces()) {
			if (!urlPrefixMap.containsKey(updateNs.getUrl().trim())) {
				urlPrefixMap.put(updateNs.getUrl().trim(), updateNs.getPrefix().trim());
			}
		}
		
		if (existingXmlNature.getTerminals()!=null) {
			for (XmlTerminal xmlTerminal : existingXmlNature.getTerminals()) {
				if (removeNamespaces.contains(xmlTerminal.getNamespace())) {
					xmlTerminal.setNamespace("");
				}
			}
		}
		
		existingXmlNature.setNamespaces(new ArrayList<XmlNamespace>());
		for (String nsUrl : urlPrefixMap.keySet()) {
			if (!nsUrl.isEmpty()) {
				existingXmlNature.getNamespaces().add(new XmlNamespace(urlPrefixMap.get(nsUrl), nsUrl));
			}
		}
		
		this.saveSchema(m, auth);
	}

	@Override
	public void createTerminals(String entityId, String natureClass, String namingOption, AuthPojo auth) throws ClassNotFoundException, MetamodelConsistencyException {
		@SuppressWarnings("unchecked")
		Class<? extends DatamodelNature> modelClazz = (Class<? extends DatamodelNature>)Class.forName(natureClass);
		Datamodel m = findByIdAndAuth(entityId, auth).getElement();
		DatamodelNature n = m.getNature(modelClazz);
		
		List<Element> nonterminals = elementDao.find(new Query(Criteria.where("entityId").is(entityId).and("_class").is(NonterminalImpl.class.getName())));
		TerminalImpl t;
		for (Element e : nonterminals) {
			if (n.getTerminalId(e.getId())==null) {
				t = XmlDatamodelNature.class.isAssignableFrom(n.getClass()) ? new XmlTerminal() : new TerminalImpl();
				t.setId(BaseDaoImpl.createNewObjectId());
				if (namingOption.equals("unchanged")) {
					t.setName(e.getName());
				} else if (namingOption.equals("first_lower")) {
					t.setName(e.getName().substring(0, 1).toUpperCase() + e.getName().substring(1));
				} else if (namingOption.equals("all_upper")) {
					t.setName(e.getName().toUpperCase());
				} else if (namingOption.equals("all_lower")) {
					t.setName(e.getName().toLowerCase());
				} 
				n.addTerminal(t);
				n.mapNonterminal(e.getId(), t.getId());
			}
		}
		
		this.saveSchema(m, auth);
	}
}
