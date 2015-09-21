package eu.dariah.de.minfba.schereg.dao.base;

import eu.dariah.de.minfba.core.metamodel.tracking.TrackedEntity;

public interface TrackedEntityDao<T extends TrackedEntity> extends BaseDao<T> {
	public <S extends T> S save(S entity, String userId, String sessionId);
}
