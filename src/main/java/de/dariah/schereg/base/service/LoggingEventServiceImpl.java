package de.dariah.schereg.base.service;

import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import de.dariah.schereg.base.dao.LoggingEventDao;
import de.dariah.schereg.base.model.LoggingEvent;

@Service
@Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
public class LoggingEventServiceImpl implements LoggingEventService {

	@Autowired
	private LoggingEventDao loggingEventDao;
	
	@Override
	public Collection<LoggingEvent> getLatest(int count) {
		return loggingEventDao.getLatest(count);
	}

	@Override
	public Collection<LoggingEvent> listNewerLogEntries(int displayedItemCount, int currentLatestId) {
		return loggingEventDao.getLatest(displayedItemCount, currentLatestId);
	}

	@Override
	public LoggingEvent getLogEntry(int id) {
		return loggingEventDao.findById(id);
	}
}
