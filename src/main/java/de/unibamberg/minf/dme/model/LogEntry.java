package de.unibamberg.minf.dme.model;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class LogEntry implements Comparable<LogEntry> {
	public static enum LogType { SUCCESS, INFO, WARNING, ERROR }
	
	public static LogEntry createEntry(LogType logType, String messageCode, Object[] args, String defaultMessage) {
		LogEntry entry = new LogEntry();
		entry.logType = logType;
		entry.messageCode = messageCode;
		entry.args = args;
		entry.defaultMessage = defaultMessage;
		entry.setTimestamp(DateTime.now());
		return entry;
	}
	
	private DateTime timestamp;
	private LogType logType;
	private String defaultMessage;
	private String messageCode;
	private Object[] args;
	
	public DateTime getTimestamp() { return timestamp; }
	public void setTimestamp(DateTime timestamp) { this.timestamp = timestamp; }
	
	public LogType getLogType() { return logType; }
	public void setLogType(LogType logType) { this.logType = logType; }
	
	protected Object[] getArgs() { return args; }
	protected void setArgs(Object[] args) { this.args = args; }
	
	public String getDefaultMessage() { return defaultMessage; }
	public void setDefaultMessage(String defaultMessage) { this.defaultMessage = defaultMessage; }
	
	protected String getMessageCode() { return messageCode; }
	protected void setMessageCode(String messageCode) { this.messageCode = messageCode; }
	
	
	public long getNumericTimestamp() {
		return timestamp.getMillis();
	}
	
	public String getDisplayTimestamp() {
		return timestamp.toString("yyyy-MM-dd HH:mm:ss.SSS");
	}
	
	@Override
	public int compareTo(LogEntry o) {
		return this.timestamp.compareTo(o.getTimestamp());
	}
}