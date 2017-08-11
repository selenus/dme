package eu.dariah.de.minfba.schereg.dao.interfaces;

import java.util.List;

import de.unibamberg.minf.dme.model.base.Grammar;
import eu.dariah.de.minfba.schereg.dao.base.TrackedEntityDao;

public interface GrammarDao extends TrackedEntityDao<Grammar> {
	public List<Grammar> findByEntityId(String entityId);
	
	public int deleteAll(String entityId);
}