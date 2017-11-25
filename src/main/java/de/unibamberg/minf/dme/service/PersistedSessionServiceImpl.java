package de.unibamberg.minf.dme.service;

import java.util.List;
import java.util.Locale;


import org.bson.types.ObjectId;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import de.unibamberg.minf.dme.dao.interfaces.PersistedSessionDao;
import de.unibamberg.minf.dme.exception.GenericScheregException;
import de.unibamberg.minf.dme.model.PersistedSession;
import de.unibamberg.minf.dme.model.LogEntry.LogType;
import de.unibamberg.minf.dme.service.interfaces.PersistedSessionService;

@Service
public class PersistedSessionServiceImpl implements PersistedSessionService {
	@Autowired private PersistedSessionDao sessionDao;

	@Override
	public List<PersistedSession> findAllByUser(String entityId, String userId) {
		return sessionDao.find(Query.query(Criteria.where("userId").is(userId).and("entityId").is(entityId)));
	}
	
	@Override
	public List<PersistedSession> findExpiredSessions(DateTime cutoffTimestamp) {
		return sessionDao.find(Query.query(Criteria.where("notExpiring").is(false).and("lastAccessed").lte(cutoffTimestamp)));
	}
	
	@Override
	public PersistedSession access(String entityId, String httpSessionId, String userId) {
		PersistedSession s = sessionDao.findOne(Query.query(Criteria.where("httpSessionId").is(httpSessionId).and("entityId").is(entityId)));
		if (s==null) {
			return null;
		}
		return s;
		//return this.saveSession(s);
	}
	
	@Override
	public PersistedSession accessOrCreate(String entityId, String httpSessionId, String userId, MessageSource messageSource, Locale locale) throws GenericScheregException {
		PersistedSession s = this.find(entityId, httpSessionId, userId);
		if (s==null) {
			s = this.findLatest(entityId, userId);
			if (s!=null) {
				return reassignPersistedSession(httpSessionId, userId, s.getId());
			} else {
				s = createAndSaveSession(entityId, httpSessionId, userId, messageSource, locale);
			}
		}
		return this.saveSession(s);
	}

	@Override
	public String getSampleInputValue(PersistedSession s, String functionId) {
		if (s.getSelectedValueMap()!=null) {
			
			if (s.getSelectedValueMap().containsKey(functionId)) {
				return s.getSelectedValueMap().get(functionId);
			}
		}
		return "";
	}
	
	@Override
	public PersistedSession createAndSaveSession(String entityId, String httpSessionId, String userId, MessageSource messageSource, Locale locale) throws GenericScheregException {
		if (httpSessionId==null) {
			throw new GenericScheregException("PersistedSession can only be created on a valid http session -> none provided");
		}
		PersistedSession session = new PersistedSession();
		session.setId(new ObjectId().toString());
		session.setHttpSessionId(httpSessionId);
		session.setUserId(userId);
		session.setEntityId(entityId);
		session.addLogEntry(LogType.INFO, messageSource.getMessage("~de.unibamberg.minf.dme.editor.sample.log.session_started", new Object[]{session.getId()}, locale));
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
	
	@Override
	public void deleteSessions(List<PersistedSession> sessions) {
		sessionDao.delete(sessions);
	}
	
	private PersistedSession findLatest(String entityId, String userId) {
		PersistedSession s = sessionDao.findOne(Query.query(Criteria.where("userId").is(userId).and("entityId").is(entityId)), 
				new Sort(Sort.Direction.DESC, "lastAccessed"));
		if (s==null) {
			return null;
		}
		return s;
	}
	
	private PersistedSession find(String entityId, String httpSessionId, String userId) {
		return sessionDao.findOne(Query.query(Criteria.where("httpSessionId").is(httpSessionId).and("entityId").is(entityId)));
	}
}