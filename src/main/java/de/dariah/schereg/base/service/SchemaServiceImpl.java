package de.dariah.schereg.base.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import de.dariah.base.model.base.SchemaElement;
import de.dariah.schereg.base.dao.AliasDao;
import de.dariah.schereg.base.dao.AttributeDao;
import de.dariah.schereg.base.dao.ContainmentDao;
import de.dariah.schereg.base.dao.DomainDao;
import de.dariah.schereg.base.dao.DomainValueDao;
import de.dariah.schereg.base.dao.EntityDao;
import de.dariah.schereg.base.dao.FileDao;
import de.dariah.schereg.base.dao.RelationshipDao;
import de.dariah.schereg.base.dao.SchemaDao;
import de.dariah.schereg.base.dao.SubtypeDao;
import de.dariah.schereg.base.dao.SynonymDao;
import de.dariah.schereg.base.model.Alias;
import de.dariah.schereg.base.model.Attribute;
import de.dariah.schereg.base.model.Containment;
import de.dariah.schereg.base.model.Domain;
import de.dariah.schereg.base.model.DomainValue;
import de.dariah.schereg.base.model.Entity;
import de.dariah.schereg.base.model.Relationship;
import de.dariah.schereg.base.model.Schema;
import de.dariah.schereg.base.model.Subtype;
import de.dariah.schereg.base.model.Synonym;
import de.dariah.schereg.util.ScheRegConstants;

@Service
@Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
public class SchemaServiceImpl implements SchemaService {

	private static final Logger logger = LoggerFactory.getLogger(SchemaServiceImpl.class);
	
	private final ConcurrentHashMap<Schema, Collection<SchemaElement>> schemaProcessingMap;
	
	@Autowired private SchemaDao schemaDao;
	@Autowired private DomainDao domainDao;
	@Autowired private DomainValueDao domainValueDao;
	@Autowired private EntityDao entityDao;
	@Autowired private AttributeDao attributeDao;
	@Autowired private RelationshipDao relationshipDao;
	@Autowired private ContainmentDao containmentDao;
	@Autowired private SubtypeDao subtypeDao;
	@Autowired private SynonymDao synonymDao;
	@Autowired private AliasDao aliasDao;
	@Autowired private FileDao fileDao;
	
	public ConcurrentHashMap<Schema, Collection<SchemaElement>> getSchemaProcessingMap() { return schemaProcessingMap; }
	
	public SchemaServiceImpl() {
		schemaProcessingMap = new ConcurrentHashMap<Schema, Collection<SchemaElement>>();
	}
		
	@Override
	public List<Domain> getGlobalDomains() {
		return domainDao.findGlobalDomains();
	}

	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public void saveImportedSchema(Schema schema, Collection<SchemaElement> schemaElements) throws Exception {
		HashMap<Integer, HashSet<SchemaElement>> objToSave = new HashMap<Integer, HashSet<SchemaElement>>();
		for (SchemaElement element : schemaElements) {			
			int key = element.getClass().hashCode();
			if (!objToSave.containsKey(key)) {
				objToSave.put(key, new HashSet<SchemaElement>());
			}
			objToSave.get(key).add(element);
		} 
						
		HashSet<SchemaElement> elements = objToSave.remove(Entity.class.hashCode());
		if (elements != null && elements.size() > 0) {
			entityDao.saveOrUpdate(entityDao.cast(elements));
		}
		elements = objToSave.remove(Domain.class.hashCode());
		if (elements != null && elements.size() > 0) {
			domainDao.saveOrUpdate(domainDao.cast(elements));
		}
		elements = objToSave.remove(Attribute.class.hashCode());
		if (elements != null && elements.size() > 0) {
			
			for (Attribute attr : attributeDao.cast(elements)) {
				if (attr.getEntity().getId() <= 0) {
					logger.warn("Unsaved entity references in attribute");
				}
			}
			
			attributeDao.saveOrUpdate(attributeDao.cast(elements));
		}
		elements = objToSave.remove(DomainValue.class.hashCode());
		if (elements != null && elements.size() > 0) {
			domainValueDao.saveOrUpdate(domainValueDao.cast(elements));
		}
		elements = objToSave.remove(Relationship.class.hashCode());
		if (elements != null && elements.size() > 0) {
			relationshipDao.saveOrUpdate(relationshipDao.cast(elements));
		}
		elements = objToSave.remove(Containment.class.hashCode());
		if (elements != null && elements.size() > 0) {
			containmentDao.saveOrUpdate(containmentDao.cast(elements));
		}
		elements = objToSave.remove(Subtype.class.hashCode());
		if (elements != null && elements.size() > 0) {
			subtypeDao.saveOrUpdate(subtypeDao.cast(elements));
		}
		elements = objToSave.remove(Synonym.class.hashCode());
		if (elements != null && elements.size() > 0) {
			aliasDao.saveOrUpdate(aliasDao.cast(elements));
		}
		elements = objToSave.remove(Alias.class.hashCode());
		if (elements != null && elements.size() > 0) {
			synonymDao.saveOrUpdate(synonymDao.cast(elements));
		}
		
		if (objToSave.size() > 0) {
			throw new Exception("Not all elements of a schema could be resolved. Import cancelled.");
		}
		
		schema.setState(ScheRegConstants.STATE_OK);
		schema.setMessage("");
		schemaDao.saveOrUpdate(schema);
		
	}
		
	@Override
	@Transactional(propagation=Propagation.REQUIRES_NEW, readOnly=false)
	public void saveSchema(Schema schema) {
				
		// Meaning: schema has been updated without new source provided
		if (schema.getSource()==null) {			
			schema.resetSource();
		}
				
		schemaDao.saveOrUpdate(schema);
	}

	@Override
	public List<Schema> listSchemas() {
		List<Schema> result = schemaDao.findAll();
		
		return result;
	}

	@Override
	@Transactional(propagation=Propagation.REQUIRES_NEW, readOnly=false)
	public void removeSchema(Integer id) {
		try {
			Schema schema = schemaDao.findById(id);
			Criterion cr = Restrictions.eq("schema", schema);
			
			
			synonymDao.delete(synonymDao.findByCriterion(cr));
			aliasDao.delete(aliasDao.findByCriterion(cr));
			subtypeDao.delete(subtypeDao.findByCriterion(cr));
			containmentDao.delete(containmentDao.findByCriterion(cr));
			relationshipDao.delete(relationshipDao.findByCriterion(cr));
			domainValueDao.delete(domainValueDao.findByCriterion(cr));
			attributeDao.delete(attributeDao.findByCriterion(cr));
			domainDao.delete(domainDao.findByCriterion(cr));
			entityDao.delete(entityDao.findByCriterion(cr));
			schemaDao.delete(schema);
			
		} catch (Exception e) {
			logger.error("Failed to delete schema", e);
			throw new RuntimeException("Failed to delete schema", e);
		}
	}

	@Override
	@Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
	public Schema getSchema(Integer id) {
		return schemaDao.findById(id);
	}	

	@Override
	public Collection<Schema> getSchemasCreatedAfter(DateTime created) {
		
		Criterion cr = Restrictions.gt("created", created);
		
		return schemaDao.findByCriterion(cr);
		
	}
		
	@Override
	public List<String> getSupportedSchemaTypes() {
		List<String> schemaTypes = new ArrayList<String>();
		schemaTypes.add("XML Schema");
		return schemaTypes;
	}

	@Override
	public long getSchemaCount() {
		return schemaDao.count(null);
	}

	@Override
	public DateTime getLastModified() {
		return schemaDao.getLastModified(null);
	}

	@Override
	@Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
	public Schema getSchema(Integer id, boolean fullyInitialized) {
		return schemaDao.findById(id, fullyInitialized);
	}

	@Override
	public List<Schema> findByName(String name, boolean caseInsensitive) {
		return schemaDao.findByName(name, caseInsensitive);
	}

	@Override
	public int getFileBySchema(int schemaId) {
		if (schemaId <= 0) {
			return -1;
		}
		return schemaDao.getFileBySchema(schemaId);
	}

	@Override
	public List<Schema> getSchemas() {
		return schemaDao.findAll();
	}

	@Override
	public Schema getSchemaByUuid(String uuid) throws Exception {

		List<Criterion> cr = new ArrayList<Criterion>();
		cr.add(Restrictions.eq("uuid", uuid));
		
		return schemaDao.findByCriteriaDistinct(cr);
	}
}