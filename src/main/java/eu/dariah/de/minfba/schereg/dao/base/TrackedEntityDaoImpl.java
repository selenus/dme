package eu.dariah.de.minfba.schereg.dao.base;

import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import eu.dariah.de.minfba.core.metamodel.tracking.Change;
import eu.dariah.de.minfba.core.metamodel.tracking.ChangeSet;
import eu.dariah.de.minfba.core.metamodel.tracking.ChangeType;
import eu.dariah.de.minfba.core.metamodel.tracking.TrackedEntity;
import eu.dariah.de.minfba.schereg.dao.interfaces.ChangeSetDao;
import eu.dariah.de.minfba.schereg.exception.GenericScheregException;

public class TrackedEntityDaoImpl<T extends TrackedEntity> extends DaoImpl<T> implements TrackedEntityDao<T> {
	@Autowired private ChangeSetDao changeSetDao;
	
	public TrackedEntityDaoImpl(Class<?> clazz) {
		super(clazz);
	}

	@Override
	public List<T> find(Query q) {
		List<T> result = mongoTemplate.find(q, this.clazz, this.getCollectionName());
		if (result != null) {
			for (T e : result) {
				e.flush();
			}
		}
		return result;
	}
	
	@Override
	public List<T> findAll() {
		List<T> result = mongoTemplate.findAll(clazz, this.getCollectionName());
		if (result != null) {
			for (T e : result) {
				e.flush();
			}
		}
		return result;
	}
	
	@Override
	public List<T> findAll(Sort sort) {
		// TODO sort
		List<T> result = mongoTemplate.findAll(clazz, this.getCollectionName());
		if (result != null) {
			for (T e : result) {
				e.flush();
			}
		}
		return result;
	}
	
	@Override
	public T findById(String id) {
		T result = mongoTemplate.findById(id, clazz, this.getCollectionName());
		if (result != null) {
			result.flush();
		}
		return result;
	}
	
	@Override
	public T findOne(Query q) {
		T result = mongoTemplate.findOne(q, this.clazz, this.getCollectionName());
		if (result != null) {
			result.flush();
		}
		return result;
	}
	
	@Override
	public int delete(Collection<String> id, String userId, String sessionId) {
		List<T> deleted = mongoTemplate.findAllAndRemove(Query.query(Criteria.where(ID_FIELD).in(id)), this.getCollectionName());
		if (deleted!=null) {
			for (T d : deleted) {
				d.flush();
				d.addChange(ChangeType.DELETE_OBJECT, this.getCollectionName(), d.getId(), null);
				List<Change> changes = d.flush();
				this.createAndSaveChangeSet(changes, d.getId(), d.getEntityId(), userId, sessionId);
			}
			return deleted.size();
		}
		return 0;
	}
	
	@Override
	public void delete(T element, String userId, String sessionId) {
		if (element.getId()==null || element.getId().isEmpty() || 
				!mongoTemplate.exists(Query.query(Criteria.where(ID_FIELD).is(element.getId())), this.getCollectionName())) {
			return;
		}
		element.addChange(ChangeType.DELETE_OBJECT, this.getCollectionName(), element.getId(), null);
		List<Change> changes = element.flush();
		String elementId = element.getId();
		String parentEntityId = element.getEntityId();
		
		mongoTemplate.remove(element, this.getCollectionName());
		
		this.createAndSaveChangeSet(changes, elementId, parentEntityId, userId, sessionId);
	}
			
	@Override
	public <S extends T> S save(S element, String userId, String sessionId) {		
		if (element.getId()!=null && element.getId().isEmpty()) {
			element.setId(null);
			element.addChange(ChangeType.NEW_OBJECT, this.getCollectionName(), null, element.getId());
		} else if (!mongoTemplate.exists(Query.query(Criteria.where(ID_FIELD).is(element.getId())), this.getCollectionName())) {
			element.addChange(ChangeType.NEW_OBJECT, this.getCollectionName(), null, element.getId());
		}
		List<Change> changes = element.flush();
		mongoTemplate.save(element, this.getCollectionName());
		
		this.createAndSaveChangeSet(changes, element.getId(), element.getEntityId(), userId, sessionId);
		return element;
	}
	
	private void createAndSaveChangeSet(List<Change> changes, String elementId, String parentEntityId, String userId, String sessionId) {
		if (changes!=null && changes.size()>0) {
			ChangeSet c = new ChangeSet();
			c.setUserId(userId);
			c.setSessionId(sessionId);
			c.setEntityId(parentEntityId);
			c.setElementId(elementId);
			c.setChanges(changes);
			
			changeSetDao.save(c);
		}
	}
}
