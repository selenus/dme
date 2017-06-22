package eu.dariah.de.minfba.schereg.dao.base;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import com.mongodb.WriteResult;

import eu.dariah.de.minfba.core.metamodel.tracking.Change;
import eu.dariah.de.minfba.core.metamodel.tracking.ChangeImpl;
import eu.dariah.de.minfba.core.metamodel.tracking.ChangeSet;
import eu.dariah.de.minfba.core.metamodel.tracking.ChangeType;
import eu.dariah.de.minfba.core.metamodel.tracking.TrackedEntity;
import eu.dariah.de.minfba.schereg.dao.interfaces.ChangeSetDao;
import eu.dariah.de.minfba.schereg.exception.GenericScheregException;

public abstract class TrackedEntityDaoImpl<T extends TrackedEntity> extends BaseDaoImpl<T> implements TrackedEntityDao<T> {
	@Autowired protected ChangeSetDao changeSetDao;
	
	public TrackedEntityDaoImpl(Class<?> clazz) {
		super(clazz);
	}
	
	public TrackedEntityDaoImpl(Class<?> clazz, String collectionName) {
		super(clazz, collectionName);
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
		Query query = new Query();
		query.with(sort);
		
		List<T> result = mongoTemplate.find(query, clazz, this.getCollectionName());
		if (result != null) {
			for (T e : result) {
				e.flush();
			}
		}
		return result;
	}
	
	@Override
	public T findOne(Query q, Sort sort) {
		q.with(sort);
		T result = mongoTemplate.findOne(q, clazz, this.getCollectionName());
		if (result != null) {
			result.flush();
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
		/*List<T> delete = mongoTemplate.find(Query.query(Criteria.where(ID_FIELD).in(id)), this.clazz, this.getCollectionName());
		
		int count = 0;
		for (T del : delete) {
			this.delete(del, userId, sessionId);
			count++;
		}
		return count;*/
		
		WriteResult result = mongoTemplate.remove(Query.query(Criteria.where(ID_FIELD).in(id)), this.getCollectionName());
		return result.getN();
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
		boolean isNew = false;
		if (element.getId()!=null && element.getId().isEmpty()) {
			element.setId(null);
			isNew = true;
		} else if (!mongoTemplate.exists(Query.query(Criteria.where(ID_FIELD).is(element.getId())), this.getCollectionName())) {
			isNew = true;
		}
		List<Change> changes = element.flush();
		mongoTemplate.save(element, this.getCollectionName());
		
		if (isNew) {
			if (changes==null) {
				changes = new ArrayList<Change>();
			}
			changes.add(new ChangeImpl<String>(ChangeType.NEW_OBJECT, this.getCollectionName(), null, element.getId(), DateTime.now()));
		}
		
		this.createAndSaveChangeSet(changes, element.getId(), element.getEntityId(), userId, sessionId);
		return element;
	}
	
	protected void createAndSaveChangeSet(List<Change> changes, String elementId, String parentEntityId, String userId, String sessionId) {
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
