package de.unibamberg.minf.dme.dao;

import org.springframework.stereotype.Repository;

import de.unibamberg.minf.dme.dao.base.BaseDaoImpl;
import de.unibamberg.minf.dme.dao.interfaces.PersistedSessionDao;
import de.unibamberg.minf.dme.model.PersistedSession;

@Repository
public class PersistedSessionDaoImpl extends BaseDaoImpl<PersistedSession> implements PersistedSessionDao {
	public PersistedSessionDaoImpl() {
		super(PersistedSession.class);
	}
}
