package eu.dariah.de.minfba.schereg.service.interfaces;

import org.springframework.security.core.userdetails.UsernameNotFoundException;

import eu.dariah.de.minfba.schereg.model.PersistedUserDetails;

public interface PersistedUserDetailsService {
	public PersistedUserDetails loadUserByUsername(String domain, String username) throws UsernameNotFoundException;
	public void saveUser(PersistedUserDetails persistedUser);
}
