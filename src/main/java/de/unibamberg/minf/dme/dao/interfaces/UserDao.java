package de.unibamberg.minf.dme.dao.interfaces;

import de.unibamberg.minf.dme.dao.base.BaseDao;
import eu.dariah.de.dariahsp.model.User;

public interface UserDao extends BaseDao<User> {
	public User findByUsername(String domain, String username);
}