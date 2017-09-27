package de.unibamberg.minf.dme.dao;

import java.util.List;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.mongodb.WriteResult;

import de.unibamberg.minf.dme.dao.base.TrackedEntityDaoImpl;
import de.unibamberg.minf.dme.dao.interfaces.ElementDao;
import de.unibamberg.minf.dme.model.base.Element;

@Repository
public class ElementDaoImpl extends TrackedEntityDaoImpl<Element> implements ElementDao {
	public ElementDaoImpl() {
		super(Element.class);
	}

	@Override
	public List<Element> findByEntityId(String entityId) {		
		Query q = Query.query(Criteria.where(ENTITY_ID_FIELD).is(entityId));
		return this.find(q);
	}

	@Override
	public int deleteAll(String entityId) {
		WriteResult result = mongoTemplate.remove(Query.query(Criteria.where(ENTITY_ID_FIELD).is(entityId)), this.getCollectionName());
		return result.getN();
	}
	
	@Override
	public void updateByQuery(Query query, Update update) {
		mongoTemplate.updateMulti(query, update, this.getCollectionName());
	}

}