package de.unibamberg.minf.dme.service.base;

import java.util.List;

import de.unibamberg.minf.dme.model.tracking.ChangeSet;

public interface BaseService {
	public List<ChangeSet> getChangeSetForEntity(String entityId);
	public List<ChangeSet> getChangeSetForElement(String elementId);
	public List<ChangeSet> getChangeSetForEntities(List<String> entityIds);
	public List<ChangeSet> getChangeSetForElements(List<String> elementIds);
	public ChangeSet getLatestChangeSetForEntity(String id);
}
