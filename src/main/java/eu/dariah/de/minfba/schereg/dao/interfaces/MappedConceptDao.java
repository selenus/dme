package eu.dariah.de.minfba.schereg.dao.interfaces;

import java.util.List;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import eu.dariah.de.minfba.core.metamodel.interfaces.Element;
import eu.dariah.de.minfba.core.metamodel.interfaces.MappedConcept;
import eu.dariah.de.minfba.schereg.dao.base.TrackedEntityDao;

public interface MappedConceptDao extends TrackedEntityDao<MappedConcept> {
	
	public List<MappedConcept> findByEntityId(String entityId);
}
