package de.unibamberg.minf.dme.dao.interfaces;

import de.unibamberg.minf.dme.dao.base.RightsAssignedObjectDao;
import de.unibamberg.minf.dme.model.mapping.base.Mapping;

public interface MappingDao extends RightsAssignedObjectDao<Mapping> {
	public void updateSourceModel(String currentSourceId, String newSourceId);
	public void updateTargetModel(String currentTargetId, String newTargetId);
}
