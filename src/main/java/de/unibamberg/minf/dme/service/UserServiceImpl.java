package de.unibamberg.minf.dme.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import de.unibamberg.minf.dme.dao.interfaces.PersistedSessionDao;
import de.unibamberg.minf.dme.dao.interfaces.UserDao;
import de.unibamberg.minf.dme.model.PersistedSession;
import eu.dariah.de.dariahsp.model.User;
import eu.dariah.de.dariahsp.service.BaseUserService;

@Service
public class UserServiceImpl extends BaseUserService {
	protected static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
			
	@Autowired private UserDao userDetailsDao;
	@Autowired private PersistedSessionDao sessionDao;
	
	@Override
	public User loadUserByUsername(String domain, String username) throws UsernameNotFoundException {
		return userDetailsDao.findByUsername(domain, username);
	}

	@Override
	protected void innerSaveUser(User persistedUser) {
		userDetailsDao.save(persistedUser);
		
		this.cleanEmptySessions(persistedUser.getId());
	}
	
	private void cleanEmptySessions(String userId) {
		
		List<PersistedSession> emptySessions = sessionDao.find(Query.query(Criteria.where("userId").is(userId).and("sampleInput").is(null).and("label").exists(false)));
		
		if (emptySessions!=null && emptySessions.size()>0) {
			try {
				sessionDao.delete(emptySessions);
				logger.debug("Deleted {} empty sessions for user {}", emptySessions.size(), userId);
			} catch (Exception e) {
				logger.error("Failed to delete empty sessions for user {}", userId);
			}
		}
	}
}
