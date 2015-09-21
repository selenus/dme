package eu.dariah.de.minfba.schereg.dao.base;

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

public abstract class TrackedEntityDaoImpl<T extends TrackedEntity> extends BaseDaoImpl<T> implements TrackedEntityDao<T> {
	@Autowired private ChangeSetDao changeSetDao;
	
	public TrackedEntityDaoImpl(Class<?> clazz) {
		super(clazz);
	}

	@Override
	public <S extends T> S save(S entity) {
		try {
			throw new GenericScheregException("Tracked entity must be saved with multi-param save method");
		} catch (Exception e) {
			logger.error("Failed to save entity", e);
		}
		return null;
	}
	
	@Override
	public List<T> find(Query q) {
		List<T> result = super.find(q);
		if (result != null) {
			for (T e : result) {
				e.startTracking();
			}
		}
		return result;
	}
	
	@Override
	public List<T> findAll() {
		List<T> result = super.findAll();
		if (result != null) {
			for (T e : result) {
				e.startTracking();
			}
		}
		return result;
	}
	
	@Override
	public List<T> findAll(Sort sort) {
		List<T> result = super.findAll(sort);
		if (result != null) {
			for (T e : result) {
				e.startTracking();
			}
		}
		return result;
	}
	
	@Override
	public T findById(String id) {
		T result = super.findById(id);
		if (result != null) {
			result.startTracking();
		}
		return result;
	}
	
	@Override
	public T findOne(Query q) {
		T result = super.findOne(q);
		if (result != null) {
			result.startTracking();
		}
		return result;
	}
		
	@Override
	public <S extends T> S save(S entity, String userId, String sessionId) {		
		if (entity.getId()!=null && entity.getId().isEmpty()) {
			entity.setId(null);
			entity.addChange(ChangeType.NEW_OBJECT, this.getCollectionName(), null, entity.getId());
		} else if (!mongoTemplate.exists(Query.query(Criteria.where(ID_FIELD).is(entity.getId())), this.getCollectionName())) {
			entity.addChange(ChangeType.NEW_OBJECT, this.getCollectionName(), null, entity.getId());
		}
		List<Change> changes = entity.flush();
		mongoTemplate.save(entity, this.getCollectionName());
		
		if (changes!=null && changes.size()>0) {
			ChangeSet c = new ChangeSet();
			c.setUserId(userId);
			c.setSessionId(sessionId);
			c.setEntityId(entity.getId());
			c.setChanges(changes);
			
			changeSetDao.save(c);
		}
		return entity;
	}
}
