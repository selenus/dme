package eu.dariah.de.minfba.schereg.dao.interfaces;

import java.util.List;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import de.unibamberg.minf.dme.model.base.Element;
import de.unibamberg.minf.dme.model.mapping.base.MappedConcept;
import eu.dariah.de.minfba.schereg.dao.base.TrackedEntityDao;

public interface MappedConceptDao extends TrackedEntityDao<MappedConcept> {
	
	public List<MappedConcept> findByEntityId(String entityId);
}
