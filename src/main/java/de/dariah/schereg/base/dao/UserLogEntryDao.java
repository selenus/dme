package de.dariah.schereg.base.dao;

import java.util.List;

import de.dariah.schereg.base.model.UserLogEntry;

public interface UserLogEntryDao {
	public List<UserLogEntry> findAll() ;
	public UserLogEntry findById(Integer id);
	public UserLogEntry findBySessionId(String sessionId);
	public UserLogEntry saveOrUpdate(UserLogEntry userLogEntry);
}
