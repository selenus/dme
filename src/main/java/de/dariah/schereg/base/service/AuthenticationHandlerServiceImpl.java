package de.dariah.schereg.base.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.ldap.userdetails.LdapUserDetailsImpl;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import de.dariah.schereg.base.dao.UserLogEntryDao;
import de.dariah.schereg.base.model.UserLogEntry;
import de.dariah.schereg.util.MessageContextHolder;

/* Not annotation configured due to required bean-ref in security-context configuration */
/**
 * Implementation of various handlers in the context of user authentication. 
 * Service to the UserLog containing information on user-login and -logout as well as failed attempts.
 *  
 * @author Tobias Gradl
 */
@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
public class AuthenticationHandlerServiceImpl implements AuthenticationSuccessHandler, AuthenticationFailureHandler, LogoutSuccessHandler {
	
	private static final Logger logger = LoggerFactory.getLogger(AuthenticationHandlerServiceImpl.class);
	
	UserLogEntryDao userLogEntryDao; 
	
    public UserLogEntryDao getUserLogEntryDao() { return userLogEntryDao; }
	public void setUserLogEntryDao(UserLogEntryDao userLogEntryDao) { this.userLogEntryDao = userLogEntryDao; }


	/**
	 * Handles successful authentication request
	 * 
	 * 
	 * @param HttpServletRequest request 
	 * @param HttpServletResponse response 
	 * @param Authentication authentication
	 * 			The authentication includes information on the Principal and Authentication-Details. 
	 * 			Make sure that the mechanisms match the expected object (e.g. ) 
	 */
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        String redirect = request.getContextPath() + "/auth/login?error=true";
    	
    	if (authentication.isAuthenticated()) {
    		redirect=request.getContextPath() + "/";
    	}
 
    	try {
    		UserLogEntry logEntry = new UserLogEntry();
    		logEntry.setAuthenticated(authentication.isAuthenticated());
    		
    		if (authentication.getDetails().getClass().equals(WebAuthenticationDetails.class)) {    		
    			logEntry.setSessionId(((WebAuthenticationDetails)authentication.getDetails()).getSessionId());
    			logEntry.setRemoteAddress(((WebAuthenticationDetails)authentication.getDetails()).getRemoteAddress());
    		} else {
    			logger.error("Provided authentication details are invalid. UserLogEntry incomplete.");
    		}
    		
    		if (authentication.getPrincipal().getClass().equals(LdapUserDetailsImpl.class)) {    		
    			logEntry.setDn(((LdapUserDetailsImpl)authentication.getPrincipal()).getDn());
        		logEntry.setUsername(((LdapUserDetailsImpl)authentication.getPrincipal()).getUsername());
        		
        		MessageContextHolder.addMessage("auth.message.loggedin", "auth.message.loggedin", new String[] {logEntry.getUsername()});        		
    		} else {
    			logger.error("Provided authentication principal is invalid. UserLogEntry incomplete.");
    		}
    		
    		logEntry.setLoggedIn(DateTime.now());
    		userLogEntryDao.saveOrUpdate(logEntry);
    		
    		logger.info(String.format("Login successful -> user [%s] from address [%s]", logEntry.getUsername(), logEntry.getRemoteAddress()));
    		
			response.sendRedirect(redirect);
		} catch (Exception e) {
			logger.error("Exception occurred after successful user authentification", e);
		}
        
    }

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) {
    	try {
    		UserLogEntry logEntry = new UserLogEntry();
    		logEntry.setAuthenticated(false);
    		logEntry.setRemoteAddress(request.getRemoteAddr().toString());
      		
    		userLogEntryDao.saveOrUpdate(logEntry);
    		
    		logger.info(String.format("Login failed from address [%s]", logEntry.getRemoteAddress()));
    		
			response.sendRedirect(request.getContextPath() + "/auth/login?error=true");
		} catch (Exception e) {
			logger.error("Exception occurred after failed user authentification", e);
		}
	}

	@Override
	public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
		
		MessageContextHolder.addMessage("auth.message.loggedout", "auth.message.loggedout");     		
		try {
			if (authentication.getDetails().getClass().equals(WebAuthenticationDetails.class)) {    	
				String sessionId = ((WebAuthenticationDetails)authentication.getDetails()).getSessionId();
				UserLogEntry userLogEntry = userLogEntryDao.findBySessionId(sessionId);
				if (userLogEntry != null) {
					userLogEntry.setLoggedOut(DateTime.now());
					
					logger.info(String.format("Logout successful -> user [%s] from address [%s]", userLogEntry.getUsername(), userLogEntry.getRemoteAddress()));
				}
    			
    		} else {
    			logger.error("Provided logout details are invalid. UserLogEntry incomplete.");
    		}
						
			response.sendRedirect(request.getContextPath() + "/auth/login");
		} catch (Exception e) {
			logger.error("Exception occurred after user log-out", e);
		}
		
	}
}
