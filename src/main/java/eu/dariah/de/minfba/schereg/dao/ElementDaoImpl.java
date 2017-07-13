package eu.dariah.de.minfba.schereg.dao;

import java.util.List;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.mongodb.WriteResult;

import eu.dariah.de.minfba.core.metamodel.interfaces.Element;
import eu.dariah.de.minfba.schereg.dao.base.TrackedEntityDaoImpl;
import eu.dariah.de.minfba.schereg.dao.interfaces.ElementDao;

@Repository
public class ElementDaoImpl extends TrackedEntityDaoImpl<Element> implements ElementDao {
	public ElementDaoImpl() {
		super(Element.class);
	}

	@Override
	public List<Element> findByEntityId(String entityId) {		
		Query q = Query.query(Criteria.where("entityId").is(entityId));
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