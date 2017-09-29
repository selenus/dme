package de.unibamberg.minf.dme.model;

import org.joda.time.DateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class LogEntry implements Comparable<LogEntry> {
	public static enum LogType { SUCCESS, INFO, WARNING, ERROR }
	
	public static LogEntry createEntry(LogType logType, String message) {
		LogEntry entry = new LogEntry();
		entry.logType = logType;
		entry.message = message;
		entry.setTimestamp(DateTime.now());
		return entry;
	}
	
	private DateTime timestamp;
	private LogType logType;
	private String message;
	
	@JsonIgnore
	public DateTime getTimestamp() { return timestamp; }
	public void setTimestamp(DateTime timestamp) { this.timestamp = timestamp; }
	
	public LogType getLogType() { return logType; }
	public void setLogType(LogType logType) { this.logType = logType; }
	
	public String getMessage() { return message; }
	public void setMessage(String message) { this.message = message; }
	
	
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