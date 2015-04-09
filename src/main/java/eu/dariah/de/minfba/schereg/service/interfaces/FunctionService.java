package eu.dariah.de.minfba.schereg.service.interfaces;

import eu.dariah.de.minfba.core.metamodel.function.interfaces.TransformationFunction;

public interface FunctionService {
	public TransformationFunction createAndAppendFunction(String schemaId, String grammarId, String label);
	
	public void deleteFunctionsBySchemaId(String schemaId);
	public TransformationFunction deleteFunctionById(String schemaId, String id);
}
