package de.dariah.schereg.base.dao;

import java.util.List;

import de.dariah.base.dao.base.BaseEntityDao;
import de.dariah.schereg.base.model.LoggingEvent;

public interface LoggingEventDao extends BaseEntityDao<LoggingEvent> {
	public List<LoggingEvent> getLatest(int count);
	public List<LoggingEvent> getLatest(int count, int laterThanExcludedId);
}
