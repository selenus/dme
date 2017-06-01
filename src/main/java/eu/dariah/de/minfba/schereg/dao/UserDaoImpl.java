package eu.dariah.de.minfba.schereg.dao;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import eu.dariah.de.dariahsp.model.User;
import eu.dariah.de.minfba.schereg.dao.base.BaseDaoImpl;
import eu.dariah.de.minfba.schereg.dao.interfaces.UserDao;

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