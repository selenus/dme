package eu.dariah.de.minfba.schereg.dao.base;

import java.util.List;

import org.springframework.data.domain.Sort;

import eu.dariah.de.minfba.core.metamodel.interfaces.Identifiable;

public interface BaseDao<T extends Identifiable> {
	public String getCollectionName();
	
	public List<T> findAll();
	public List<T> findAll(Sort sort);

	public T findById(String id);
	
	public <S extends T> S save(S entity);
	public <S extends T> List<S> save(Iterable<S> entites);
	
}