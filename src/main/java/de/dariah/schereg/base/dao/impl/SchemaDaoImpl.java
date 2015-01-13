package de.dariah.schereg.base.dao.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import de.dariah.base.dao.base.PersistedEntityDaoImpl;
import de.dariah.base.model.base.SchemaElement;
import de.dariah.base.model.base.SchemaElementImpl;
import de.dariah.schereg.base.dao.SchemaDao;
import de.dariah.schereg.base.model.Alias;
import de.dariah.schereg.base.model.Attribute;
import de.dariah.schereg.base.model.Containment;
import de.dariah.schereg.base.model.Domain;
import de.dariah.schereg.base.model.DomainValue;
import de.dariah.schereg.base.model.Entity;
import de.dariah.schereg.base.model.File;
import de.dariah.schereg.base.model.Relationship;
import de.dariah.schereg.base.model.Schema;
import de.dariah.schereg.base.model.Subtype;
import de.dariah.schereg.base.model.Synonym;
import de.dariah.schereg.util.ScheRegConstants;

@Repository
public class SchemaDaoImpl extends PersistedEntityDaoImpl<Schema> implements SchemaDao {

	public SchemaDaoImpl() {
		super(Schema.class);
	}
	
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public Schema saveImportedSchema(Schema schema, Collection<SchemaElement> schemaElements) {
		
		if (schema == null) {
			return null;
		}
		
		// Make sure that the elements have valid id references
		if (schema.getId() == 0) {
			saveOrUpdate(schema);
		}
		
		//Date currentDate = new Date();
		HashMap<Integer, HashSet<SchemaElement>> savedObjects = new HashMap<Integer, HashSet<SchemaElement>>();
		
		/*
		if (schemaElements != null && schemaElements.size() > 0) {
			for (PersistedSchemaElement element : schemaElements) {
				element.setSchema(schema);
				element.setModified(currentDate);
				if (element.getCreated() == null) {
					element.setCreated(currentDate);
				}				
				int key = element.getClass().hashCode();
				if (!savedObjects.containsKey(key)) {
					savedObjects.put(key, new HashSet<PersistedSchemaElement>());
				}
				savedObjects.get(key).add(element);
			}
		}*/

		try {
			HashSet<SchemaElement> elements = savedObjects.remove(Entity.class.hashCode()); 
			if (elements != null && elements.size() > 0) {
				for (SchemaElement entity : elements) {
					getCurrentSession().saveOrUpdate(entity);
				}
			}
			
			elements = savedObjects.remove(Domain.class.hashCode());
			if (elements != null && elements.size() > 0) {
				for (SchemaElement entity : elements) {
					getCurrentSession().saveOrUpdate(entity);
				}
			}	
			
			elements = savedObjects.remove(Attribute.class.hashCode());
			if (elements != null && elements.size() > 0) {
				for (SchemaElement entity : elements) {			
					getCurrentSession().saveOrUpdate(entity);
				}
			}
			
			elements = savedObjects.remove(DomainValue.class.hashCode());
			if (elements != null && elements.size() > 0) {
				for (SchemaElement entity : elements) {
					getCurrentSession().saveOrUpdate(entity);
				}
			}
			
			elements = savedObjects.remove(Relationship.class.hashCode());
			if (elements != null && elements.size() > 0) {
				for (SchemaElement entity : elements) {
					Relationship rel = (Relationship)entity;
					if (rel.getLeft()!=null) {
						rel.setLeftId(rel.getLeft().getId());
					}
					if (rel.getRight()!=null) {
						rel.setRightId(rel.getRight().getId());
					}
					getCurrentSession().saveOrUpdate(entity);
				}
			}
			
			elements = savedObjects.remove(Containment.class.hashCode());
			if (elements != null && elements.size() > 0) {
				for (SchemaElement entity : elements) {
					Containment cont = (Containment)entity;
					if (cont.getParent()!=null) {
						cont.setParentId(cont.getParent().getId());
					}
					if (cont.getChild()!=null) {
						cont.setChildId(cont.getChild().getId());
					}
					getCurrentSession().saveOrUpdate(entity);
				}
			}
			
			elements = savedObjects.remove(Subtype.class.hashCode());
			if (elements != null && elements.size() > 0) {
				for (SchemaElement entity : elements) {
					Subtype subtype = (Subtype)entity;
					if (subtype.getParent()!=null) {
						subtype.setParentId(subtype.getParent().getId());
					}
					if (subtype.getChild()!=null) {
						subtype.setChildId(subtype.getChild().getId());
					}				
					getCurrentSession().saveOrUpdate(entity);
				}
			}
			
			elements = savedObjects.remove(Synonym.class.hashCode());
			if (elements != null && elements.size() > 0) {
				for (SchemaElement entity : elements) {			
					getCurrentSession().saveOrUpdate(entity);
				}
			}

			elements = savedObjects.remove(Alias.class.hashCode());
			if (elements != null && elements.size() > 0) {
				for (SchemaElement entity : elements) {
					getCurrentSession().saveOrUpdate(entity);
				}
			}
			
			if (savedObjects.size() > 0) {
				throw new Exception("Not all elements of a schema could be resolved. Import cancelled.");
			}
			
			schema.setState(ScheRegConstants.STATE_OK);
			schema.setMessage("");
			saveOrUpdate(schema);
			
		} catch (Exception ex) {
			clear();
			String errorMessage = "Failed to save PersistedSchemaElement.";
			logger.error(errorMessage, ex);
			throw new RuntimeException(errorMessage, ex);
		}
		
		flush();
		clear();
		
		return schema;
	}
	
	public Schema getById(int id) {
		return super.findById(id);
	}
	
	@Override
	public Schema findById(int id, boolean fullyInitialized) {
		if (!fullyInitialized) {
			return super.findById(id);
		}
		
		Schema s = (Schema)getCurrentSession().byId(Schema.class).load(id);
		
		//Hibernate.initialize(s);
		getCurrentSession().refresh(s);
		getCurrentSession().evict(s);
		
		flush();
		
		return s;
	}
	
	
	@Override
	public List<Schema> findAll() {
		return super.findAll();
	}

	@Override
	public List<Schema> findByName(String name, boolean caseInsensitive) {
		Criteria cr = getCurrentSession().createCriteria(Schema.class);
		cr.add(Restrictions.eq("name", name).ignoreCase());
		return cast(cr.list());
	}

	@Override
	public int getFileBySchema(int schemaId) {
		Criteria cr = getCurrentSession().createCriteria(Schema.class);
		cr.add(Restrictions.idEq(schemaId));
		cr.setProjection(Projections.property("file"));
		
		List result = cr.list();
		if (result == null || result.size()==0 || result.get(0)==null) {
			return -1;
		}
		if (result.get(0) instanceof File) {
			return ((File)result.get(0)).getId();
		}
		return (int)result.get(0);
	}
	
	@Override
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public int[] saveOrUpdate(final Collection<Schema> schemas) {
		for (Schema schema : schemas) {
			if (schema.getUuid() == null) {
				schema.setUuid(java.util.UUID.randomUUID().toString());
			}
		}	
		return super.saveOrUpdate(schemas);
	}
	
	@Override
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public Schema saveOrUpdate(final Schema schema) {
		if (schema.getUuid() == null) {
			schema.setUuid(java.util.UUID.randomUUID().toString());
		}
		return super.saveOrUpdate(schema);
	}

}
