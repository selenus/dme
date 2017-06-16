package eu.dariah.de.minfba.schereg.dao.interfaces;

import java.util.List;

import eu.dariah.de.minfba.core.metamodel.function.interfaces.TransformationFunction;
import eu.dariah.de.minfba.schereg.dao.base.TrackedEntityDao;

public interface FunctionDao extends TrackedEntityDao<TransformationFunction> {
	public List<TransformationFunction> findByEntityId(String entityId);
	
	public int deleteAll(String entityId);
}
