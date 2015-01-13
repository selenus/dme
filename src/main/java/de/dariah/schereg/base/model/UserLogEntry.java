package de.dariah.schereg.base.model;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import de.dariah.base.model.base.BaseEntityImpl;
import de.dariah.base.model.base.Identifiable;

@javax.persistence.Entity
@Table(name="user_log")
public class UserLogEntry implements Identifiable {

	@Id
	@TableGenerator(name = "user_log_entry_gen", table="universal_id", valueColumnName="id", allocationSize=1, pkColumnName="sequence_name", pkColumnValue="userlog")
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "user_log_entry_gen")
	private int id;
	
	private boolean authenticated;
	private String remoteAddress;
	private String sessionId;
	private String username;
	private String dn;
	private String exception;
	
	@Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime loggedIn;
	
	@Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime loggedOut;
	
		
	@Override
	public int getId() { return id; }
	@Override
	public void setId(int id) { this.id = id; }

	public boolean isAuthenticated() { return authenticated; }
	public void setAuthenticated(boolean authenticated) { this.authenticated = authenticated; }

	public String getRemoteAddress() { return remoteAddress; }
	public void setRemoteAddress(String remoteAddress) { this.remoteAddress = remoteAddress; }

	public String getSessionId() { return sessionId; }
	public void setSessionId(String sessionId) { this.sessionId = sessionId; }

	public String getUsername() { return username; }
	public void setUsername(String username) { this.username = username; }

	public String getDn() { return dn; }
	public void setDn(String dn) { this.dn = dn; }

	public String getException() { return exception; }
	public void setException(String exception) { this.exception = exception; }

	public DateTime getLoggedIn() { return loggedIn; }
	public void setLoggedIn(DateTime loggedIn) { this.loggedIn = loggedIn; }

	public DateTime getLoggedOut() { return loggedOut; }
	public void setLoggedOut(DateTime loggedOut) { this.loggedOut = loggedOut; }
}
