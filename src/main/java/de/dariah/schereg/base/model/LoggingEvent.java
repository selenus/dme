package de.dariah.schereg.base.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.joda.time.DateTime;

import de.dariah.base.model.base.Identifiable;

@Entity
@Table(name="logging_event")
public class LoggingEvent implements Identifiable {

	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
	@Column(name="event_id")
	private int id;
	
	@Column(name="formatted_message")
	private String message;
	
	@Column(name="timestmp")
	private long timestamp;
	
	@Column(name="level_string")
	private String level;
	
	@Column(name="caller_class")
	private String className;
	
	@Column(name="caller_method")
	private String methodName;
	
	@Column(name="caller_line")
	private int lineNumber;
	
	@Transient
	private String stackTrace;
	
	@Override public int getId() { return this.id; } 
	@Override public void setId(int id) { this.id = id; }

	public String getMessage() { return message; }
	public void setMessage(String message) { this.message = message; }

	public long getTimestamp() { return timestamp; }
	public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

	public DateTime getDateTime() { return new DateTime(timestamp); }
	
	public String getLevel() { return level; }
	public void setLevel(String level) { this.level = level; }

	public String getClassName() { return className; }
	public void setClassName(String className) { this.className = className; }

	public String getMethodName() { return methodName; }
	public void setMethodName(String methodName) { this.methodName = methodName; }

	public int getLineNumber() { return lineNumber; }
	public void setLineNumber(int lineNumber) { this.lineNumber = lineNumber; }

	public String getStackTrace() { return stackTrace; }
	public void setStackTrace(String stackTrace) { this.stackTrace = stackTrace; }	
}

