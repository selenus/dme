package eu.dariah.de.minfba.schereg.dao.interfaces;

import java.util.List;

import eu.dariah.de.minfba.core.metamodel.interfaces.Element;
import eu.dariah.de.minfba.schereg.dao.base.TrackedEntityDao;

public interface ElementDao extends TrackedEntityDao<Element> {
	public List<Element> findByEntityId(String entityId);

	public void saveNew(List<Element> saveElements, String userId, String sessionId);
}