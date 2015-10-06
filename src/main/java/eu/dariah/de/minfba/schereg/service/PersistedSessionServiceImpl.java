package eu.dariah.de.minfba.schereg.service;

import java.util.List;

import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import eu.dariah.de.minfba.schereg.dao.interfaces.PersistedSessionDao;
import eu.dariah.de.minfba.schereg.exception.GenericScheregException;
import eu.dariah.de.minfba.schereg.model.PersistedSession;
import eu.dariah.de.minfba.schereg.pojo.LogEntryPojo.LogType;
import eu.dariah.de.minfba.schereg.service.interfaces.PersistedSessionService;

@Service
public class PersistedSessionServiceImpl implements PersistedSessionService {
	@Autowired private PersistedSessionDao sessionDao;

	@Override
	public List<PersistedSession> findAllByUser(String entityId, String userId) {
		return sessionDao.find(Query.query(Criteria.where("userId").is(userId).and("entityId").is(entityId)));
	}
	
	@Override
	public PersistedSession access(String entityId, String httpSessionId, String userId) {
		PersistedSession s = sessionDao.findOne(Query.query(Criteria.where("httpSessionId").is(httpSessionId).and("entityId").is(entityId)));
		return this.saveSession(s);
	}
	
	@Override
	public PersistedSession accessOrCreate(String entityId, String httpSessionId, String userId) throws GenericScheregException {
		PersistedSession s = this.find(entityId, httpSessionId, userId);
		if (s==null) {
			s = createAndSaveSession(entityId, httpSessionId, userId);
		}
		return this.saveSession(s);
	}
	
	@Override
	public PersistedSession createAndSaveSession(String entityId, String httpSessionId, String userId) throws GenericScheregException {
		if (httpSessionId==null) {
			throw new GenericScheregException("PersistedSession can only be created on a valid http session -> none provided");
		}
		PersistedSession session = new PersistedSession();
		session.setId(new ObjectId().toString());
		session.setHttpSessionId(httpSessionId);
		session.setUserId(userId);
		session.setEntityId(entityId);
		session.addLogEntry(LogType.INFO, String.format("~ Schema editor session started [id: %s]", session.getId()));
		session.setCreated(DateTime.now());
		return this.saveSession(session);
	}

	@Override
	public PersistedSession reassignPersistedSession(String httpSessionId, String userId, String persistedSessionId) {
		PersistedSession s = sessionDao.findById(persistedSessionId);
		if (s!=null) {
			PersistedSession sCurrent = this.find(s.getEntityId(), httpSessionId, userId);
			if (sCurrent!=null) {
				// Loading 'current' session -> nothing to do really
				if (sCurrent.getId().equals(persistedSessionId)) {
					return sCurrent;
				}
				// Switching HTTP Session ID to remain consistent
				sCurrent.setHttpSessionId(s.getHttpSessionId());
				sessionDao.save(sCurrent);
			}
			s.setUserId(userId);
			s.setHttpSessionId(httpSessionId);
			return sessionDao.save(s);
		}
		return null;
	}

	@Override
	public PersistedSession saveSession(PersistedSession session) {
		session.setLastAccessed(DateTime.now());
		return sessionDao.save(session);
	}
	
	@Override
	public void deleteSession(String entityId, String httpSessionId, String userId) {
		this.deleteSession(this.find(entityId, httpSessionId, userId));
	}
	
	@Override
	public void deleteSession(PersistedSession session) {
		if (session!=null) {
			sessionDao.delete(session);
		}
	}
	
	private PersistedSession find(String entityId, String httpSessionId, String userId) {
		return sessionDao.findOne(Query.query(Criteria.where("httpSessionId").is(httpSessionId).and("entityId").is(entityId)));
	}
}