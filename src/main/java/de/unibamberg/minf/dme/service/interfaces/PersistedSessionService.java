package de.unibamberg.minf.dme.service.interfaces;

import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.springframework.context.MessageSource;

import de.unibamberg.minf.dme.exception.GenericScheregException;
import de.unibamberg.minf.dme.model.PersistedSession;

public interface PersistedSessionService {
	public List<PersistedSession> findAllByUser(String entityId, String userId);
	public List<PersistedSession> findExpiredSessions(DateTime cutoffTimestamp);
	
	public PersistedSession access(String entityId, String httpSessionId, String userId);
	public PersistedSession accessOrCreate(String entityId, String httpSessionId, String userId, MessageSource messageSource, Locale locale) throws GenericScheregException;
	
	public PersistedSession reassignPersistedSession(String httpSessionId, String userId, String persistedSessionId);
	public PersistedSession saveSession(PersistedSession session);
	
	public void deleteSession(String entityId, String httpSessionId, String userId);
	public void deleteSession(PersistedSession session);
	public void deleteSessions(List<PersistedSession> sessions);
	public PersistedSession createAndSaveSession(String entityId, String httpSessionId, String userId, MessageSource messageSource, Locale locale) throws GenericScheregException;	

	public String getSampleInputValue(PersistedSession s, String functionId);
}