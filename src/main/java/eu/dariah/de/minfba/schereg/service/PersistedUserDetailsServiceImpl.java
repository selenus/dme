package eu.dariah.de.minfba.schereg.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import eu.dariah.de.minfba.schereg.dao.interfaces.PersistedUserDetailsDao;
import eu.dariah.de.minfba.schereg.model.PersistedUserDetails;
import eu.dariah.de.minfba.schereg.service.interfaces.PersistedUserDetailsService;

@Service
public class PersistedUserDetailsServiceImpl implements PersistedUserDetailsService {
	@Autowired private PersistedUserDetailsDao userDetailsDao;
	
	@Override
	public PersistedUserDetails loadUserByUsername(String domain, String username) throws UsernameNotFoundException {
		return userDetailsDao.findByUsername(domain, username);
	}

	@Override
	public void saveUser(PersistedUserDetails persistedUser) {
		userDetailsDao.save(persistedUser);
	}
}
