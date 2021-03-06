package de.unibamberg.minf.dme.service.base;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import de.unibamberg.minf.dme.dao.interfaces.ChangeSetDao;
import de.unibamberg.minf.dme.model.tracking.ChangeSet;

public abstract class BaseServiceImpl implements BaseService {
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired protected ChangeSetDao changeSetDao;
	
	public static String getNormalizedName(String label) {
		return label.substring(0,1).toUpperCase() + label.substring(1);
	}
	
	@Override
	public List<ChangeSet> getChangeSetForEntity(String entityId) {
		return changeSetDao.findByEntityId(entityId);
	}
	
	@Override
	public ChangeSet getLatestChangeSetForEntity(String id) {
		return changeSetDao.findLatestByEntityId(id);
	}
	
	@Override
	public List<ChangeSet> getChangeSetForElement(String elementId) {
		return changeSetDao.findByElementId(elementId);
	}
	
	@Override
	public List<ChangeSet> getChangeSetForEntities(List<String> entityIds) {
		return changeSetDao.findByEntityIds(entityIds);
	}
	
	@Override
	public List<ChangeSet> getChangeSetForElements(List<String> elementIds) {
		return changeSetDao.findByElementIds(elementIds);
	}
}