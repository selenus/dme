package de.unibamberg.minf.dme.dao;

import java.util.List;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import de.unibamberg.minf.dme.dao.base.ModelElementDaoImpl;
import de.unibamberg.minf.dme.dao.interfaces.MappedConceptDao;
import de.unibamberg.minf.dme.model.mapping.base.MappedConcept;

@Repository
public class MappedConceptDaoImpl extends ModelElementDaoImpl<MappedConcept> implements MappedConceptDao {
	public MappedConceptDaoImpl() {
		super(MappedConcept.class);
	}
	
	@Override
	public List<MappedConcept> findByEntityId(String entityId) {		
		Query q = Query.query(Criteria.where("entityId").is(entityId));
		return this.find(q);
	}

	@Override
	public List<MappedConcept> findBySourceElementId(String elementId) {
		/* This is possible because the elementId actually forms the key of a hashmap, which - in JSON - 
		 *  is persisted as a property label - the grammar id being its property value */
		return this.find(Query.query(Criteria.where("elementGrammarIdsMap." + elementId).exists(true)));
	}

	@Override
	public List<MappedConcept> findByTargetElementId(String elementId) {
		return this.find(Query.query(Criteria.where("targetElementIds").in(elementId)));
	}
}
