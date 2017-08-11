package eu.dariah.de.minfba.schereg.dao.interfaces;

import java.util.List;

import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import de.unibamberg.minf.dme.model.base.Element;
import eu.dariah.de.minfba.schereg.dao.base.TrackedEntityDao;

public interface ElementDao extends TrackedEntityDao<Element> {
	public List<Element> findByEntityId(String entityId);
	public int deleteAll(String entityId);
	public void updateByQuery(Query query, Update update);
}