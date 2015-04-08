package eu.dariah.de.minfba.schereg.dao;

import java.util.Map;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import com.mongodb.WriteResult;

import eu.dariah.de.minfba.schereg.dao.base.BaseDaoImpl;
import eu.dariah.de.minfba.schereg.serialization.Reference;

@Repository
public class ReferenceDaoImpl extends BaseDaoImpl<Reference> implements ReferenceDao {
	public ReferenceDaoImpl() {
		super(Reference.class);
	}

	@Override
	public void deleteAll(Map<String, Reference[]> idMap) throws IllegalArgumentException, ClassNotFoundException {
		if (idMap==null) {
			return;
		}
		
		Query deleteQuery;
		Reference[] deleteReferences;
		Object[] deleteIds;
		Class<?> clazz;
		for (String type : idMap.keySet()) {
			deleteReferences = idMap.get(type);
			if (deleteReferences!=null && deleteReferences.length>0) {
				clazz = Class.forName(type);
				deleteIds = new String[deleteReferences.length];
				for (int i=0; i<deleteReferences.length; i++) {
					Assert.isTrue(this.isValidObjectId(deleteReferences[i].getId()));
					deleteIds[i] = deleteReferences[i].getId();
				}
				
				deleteQuery = new Query();
				deleteQuery.addCriteria(Criteria.where("_id").in(deleteIds));
				
				WriteResult result = getMongoTemplate().remove(deleteQuery, clazz, clazz.getSimpleName());
				
				logger.info("Removed {} {} entities in consequence of a delete cascade", result.getN(), clazz.getSimpleName());
			}
		}
	}
}
