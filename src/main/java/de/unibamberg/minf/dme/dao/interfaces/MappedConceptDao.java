package de.unibamberg.minf.dme.dao.interfaces;

import java.util.List;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import de.unibamberg.minf.dme.dao.base.TrackedEntityDao;
import de.unibamberg.minf.dme.model.base.Element;
import de.unibamberg.minf.dme.model.mapping.base.MappedConcept;

public interface MappedConceptDao extends TrackedEntityDao<MappedConcept> {
	
	public List<MappedConcept> findByEntityId(String entityId);
}
