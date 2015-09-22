package eu.dariah.de.minfba.schereg.dao;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import eu.dariah.de.minfba.core.metamodel.tracking.ChangeSet;
import eu.dariah.de.minfba.schereg.dao.base.BaseDaoImpl;
import eu.dariah.de.minfba.schereg.dao.interfaces.ChangeSetDao;

@Repository
public class ChangeSetDaoImpl extends BaseDaoImpl<ChangeSet> implements ChangeSetDao {
	public ChangeSetDaoImpl() {
		super(ChangeSet.class);
	}
	
	@Override
	public ChangeSet save(ChangeSet entity) {
		ChangeSet sessionChangeSet = findByIds(entity.getSessionId(), entity.getEntityId(), entity.getElementId());
		if (sessionChangeSet==null) {
			return super.save(entity);
		}
		sessionChangeSet.getChanges().addAll(entity.getChanges());
		return super.save(sessionChangeSet);
	}
	
	public ChangeSet findByIds(String sessionId, String entityId, String elementId) {
		return this.findOne(Query.query(Criteria
				.where("sessionId").is(sessionId)
				.and("entityId").is(entityId)
				.and("elementId").is(elementId)));
	}
}
