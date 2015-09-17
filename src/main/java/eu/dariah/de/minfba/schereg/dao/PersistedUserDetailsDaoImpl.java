package eu.dariah.de.minfba.schereg.dao;

import eu.dariah.de.minfba.schereg.dao.base.BaseDaoImpl;
import eu.dariah.de.minfba.schereg.dao.interfaces.PersistedUserDetailsDao;
import eu.dariah.de.minfba.schereg.model.PersistedUserDetails;

public class PersistedUserDetailsDaoImpl extends BaseDaoImpl<PersistedUserDetails> implements PersistedUserDetailsDao {
	public PersistedUserDetailsDaoImpl() {
		super(PersistedUserDetails.class);
	}
}
