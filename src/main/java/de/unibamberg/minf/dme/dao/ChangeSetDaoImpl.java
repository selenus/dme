package de.unibamberg.minf.dme.dao;

import java.util.List;

import org.joda.time.DateTime;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import de.unibamberg.minf.dme.dao.base.BaseDaoImpl;
import de.unibamberg.minf.dme.dao.interfaces.ChangeSetDao;
import de.unibamberg.minf.dme.model.tracking.ChangeSet;

@Repository
public class ChangeSetDaoImpl extends BaseDaoImpl<ChangeSet> implements ChangeSetDao {
	public ChangeSetDaoImpl() {
		super(ChangeSet.class);
	}

	@Override
	@SuppressWarnings("unchecked")
	public ChangeSet save(ChangeSet entity) {
		ChangeSet sessionChangeSet = findOneByIds(entity.getSessionId(), entity.getEntityId(), entity.getElementId());
		if (sessionChangeSet==null) {
			entity.setTimestamp(DateTime.now());
			return super.save(entity);
		}
		sessionChangeSet.getChanges().addAll(entity.getChanges());
		sessionChangeSet.setTimestamp(DateTime.now());
		return super.save(sessionChangeSet);
	}
	
	@Override
	public ChangeSet findOneByIds(String sessionId, String entityId, String elementId) {
		return this.findOne(Query.query(Criteria
				.where("sessionId").is(sessionId)
				.and("entityId").is(entityId)
				.and("elementId").is(elementId)));
	}
	
	@Override
	public List<ChangeSet> findByEntityId(String entityId) {
		Query q = Query.query(Criteria.where("entityId").is(entityId));
		q.with(new Sort(Sort.Direction.DESC, "timestamp"));
		return this.find(q);
	}
	
	@Override
	public List<ChangeSet> findByElementId(String elementId) {
		Query q = Query.query(Criteria.where("elementId").is(elementId));
		q.with(new Sort(Sort.Direction.DESC, "timestamp"));
		return this.find(q);
	}
	
	@Override
	public List<ChangeSet> findByEntityIds(List<String> entityIds) {
		Query q = Query.query(Criteria.where("entityId").in(entityIds));
		q.with(new Sort(Sort.Direction.DESC, "timestamp"));
		return this.find(q);
	}
	
	@Override
	public List<ChangeSet> findByElementIds(List<String> elementIds) {
		Query q = Query.query(Criteria.where("elementId").in(elementIds));
		q.with(new Sort(Sort.Direction.DESC, "timestamp"));
		return this.find(q);
	}

	@Override
	public ChangeSet findLatestByEntityId(String entityId) {
		Query q = Query.query(Criteria.where("entityId").is(entityId));
		q.with(new Sort(Sort.Direction.DESC, "timestamp"));
		q.limit(1);		

		return this.findOne(q);
	}
}
