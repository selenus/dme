package de.unibamberg.minf.dme.dao;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import de.unibamberg.minf.dme.dao.base.RightsAssignedObjectDaoImpl;
import de.unibamberg.minf.dme.dao.interfaces.MappingDao;
import de.unibamberg.minf.dme.model.RightsContainer;
import de.unibamberg.minf.dme.model.mapping.base.Mapping;

@Repository
public class MappingDaoImpl extends RightsAssignedObjectDaoImpl<Mapping> implements MappingDao {
	public MappingDaoImpl() {
		super(new RightsContainer<Mapping>().getClass(), "mapping");
	}
	
	
	@Override
	public void updateSourceModel(String currentSourceId, String newSourceId) {
		mongoTemplate.updateMulti(
				Query.query(Criteria.where("element.sourceId").is(currentSourceId)), 
				Update.update("element.sourceId", newSourceId), 
				this.getCollectionName());
	}
	
	@Override
	public void updateTargetModel(String currentTargetId, String newTargetId) {
		mongoTemplate.updateMulti(
				Query.query(Criteria.where("element.targetId").is(currentTargetId)), 
				Update.update("element.targetId", newTargetId), 
				this.getCollectionName());
	}
}
