package eu.dariah.de.minfba.schereg.pojo;

import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import eu.dariah.de.minfba.core.metamodel.tracking.Change;

public class ChangeSetPojo {
	private String user;
	private Map<String, List<Change>> changes;
	private DateTime timestamp;
	private int edits;
	private int news;
	private int deletes;
	
	public String getUser() { return user; }
	public void setUser(String user) { this.user = user; }
	
	public Map<String, List<Change>> getChanges() { return changes; }
	public void setChanges(Map<String, List<Change>> changes) { this.changes = changes; }
	
	public DateTime getTimestamp() { return timestamp; }
	public void setTimestamp(DateTime timestamp) { this.timestamp = timestamp; }
	
	public String getTimestampString() { return timestamp==null ? null : timestamp.toString("yyyy-MM-dd HH:mm:ss"); }
	
	public int getEdits() { return edits; }
	public void setEdits(int edits) { this.edits = edits; }
	
	public int getNews() { return news; }
	public void setNews(int news) { this.news = news; }
	
	public int getDeletes() { return deletes; }
	public void setDeletes(int deletes) { this.deletes = deletes; }
}