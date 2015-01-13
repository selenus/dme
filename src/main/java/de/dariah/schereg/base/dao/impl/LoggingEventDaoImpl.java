package de.dariah.schereg.base.dao.impl;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;

import de.dariah.base.dao.base.BaseEntityDaoImpl;
import de.dariah.schereg.base.dao.LoggingEventDao;
import de.dariah.schereg.base.model.LoggingEvent;

@Repository
public class LoggingEventDaoImpl extends BaseEntityDaoImpl<LoggingEvent>
		implements LoggingEventDao {

	public LoggingEventDaoImpl() {
		super(LoggingEvent.class);
	}

	public List<LoggingEvent> getLatest(int count) {
		
		Criteria criteria = getCurrentSession()
				.createCriteria(LoggingEvent.class).setFirstResult(0)
				.setMaxResults(count)
				.addOrder(Order.desc("timestamp"));

		return cast(criteria.list());
	}
	
	public List<LoggingEvent> getLatest(int count, int laterThanExcludedId) {
		
		Criteria criteria = getCurrentSession()
				.createCriteria(LoggingEvent.class).setFirstResult(0)
				.setMaxResults(count)
				.add(Restrictions.gt("id", laterThanExcludedId))
				.addOrder(Order.desc("timestamp"));

		return cast(criteria.list());
	}
}
