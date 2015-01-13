package de.dariah.schereg.base.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import de.dariah.base.dao.base.PersistedSchemaElementDao;
import de.dariah.base.model.base.ConfigurableSchemaElementImpl;
import de.dariah.base.model.base.SchemaElement;
import de.dariah.schereg.base.dao.AliasDao;
import de.dariah.schereg.base.dao.AttributeDao;
import de.dariah.schereg.base.dao.ContainmentDao;
import de.dariah.schereg.base.dao.DomainDao;
import de.dariah.schereg.base.dao.DomainValueDao;
import de.dariah.schereg.base.dao.EntityDao;
import de.dariah.schereg.base.dao.ReadOnlySchemaElementDao;
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
import de.dariah.schereg.base.model.ReadOnlySchemaElement;
import de.dariah.schereg.base.model.Relationship;
import de.dariah.schereg.base.model.Subtype;
import de.dariah.schereg.base.model.Synonym;
import de.dariah.schereg.util.SchemaElementContainer;
import de.dariah.schereg.util.StopWatch;

@Service
@Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
public class SchemaElementServiceImpl implements SchemaElementService {

	private static final Logger logger = LoggerFactory.getLogger(SchemaServiceImpl.class);
	
	@Autowired private DomainDao domainDao;
	@Autowired private DomainValueDao domainValueDao;
	@Autowired private EntityDao entityDao;
	@Autowired private AttributeDao attributeDao;
	@Autowired private RelationshipDao relationshipDao;
	@Autowired private ContainmentDao containmentDao;
	@Autowired private SubtypeDao subtypeDao;
	@Autowired private SynonymDao synonymDao;
	@Autowired private AliasDao aliasDao;
	@Autowired private ReadOnlySchemaElementDao readOnlySchemaElementDao;

	
	@Override
	public HashSet<SchemaElement> getAllSchemaElements(Integer schemaId) {
		
		StopWatch sw = new StopWatch();
		sw.start();
		
		HashSet<SchemaElement> result = new HashSet<SchemaElement>();
		
		PersistedSchemaElementDao<? extends SchemaElement>[] relevantDaos = new PersistedSchemaElementDao<?>[] 
				{ entityDao, attributeDao, synonymDao, aliasDao, subtypeDao, domainDao, domainValueDao, relationshipDao, containmentDao};
		
		for (PersistedSchemaElementDao<? extends SchemaElement> dao : relevantDaos) {
			Collection<? extends SchemaElement> tmpElements = dao.findBySchemaId(schemaId);
			
			if (tmpElements != null && tmpElements.size() > 0) {
				for (SchemaElement e : tmpElements) {
					result.add(e);
				}
			}
		}
		
		logger.debug(String.format("Loaded %d elements in one Hashtable for schema [%d] in %dms", result.size(), schemaId, sw.getElapsedTime()));
		return result;
	}
	
	public <T> T getSchemaElement(int id, Class<?> elementType) {
		
		if (elementType.equals(Attribute.class)) {
			return (T)attributeDao.findById(id);
		} else if (elementType.equals(Entity.class)) {
			return (T)entityDao.findById(id);
		} else if (elementType.equals(Synonym.class)) {
			return (T)synonymDao.findById(id);
		} else if (elementType.equals(Alias.class)) {
			return (T)aliasDao.findById(id);
		} else if (elementType.equals(Subtype.class)) {
			return (T)subtypeDao.findById(id);
		} else if (elementType.equals(Domain.class)) {
			return (T)domainDao.findById(id);
		} else if (elementType.equals(DomainValue.class)) {
			return (T)domainValueDao.findById(id);
		} else if (elementType.equals(Relationship.class)) {
			return (T)relationshipDao.findById(id);
		} else if (elementType.equals(Containment.class)) {
			return (T)containmentDao.findById(id);
		} 
		
		return null;
	}
	
	@Override
	public void saveOrUpdate(ConfigurableSchemaElementImpl se) {
		if (se instanceof Attribute) {
			attributeDao.saveOrUpdate((Attribute)se);
		} else if (se instanceof Containment) {
			containmentDao.saveOrUpdate((Containment)se);
		}		
	}	
	
	@Override
	public SchemaElementContainer getSchemaElements(Integer schemaId) {
		
		StopWatch sw = new StopWatch();
		sw.start();
		
		int outerSize = 0;
		
		SchemaElementContainer result = new SchemaElementContainer();
		
		PersistedSchemaElementDao<? extends SchemaElement>[] relevantDaos = new PersistedSchemaElementDao<?>[] 
				{ entityDao, attributeDao, synonymDao, aliasDao, subtypeDao, domainDao, domainValueDao, relationshipDao, containmentDao};
		
		for (PersistedSchemaElementDao<? extends SchemaElement> dao : relevantDaos) {
			Collection<? extends SchemaElement> tmpElements = dao.findBySchemaId(schemaId);
			
			if (tmpElements != null && tmpElements.size() > 0) {
				ArrayList<SchemaElement> innerResult = new ArrayList<SchemaElement>();
				
				Class<? extends SchemaElement> clazz = null;
				boolean classRead = false;
				
				for (SchemaElement e : tmpElements) {
					innerResult.add(e);
					if (!result.getIdLookupTable().containsKey(e.getId())) {
						result.getIdLookupTable().put(e.getId(), e);
					}
					
					if (!classRead) {
						classRead = true;
						clazz = e.getClass();
					}
				}
				outerSize += innerResult.size();
				result.getClassLookupTable().put(clazz, innerResult);
			}
		}

		logger.debug(String.format("Loaded %d elements in %d Hashtables for schema [%d] in %dms", outerSize, result.getClassLookupTable().size(), schemaId, sw.getElapsedTime()));
		return result;
	}

	@Override
	public ReadOnlySchemaElement getReadOnlySchemaElement(int output) {
		return readOnlySchemaElementDao.findById(output);
	}

	@Override
	public String getPath(int id) {

		ReadOnlySchemaElement se = getReadOnlySchemaElement(id);
		
		SchemaElementContainer sec = getSchemaElements(se.getSchemaId());
		
		try {
			ArrayList<SchemaElement> es = sec.getClassLookupTable().get(Class.forName(se.getType()));
			
			if (es!=null) {
				for (SchemaElement e : es) {
					if (e.getId()==id) {
						return resolvePath(e, sec);
					}
				}
			}
			
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	private String resolvePath(SchemaElement e, SchemaElementContainer sec) {

		String path = "";
		
		if (e instanceof Attribute) {
			path = "/" + ((Attribute) e).getEntity().getName() + "/@" + e.getName();
			for (SchemaElement e1 : sec.getClassLookupTable().get(Containment.class)) {
				Containment c = (Containment)e1; 
				if (c.getChildId()==((Attribute) e).getEntity().getId()) {
					return resolvePath(c, sec) + path;
				}
			}
		} else if (e instanceof Entity) {
			path = "/" + e.getName();
			for (SchemaElement e1 : sec.getClassLookupTable().get(Containment.class)) {
				Containment c = (Containment)e1; 
				if (c.getChildId()==e.getId()) {
					return resolvePath(c, sec) + path;
				}
			}
		} else if (e instanceof Containment) {
			path = "/" + e.getName();
			if (((Containment) e).getParentId()!=null && ((Containment) e).getParentId()!=0) {
				for (SchemaElement e1 : sec.getClassLookupTable().get(Containment.class)) {
					Containment c = (Containment)e1; 
					if (c.getChildId().intValue()==((Containment) e).getId()) {
						return resolvePath(c, sec) + path;
					}
				}
				for (SchemaElement e1 : sec.getClassLookupTable().get(Entity.class)) {
					Entity c = (Entity)e1; 
					if (c.getId()==((Containment) e).getParentId().intValue()) {
						for (SchemaElement e2 : sec.getClassLookupTable().get(Containment.class)) {
							Containment c2 = (Containment)e2; 
							if (c2.getChildId()==c.getId()) {
								return resolvePath(c, sec) + path;
							}
						}
					}
				}
			}
		}
		
		return path;
	}


}
