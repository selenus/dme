package eu.dariah.de.minfba.schereg.service.interfaces;

import eu.dariah.de.minfba.core.metamodel.function.DescriptionGrammarImpl;
import eu.dariah.de.minfba.core.metamodel.function.interfaces.DescriptionGrammar;
import eu.dariah.de.minfba.core.metamodel.interfaces.Element;

public interface GrammarService {
	public DescriptionGrammar createAndAppendGrammar(String schemaId, String parentElementId, String label);
	
	public void deleteGrammarsBySchemaId(String schemaId);
	public DescriptionGrammar deleteGrammarById(String schemaId, String id);

	public DescriptionGrammar findById(String grammarId);

	public void saveGrammar(DescriptionGrammarImpl grammar);
}
