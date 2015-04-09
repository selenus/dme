package eu.dariah.de.minfba.schereg.service.interfaces;

import eu.dariah.de.minfba.core.metamodel.function.TransformationFunctionImpl;
import eu.dariah.de.minfba.core.metamodel.function.interfaces.TransformationFunction;
import eu.dariah.de.minfba.core.metamodel.interfaces.Element;

public interface FunctionService {
	public TransformationFunction createAndAppendFunction(String schemaId, String grammarId, String label);
	
	public void deleteFunctionsBySchemaId(String schemaId);
	public TransformationFunction deleteFunctionById(String schemaId, String id);

	public TransformationFunction findById(String functionId);

	public void saveFunction(TransformationFunctionImpl function);
}
