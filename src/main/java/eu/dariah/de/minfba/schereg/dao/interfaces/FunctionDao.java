package eu.dariah.de.minfba.schereg.dao.interfaces;

import java.util.List;

import eu.dariah.de.minfba.core.metamodel.function.interfaces.TransformationFunction;
import eu.dariah.de.minfba.schereg.dao.base.BaseDao;

public interface FunctionDao extends BaseDao<TransformationFunction> {
	public List<TransformationFunction> findBySchemaId(String schemaId);
}
