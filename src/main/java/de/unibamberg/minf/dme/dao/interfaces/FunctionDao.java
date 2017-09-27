package de.unibamberg.minf.dme.dao.interfaces;

import java.util.List;

import de.unibamberg.minf.dme.dao.base.TrackedEntityDao;
import de.unibamberg.minf.dme.model.base.Function;

public interface FunctionDao extends TrackedEntityDao<Function> {
	public List<Function> findByEntityId(String entityId);
	
	public int deleteAll(String entityId);
}
