package de.unibamberg.minf.dme.dao;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import de.unibamberg.minf.dme.dao.base.BaseDaoImpl;
import de.unibamberg.minf.dme.dao.interfaces.UserDao;
import eu.dariah.de.dariahsp.model.User;

@Repository
public class UserDaoImpl extends BaseDaoImpl<User> implements UserDao {
	public UserDaoImpl() {
		super(User.class);
	}

	@Override
	public User findByUsername(String domain, String username) {
		Query q = new Query();
		q.addCriteria(Criteria.where("username").is(username));
		q.addCriteria(Criteria.where("endpointId").is(domain));
		return this.findOne(q);
	}
}
