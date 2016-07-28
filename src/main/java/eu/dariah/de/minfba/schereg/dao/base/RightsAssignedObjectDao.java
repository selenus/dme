package eu.dariah.de.minfba.schereg.dao.base;

import java.util.List;

import org.springframework.data.mongodb.core.query.Criteria;

import eu.dariah.de.minfba.core.metamodel.tracking.TrackedEntity;
import eu.dariah.de.minfba.schereg.exception.GenericScheregException;
import eu.dariah.de.minfba.schereg.model.RightsContainer;

public interface RightsAssignedObjectDao<T extends TrackedEntity> extends TrackedEntityDao<RightsContainer<T>> {
	public List<T> findAllEnclosed();
	public T findEnclosedById(String id);
	public List<RightsContainer<T>> findAllByUserId(String userId);
	public RightsContainer<T> findByIdAndUserId(String id, String userId);
	public RightsContainer<T> findByIdAndUserId(String id, String userId, boolean excludeContained);
	public List<RightsContainer<T>> findByCriteriaAndUserId(Criteria c, String userId);
	public void updateContained(T e, String userId, String sessionId) throws GenericScheregException;
}