package de.dariah.schereg.service;

import java.io.IOException;
import java.net.InetAddress;
import java.text.NumberFormat;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.NtpV3Packet;
import org.apache.commons.net.ntp.TimeInfo;
import org.apache.commons.net.ntp.TimeStamp;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

/**
 * Service for determining the actual time from NTP Servers. The exact time is especially reqired for the exchange of SAML messages.
 * 	TimeService does not modify the system time. Checks for the NTP time or deltas must manually be triggered. 
 * 
 * @author Tobias Gradl 
 */
@Component
public class TimeService implements InitializingBean, DisposableBean {
	
	private static final Logger logger = LoggerFactory.getLogger(TimeService.class);
	
	private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
	private Timer timer;
	private NTPUDPClient ntpClient = new NTPUDPClient();
	
	private String mainNtpServer;
	private String backupNtpServer;
	
	private int refreshInterval = 3600000; // default: every hour
	private int msOffset; 
	private DateTime lastUpdate;
	
	
	public String getMainNtpServer() { return mainNtpServer; }
	public void setMainNtpServer(String mainNtpServer) { this.mainNtpServer = mainNtpServer; }
	public String getBackupNtpServer() { return backupNtpServer; }
	public void setBackupNtpServer(String backupNtpServer) { this.backupNtpServer = backupNtpServer; }
	public int getRefreshInterval() { return refreshInterval; }
	public void setRefreshInterval(int refreshInterval) { this.refreshInterval = refreshInterval; }
	
	public int getMsOffset() { return msOffset; }
	public DateTime getLastUpdate() { return lastUpdate; }
	public DateTime getNow() { return DateTime.now().plusMillis(getMsOffset()); }
	
	@Override
	public void afterPropertiesSet() throws Exception {
		ntpClient.setDefaultTimeout(10000);
		
		// Run once at startup
		new TimeUpdateTask().run();
		// Then after certain interval (refreshInterval)
		startRefreshCycle();
	}

	@Override
	public void destroy() throws Exception {
		 if (timer != null) {
	     	timer.cancel();
	     }
	}
	
	private void startRefreshCycle() {
		 // Create timer if needed
        if (refreshInterval > 0) {
            logger.debug("Creating metadata reload timer with interval {}", refreshInterval);
            this.timer = new Timer("NTP Time Refresh", true);
            this.timer.schedule(new TimeUpdateTask(), refreshInterval, refreshInterval);
        } else {
        	logger.debug("Metadata reload timer is not created, refreshCheckInternal is {}", refreshInterval);
        }
		
	}
	
    private class TimeUpdateTask extends TimerTask {

        @Override
        public void run() {
        	if (sendNtpRequest(getMainNtpServer()) || sendNtpRequest(getBackupNtpServer())) {
    			logger.info("NTP refresh was successfully competed");
    		} else {
    			logger.error("Could not refresh NTP time...try modifying hosts and check network availability");
    		}
        }

    }
		
	public boolean sendNtpRequest(String ntpServer) {
		this.lock.writeLock().lock();
		try {
			logger.info(String.format("Requesting timestamp from NTP Server [%s]", ntpServer));
			
			if (!ntpClient.isOpen()) {
				ntpClient.open();
			}
			InetAddress ntpServerAddr = InetAddress.getByName(mainNtpServer);
			processNtpResponse(ntpClient.getTime(ntpServerAddr));
			return true;
		} catch (IOException e) {
			logger.error(String.format("Error refreshing timestamp from NTP Server [%s]", ntpServer), e);
			return false;
		} finally {
			ntpClient.close();
			this.lock.writeLock().unlock();
		}
	}
	
    public void processNtpResponse(TimeInfo info)
    {
        NtpV3Packet message = info.getMessage();

        StringBuilder stb = new StringBuilder();
        stb.append("Communication with NTP Server resulted in the following timestamps:\n");
        stb.append(String.format("\t\t- Reference Timestamp:\t%s  %s\n", message.getReferenceTimeStamp(), message.getReferenceTimeStamp().toDateString()));
        stb.append(String.format("\t\t- Originate Timestamp:\t%s  %s\n", message.getOriginateTimeStamp(), message.getOriginateTimeStamp().toDateString()));
        stb.append(String.format("\t\t- Receive Timestamp:\t%s  %s\n", message.getReceiveTimeStamp(), message.getReceiveTimeStamp().toDateString()));
        stb.append(String.format("\t\t- Transmit Timestamp:\t%s  %s\n", message.getTransmitTimeStamp(), message.getTransmitTimeStamp().toDateString()));

        TimeStamp destNtpTime = TimeStamp.getNtpTime(info.getReturnTime());
        stb.append(String.format("\t\t- Transmit Timestamp:\t%s  %s\n", destNtpTime, destNtpTime.toDateString()));
        
        info.computeDetails(); // compute offset/delay if not already done

        stb.append(String.format("\t\t- Roundtrip delay(ms):\t%s\n", info.getDelay()));
        stb.append(String.format("\t\t- New time offset:\t%sms (was %sms)", info.getOffset(), msOffset));
        
        logger.info(stb.toString());
        
        msOffset = info.getOffset().intValue();
        lastUpdate = DateTime.now();
    }
}
