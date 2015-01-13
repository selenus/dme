package de.dariah.schereg.base.service;

import java.util.Collection;

import de.dariah.schereg.base.model.LoggingEvent;

public interface LoggingEventService {
	Collection<LoggingEvent> getLatest(int count);
	Collection<LoggingEvent> listNewerLogEntries(int displayedItemCount, int currentLatestId);
	LoggingEvent getLogEntry(int id);
}
