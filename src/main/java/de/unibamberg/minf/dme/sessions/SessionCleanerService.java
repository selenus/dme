package de.unibamberg.minf.dme.sessions;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import de.unibamberg.minf.dme.service.interfaces.PersistedSessionService;

public class SessionCleanerService implements InitializingBean, DisposableBean {
	private static Logger logger = LoggerFactory.getLogger(SessionCleanerService.class);
	
	@Autowired private PersistedSessionService sessionService;
	
	private Timer timer = new Timer(true);
	private ExecutorService executorService = Executors.newFixedThreadPool(1);
	
	private int intervalMins;
	private int defaultExpirationMins;
		
	public int getIntervalMins() { return intervalMins; }
	public void setIntervalMins(int intervalMins) { this.intervalMins = intervalMins; }

	public int getDefaultExpirationMins() { return defaultExpirationMins; }
	public void setDefaultExpirationMins(int defaultExpirationMins) { this.defaultExpirationMins = defaultExpirationMins; }
	

	@Override
	public void afterPropertiesSet() throws Exception {
		DateTime firstRun = DateTime.now().plusMinutes(intervalMins);
		logger.info(String.format("Scheduling SessionCleanerService every %s minutes; first run %s", intervalMins, firstRun));
		timer.scheduleAtFixedRate(new TimerTask() { 
			@Override
			public void run() { 
				runSessionCleanerThread(); 
			}
		}, firstRun.toDate(), intervalMins * 60000);
	}
	
	@Override
	public void destroy() throws Exception {
		try {
			logger.info("Closing SessionCleanerService");
			executorService.shutdown();
			
		    // Wait until all threads are finished
		    while (!executorService.isTerminated()) {}
		} catch (final Exception e) {
			logger.error("Error closing SessionCleanerService", e);
		}
	}
	
	public synchronized void runSessionCleanerThread() {
		try {
			logger.info("Executing session cleaner as scheduled");
			SessionCleanerThread sessionCleanerThread = new SessionCleanerThread(sessionService, defaultExpirationMins);
			executorService.execute(sessionCleanerThread);
		} catch (Exception e) {
			logger.error("An error occurred while executing schedule for synchronization with Collection Registry");
		}
	}
}
