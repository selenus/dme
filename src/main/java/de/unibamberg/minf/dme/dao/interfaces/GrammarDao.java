package de.unibamberg.minf.dme.dao.interfaces;

import java.util.List;

import de.unibamberg.minf.dme.dao.base.ModelElementDao;
import de.unibamberg.minf.dme.model.base.Grammar;

public interface GrammarDao extends ModelElementDao<Grammar> {
	public List<Grammar> findByEntityId(String entityId);
	
	public int deleteAll(String entityId);
}