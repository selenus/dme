package eu.dariah.de.minfba.schereg.service.interfaces;

import java.util.List;

import org.joda.time.DateTime;

import eu.dariah.de.minfba.schereg.exception.GenericScheregException;
import eu.dariah.de.minfba.schereg.model.PersistedSession;

public interface PersistedSessionService {
	public List<PersistedSession> findAllByUser(String entityId, String userId);
	public List<PersistedSession> findExpiredSessions(DateTime cutoffTimestamp);
	
	public PersistedSession access(String entityId, String httpSessionId, String userId);
	public PersistedSession accessOrCreate(String entityId, String httpSessionId, String userId) throws GenericScheregException;
	
	public PersistedSession createAndSaveSession(String entityId, String httpSessionId, String userId) throws GenericScheregException;
	public PersistedSession reassignPersistedSession(String httpSessionId, String userId, String persistedSessionId);
	public PersistedSession saveSession(PersistedSession session);
	
	public void deleteSession(String entityId, String httpSessionId, String userId);
	public void deleteSession(PersistedSession session);
	public void deleteSessions(List<PersistedSession> sessions);	
}