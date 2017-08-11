package eu.dariah.de.minfba.schereg.dao.base;

import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Query;

import de.unibamberg.minf.dme.model.tracking.TrackedEntity;

public interface TrackedEntityDao<T extends TrackedEntity> extends BaseDao<T> {
	public List<T> findAll();
	public List<T> findAll(Sort sort);
	public T findById(String id);
	public List<T> find(Query q);
	public T findOne(Query q);
	
	public <S extends T> S save(S element, String userId, String sessionId);
	public void saveNew(List<T> saveElements, String userId, String sessionId);
	
	public void delete(T element, String userId, String sessionId);
	public int delete(Collection<String> id, String userId, String sessionId);
}
