package de.unibamberg.minf.dme.dao.interfaces;

import java.util.List;

import de.unibamberg.minf.dme.dao.base.ModelElementDao;
import de.unibamberg.minf.dme.model.mapping.base.MappedConcept;

public interface MappedConceptDao extends ModelElementDao<MappedConcept> {
	
	public List<MappedConcept> findByEntityId(String entityId);
}
