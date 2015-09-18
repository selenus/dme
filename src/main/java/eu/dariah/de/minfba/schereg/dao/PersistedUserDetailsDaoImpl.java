package eu.dariah.de.minfba.schereg.dao;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import eu.dariah.de.minfba.schereg.dao.base.BaseDaoImpl;
import eu.dariah.de.minfba.schereg.dao.interfaces.PersistedUserDetailsDao;
import eu.dariah.de.minfba.schereg.model.PersistedUserDetails;

@Repository
public class PersistedUserDetailsDaoImpl extends BaseDaoImpl<PersistedUserDetails> implements PersistedUserDetailsDao {
	public PersistedUserDetailsDaoImpl() {
		super(PersistedUserDetails.class);
	}

	@Override
	public PersistedUserDetails findByUsername(String domain, String username) {
		Query q = new Query();
		q.addCriteria(Criteria.where("username").is(username));
		q.addCriteria(Criteria.where("endpointId").is(domain));
		return this.findOne(q);
	}
}
