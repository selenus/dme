package eu.dariah.de.minfba.schereg.dao;

import org.springframework.stereotype.Repository;

import eu.dariah.de.minfba.schereg.dao.base.BaseDaoImpl;
import eu.dariah.de.minfba.schereg.dao.interfaces.PersistedSessionDao;
import eu.dariah.de.minfba.schereg.model.PersistedSession;

@Repository
public class PersistedSessionDaoImpl extends BaseDaoImpl<PersistedSession> implements PersistedSessionDao {
	public PersistedSessionDaoImpl() {
		super(PersistedSession.class);
	}
}
