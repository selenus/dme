package eu.dariah.de.minfba.schereg.dao.interfaces;

import java.util.List;

import de.unibamberg.minf.dme.model.base.Function;
import eu.dariah.de.minfba.schereg.dao.base.TrackedEntityDao;

public interface FunctionDao extends TrackedEntityDao<Function> {
	public List<Function> findByEntityId(String entityId);
	
	public int deleteAll(String entityId);
}
