package de.dariah.schereg.base.dao.impl;

import java.util.ArrayList;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import de.dariah.base.dao.base.BaseEntityDaoImpl;
import de.dariah.schereg.base.dao.UserLogEntryDao;
import de.dariah.schereg.base.model.UserLogEntry;

@Repository
public class UserLogEntryDaoImpl extends BaseEntityDaoImpl<UserLogEntry> implements UserLogEntryDao {

	public UserLogEntryDaoImpl() {
		super(UserLogEntry.class);
	}

	@Override
	public UserLogEntry findBySessionId(String sessionId) {
		
		ArrayList<Criterion> cr = new ArrayList<Criterion>();
		cr.add(Restrictions.eq("sessionId", sessionId));

		try {
			return super.findByCriteriaDistinct(new ArrayList<Criterion>(cr));
		} catch (Exception e) {
			logger.error("Exception occured while attempting to load UserLogEntry by SessionId", e);
			return null;
		}		
	}

	@Override
	public UserLogEntry findById(Integer id) {
		return super.findById(id);
	}
	

}
