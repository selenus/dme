package eu.dariah.de.minfba.schereg.sessions;

import java.util.List;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.dariah.de.minfba.schereg.model.PersistedSession;
import eu.dariah.de.minfba.schereg.service.interfaces.PersistedSessionService;

public class SessionCleanerThread implements Runnable {
	private static Logger logger = LoggerFactory.getLogger(SessionCleanerThread.class);
	
	private PersistedSessionService sessionService;
	private int expirationMins;
	
	public SessionCleanerThread(PersistedSessionService sessionService, int expirationMins) {
		this.sessionService = sessionService;
		this.expirationMins = expirationMins;
	}
	
	@Override
	public void run() {
		try {
			DateTime cutoffTimestamp = DateTime.now().minusMinutes(expirationMins);
			List<PersistedSession> deleteSessions = sessionService.findExpiredSessions(cutoffTimestamp);
			if (deleteSessions!=null && deleteSessions.size()>0) {
				logger.info(String.format("Deleting %s expired sessions", deleteSessions.size()));
				sessionService.deleteSessions(deleteSessions);
			} else {
				logger.info("No expired sessions found");
			}
		} catch (Exception e) {
			logger.error("Failed to execute SessionCleanerThread", e);
		}
	}
}
