package eu.dariah.de.minfba.schereg.dao.interfaces;

import java.util.List;

import eu.dariah.de.minfba.core.metamodel.tracking.ChangeSet;
import eu.dariah.de.minfba.schereg.dao.base.BaseDao;

public interface ChangeSetDao extends BaseDao<ChangeSet> {
	public ChangeSet findOneByIds(String sessionId, String entityId, String elementId);
	
	public List<ChangeSet> findByEntityId(String entityId);
	public List<ChangeSet> findByElementId(String elementId);
	public List<ChangeSet> findByEntityIds(List<String> entityIds);
	public List<ChangeSet> findByElementIds(List<String> elementIds);

	public ChangeSet findLatestByEntityId(String id);
}
