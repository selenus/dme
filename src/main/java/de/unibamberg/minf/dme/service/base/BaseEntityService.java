package de.unibamberg.minf.dme.service.base;

public interface BaseEntityService extends BaseService {
	public boolean getUserCanWriteEntity(String entityId, String userId);
	public boolean getUserCanShareEntity(String entityId, String userId);
}
