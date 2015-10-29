package eu.dariah.de.minfba.schereg.dao;

import java.util.List;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import eu.dariah.de.minfba.core.metamodel.interfaces.MappedConcept;
import eu.dariah.de.minfba.schereg.dao.base.TrackedEntityDaoImpl;
import eu.dariah.de.minfba.schereg.dao.interfaces.MappedConceptDao;

@Repository
public class MappedConceptDaoImpl extends TrackedEntityDaoImpl<MappedConcept> implements MappedConceptDao {
	public MappedConceptDaoImpl() {
		super(MappedConcept.class);
	}
	
	@Override
	public List<MappedConcept> findByEntityId(String entityId) {		
		Query q = Query.query(Criteria.where("entityId").is(entityId));
		return this.find(q);
	}
}
