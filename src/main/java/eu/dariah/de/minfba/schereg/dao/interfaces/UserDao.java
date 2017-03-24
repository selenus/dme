package eu.dariah.de.minfba.schereg.dao.interfaces;

import eu.dariah.de.dariahsp.model.User;
import eu.dariah.de.minfba.schereg.dao.base.BaseDao;

public interface UserDao extends BaseDao<User> {
	public User findByUsername(String domain, String username);
}