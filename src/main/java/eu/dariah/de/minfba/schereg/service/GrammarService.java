package eu.dariah.de.minfba.schereg.service;

import eu.dariah.de.minfba.core.metamodel.function.interfaces.DescriptionGrammar;
import eu.dariah.de.minfba.core.metamodel.function.interfaces.TransformationFunction;

public interface GrammarService {
	public void deleteGrammarsBySchemaId(String schemaId);
	public DescriptionGrammar deleteGrammarById(String schemaId, String id);
	
	public void deleteFunctionsBySchemaId(String schemaId);
	public TransformationFunction deleteFunctionById(String schemaId, String id);
	public DescriptionGrammar createAndAppendGrammar(String schemaId, String parentElementId, String label);
}
