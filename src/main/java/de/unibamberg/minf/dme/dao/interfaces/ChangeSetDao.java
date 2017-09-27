package de.unibamberg.minf.dme.dao.interfaces;

import java.util.List;

import de.unibamberg.minf.dme.dao.base.BaseDao;
import de.unibamberg.minf.dme.model.tracking.ChangeSet;

public interface ChangeSetDao extends BaseDao<ChangeSet> {
	public ChangeSet findOneByIds(String sessionId, String entityId, String elementId);
	
	public List<ChangeSet> findByEntityId(String entityId);
	public List<ChangeSet> findByElementId(String elementId);
	public List<ChangeSet> findByEntityIds(List<String> entityIds);
	public List<ChangeSet> findByElementIds(List<String> elementIds);

	public ChangeSet findLatestByEntityId(String id);
}
