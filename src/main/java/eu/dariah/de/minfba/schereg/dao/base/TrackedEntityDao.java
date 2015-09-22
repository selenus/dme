package eu.dariah.de.minfba.schereg.dao.base;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Query;

import eu.dariah.de.minfba.core.metamodel.tracking.TrackedEntity;

public interface TrackedEntityDao<T extends TrackedEntity> {
	public List<T> findAll();
	public List<T> findAll(Sort sort);
	public T findById(String id);
	public List<T> find(Query q);
	public T findOne(Query q);
	
	public <S extends T> S save(S element, String userId, String sessionId);
	public void delete(T element, String userId, String sessionId);
	public int delete(Collection<String> id, String userId, String sessionId);
}
