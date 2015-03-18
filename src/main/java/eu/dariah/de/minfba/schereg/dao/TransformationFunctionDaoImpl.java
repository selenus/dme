package eu.dariah.de.minfba.schereg.dao;

import org.springframework.stereotype.Repository;

import de.dariah.base.dao.base.BaseDaoImpl;
import eu.dariah.de.minfba.core.metamodel.function.TransformationFunctionImpl;
import eu.dariah.de.minfba.core.metamodel.function.interfaces.TransformationFunction;

@Repository
public class TransformationFunctionDaoImpl extends BaseDaoImpl<TransformationFunction> implements TransformationFunctionDao {
	public TransformationFunctionDaoImpl() {
		super(TransformationFunctionImpl.class);
	}
}