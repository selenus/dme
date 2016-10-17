package eu.dariah.de.minfba.schereg.dao.base;

import java.util.Collection;
import java.util.List;

import org.springframework.data.mongodb.core.query.Criteria;

public interface Dao {
	public String getCollectionName();
	public Class<?> getClazz();
	
	public List<?> findAll();
	
	/*public void delete(String id);*/
	/*public int delete(Collection<String> id);*/
}
