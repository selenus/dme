package de.unibamberg.minf.dme.dao.base;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import de.unibamberg.minf.dme.model.base.ModelElement;

public class ModelElementDaoImpl<T extends ModelElement> extends TrackedEntityDaoImpl<T> implements ModelElementDao<T> {

	public ModelElementDaoImpl(Class<?> clazz) {
		super(clazz);
	}
	
	public ModelElementDaoImpl(Class<?> clazz, String collectionName) {
		super(clazz, collectionName);
	}

	@Override
	public void updateEntityId(String currentEntityId, String newEntityId) {
		mongoTemplate.updateMulti(
				Query.query(Criteria.where(ENTITY_ID_FIELD).is(currentEntityId)), 
				Update.update(ENTITY_ID_FIELD, newEntityId), 
				this.getCollectionName());
	}
}
