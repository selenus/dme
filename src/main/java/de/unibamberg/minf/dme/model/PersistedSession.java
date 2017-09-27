package de.unibamberg.minf.dme.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;

import de.unibamberg.minf.dme.model.base.BaseIdentifiable;
import de.unibamberg.minf.dme.pojo.LogEntryPojo;
import de.unibamberg.minf.dme.pojo.LogEntryPojo.LogType;
import de.unibamberg.minf.processing.model.base.Resource;

/**
 * Persisted session
 * 
 * @todo sampleInput, sampleOutput and selectedValueMap are specific for schema modeling; validate specialize for mappings 
 * 
 * @author tobias
 */
public class PersistedSession extends BaseIdentifiable implements Comparable<PersistedSession> {
	private static final long serialVersionUID = 6949914982830008951L;

	private String httpSessionId;
	private String entityId;
	private String userId;
	private String label;
	private DateTime lastAccessed;
	private DateTime created;
	private boolean notExpiring;
	private SessionSampleFile sampleFile;
	
	private String sampleInput;
	private List<Resource> sampleOutput;
	private List<Resource> sampleMapped;
	private int selectedOutputIndex;
	private Map<String, String> selectedValueMap;
	
	private List<LogEntryPojo> sessionLog;
	
	
	public String getHttpSessionId() { return httpSessionId; }
	public void setHttpSessionId(String httpSessionId) { this.httpSessionId = httpSessionId; }

	public String getEntityId() { return entityId; }
	public void setEntityId(String entityId) { this.entityId = entityId; }
	
	public String getUserId() { return userId; }
	public void setUserId(String userId) { this.userId = userId; }
	
	public String getLabel() { return label; }
	public void setLabel(String label) { this.label = label; }
	
	public DateTime getLastAccessed() { return lastAccessed; }
	public void setLastAccessed(DateTime lastAccessed) { this.lastAccessed = lastAccessed; }
	
	public DateTime getCreated() { return created; }
	public void setCreated(DateTime created) { this.created = created; }
	
	public boolean isNotExpiring() { return notExpiring; }
	public void setNotExpiring(boolean notExpiring) { this.notExpiring = notExpiring; }
	
	public String getSampleInput() { return sampleInput; }
	public void setSampleInput(String sampleInput) { this.sampleInput = sampleInput; }

	public List<Resource> getSampleOutput() { return sampleOutput; }
	public void setSampleOutput(List<Resource> sampleOutput) { this.sampleOutput = sampleOutput; }

	public List<Resource> getSampleMapped() { return sampleMapped; }
	public void setSampleMapped(List<Resource> sampleMapped) { this.sampleMapped = sampleMapped; }
	
	public int getSelectedOutputIndex() { return selectedOutputIndex; }
	public void setSelectedOutputIndex(int selectedOutputIndex) { this.selectedOutputIndex = selectedOutputIndex; }
	
	public Map<String, String> getSelectedValueMap() { return selectedValueMap; }
	public void setSelectedValueMap(Map<String, String> selectedValueMap) { this.selectedValueMap = selectedValueMap; }

	public List<LogEntryPojo> getSessionLog() { return sessionLog; }
	public void setSessionLog(List<LogEntryPojo> sessionLog) { this.sessionLog = sessionLog; }
	
	public SessionSampleFile getSampleFile() { return sampleFile; }
	public void setSampleFile(SessionSampleFile sampleFile) { this.sampleFile = sampleFile; }
	
	
	public List<LogEntryPojo> getSortedSessionLog() {
		List<LogEntryPojo> result = this.getSessionLog();
		if (result!=null) {
			Collections.sort(result);
			Collections.reverse(result);
		}
		return result;
	}
	
	public void addLogEntry(LogType type, String message) {
		LogEntryPojo entry = new LogEntryPojo();
		entry.setTimestamp(DateTime.now());
		entry.setLogType(type);
		entry.setMessage(message);
		
		if (this.getSessionLog()==null) {
			this.setSessionLog(new ArrayList<LogEntryPojo>());
		}
		this.getSessionLog().add(entry);
	}
	
	@Override
	public int compareTo(PersistedSession arg0) {
		return this.lastAccessed.compareTo(arg0.getLastAccessed());
	}
}