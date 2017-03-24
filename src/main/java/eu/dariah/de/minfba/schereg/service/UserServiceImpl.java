package eu.dariah.de.minfba.schereg.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import eu.dariah.de.dariahsp.model.User;
import eu.dariah.de.dariahsp.service.BaseUserService;
import eu.dariah.de.minfba.schereg.dao.interfaces.UserDao;

@Service
public class UserServiceImpl extends BaseUserService {
	@Autowired private UserDao userDetailsDao;
	
	@Override
	public User loadUserByUsername(String domain, String username) throws UsernameNotFoundException {
		return userDetailsDao.findByUsername(domain, username);
	}

	@Override
	protected void innerSaveUser(User persistedUser) {
		userDetailsDao.save(persistedUser);
	}
}
