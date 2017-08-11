package eu.dariah.de.minfba.schereg.dao;

import java.util.List;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.mongodb.WriteResult;

import de.unibamberg.minf.dme.model.base.Function;
import eu.dariah.de.minfba.schereg.dao.base.TrackedEntityDaoImpl;
import eu.dariah.de.minfba.schereg.dao.interfaces.FunctionDao;

@Repository
public class FunctionDaoImpl extends TrackedEntityDaoImpl<Function> implements FunctionDao {
	public FunctionDaoImpl() {
		super(Function.class);
	}
	
	@Override
	public List<Function> findByEntityId(String entityId) {		
		Query q = Query.query(Criteria.where("entityId").is(entityId));
		return this.find(q);
	}
	
	@Override
	public int deleteAll(String entityId) {
		WriteResult result = mongoTemplate.remove(Query.query(Criteria.where(ENTITY_ID_FIELD).is(entityId)), this.getCollectionName());
		return result.getN();
	}
}
