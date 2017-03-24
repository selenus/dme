package eu.dariah.de.minfba.schereg.service.interfaces;

import java.util.List;

import eu.dariah.de.dariahsp.model.web.AuthPojo;
import eu.dariah.de.minfba.core.metamodel.function.TransformationFunctionImpl;
import eu.dariah.de.minfba.core.metamodel.function.interfaces.TransformationFunction;
import eu.dariah.de.minfba.core.metamodel.interfaces.Element;
import eu.dariah.de.minfba.schereg.service.base.BaseService;

public interface FunctionService extends BaseService {
	public TransformationFunction createAndAppendFunction(String schemaId, String grammarId, String label, AuthPojo auth);
	
	public void deleteFunctionsBySchemaId(String schemaId);

	public TransformationFunction findById(String functionId);

	public void saveFunction(TransformationFunctionImpl function, AuthPojo auth);

	public TransformationFunction deleteFunctionById(String schemaId, String id, AuthPojo auth);

	public List<TransformationFunction> findByEntityId(String entityId);
}
