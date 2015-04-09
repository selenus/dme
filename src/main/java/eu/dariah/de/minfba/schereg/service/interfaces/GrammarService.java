package eu.dariah.de.minfba.schereg.service.interfaces;

import eu.dariah.de.minfba.core.metamodel.function.interfaces.DescriptionGrammar;

public interface GrammarService {
	public DescriptionGrammar createAndAppendGrammar(String schemaId, String parentElementId, String label);
	
	public void deleteGrammarsBySchemaId(String schemaId);
	public DescriptionGrammar deleteGrammarById(String schemaId, String id);
}
