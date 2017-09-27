package de.unibamberg.minf.dme.service.base;

import org.springframework.beans.factory.annotation.Autowired;

import de.unibamberg.minf.dme.dao.base.DaoImpl;
import de.unibamberg.minf.dme.dao.interfaces.MappingDao;
import de.unibamberg.minf.dme.dao.interfaces.SchemaDao;
import de.unibamberg.minf.dme.model.RightsContainer;
import de.unibamberg.minf.dme.model.tracking.TrackedEntity;

public class BaseEntityServiceImpl extends BaseReferenceServiceImpl implements BaseEntityService {
	@Autowired protected MappingDao mappingDao;
	@Autowired protected SchemaDao schemaDao;
	
	@Override
	public boolean getUserCanWriteEntity(String entityId, String userId) {
		/* User is logged in (has an ID) and creates a new entity (no ID) */
		if (DaoImpl.isNewId(entityId) && (userId!=null && !userId.isEmpty()) ) {
			return true;
		}		
		return this.getUserCanWriteEntity(this.getRightsContainer(entityId, userId), userId);
	}
	
	@Override
	public boolean getUserCanShareEntity(String entityId, String userId) {
		if (DaoImpl.isNewId(entityId) && (userId!=null && !userId.isEmpty()) ) {
			return true;
		}
		return this.getUserCanShareEntity(this.getRightsContainer(entityId, userId), userId);
	}
		
	protected boolean getUserCanWriteEntity(RightsContainer<? extends TrackedEntity> container, String userId) {
		// No authorized user => no entity write access
		if (userId==null || container==null) {
			return false;
		}
		// Owner has every rights
		if (container.getOwnerId().equals(userId)) {
			return true;
		}
		// Read only entities are only accessible for the owner
		if (container.isReadOnly()) {
			return false;
		}
		// No restricted write list => every authorized user can write
		if (container.getWriteIds()==null) {
			return true;
		}
		// Write list is restricted and the user is on it
		if (container.getWriteIds()!=null && container.getWriteIds().contains(userId)) {
			return true;
		}
		return false;
	}
	
	protected boolean getUserCanShareEntity(RightsContainer<? extends TrackedEntity> container, String userId) {
		// No authorized user => no entity share access
		if (userId==null || container==null) {
			return false;
		}
		// Owner has every rights
		if (container.getOwnerId().equals(userId)) {
			return true;
		}
		// Read only entities are only accessible for the owner
		if (container.isReadOnly()) {
			return false;
		}
		// No restricted share list => every authorized user can share
		if (container.getShareIds()==null) {
			return true;
		}
		// Share list is restricted and the user is on it
		if (container.getShareIds()!=null && container.getShareIds().contains(userId)) {
			return true;
		}
		return false;
	}
	
	private RightsContainer<? extends TrackedEntity> getRightsContainer(String entityId, String userId) {
		if (schemaDao.findEnclosedById(entityId)!=null) {
			return schemaDao.findByIdAndUserId(entityId, userId, true);
		} else {
			return mappingDao.findByIdAndUserId(entityId, userId, true);
		}
	}
}
