package eu.dariah.de.minfba.schereg.service.interfaces;

import java.io.IOException;

import de.unibamberg.minf.gtf.exception.GrammarProcessingException;
import eu.dariah.de.minfba.core.metamodel.function.DescriptionGrammarImpl;
import eu.dariah.de.minfba.core.metamodel.function.interfaces.DescriptionGrammar;

public interface GrammarService {
	public DescriptionGrammar createAndAppendGrammar(String schemaId, String parentElementId, String label);
	
	public void deleteGrammarsBySchemaId(String schemaId);
	public DescriptionGrammar deleteGrammarById(String schemaId, String id);

	public DescriptionGrammar findById(String grammarId);

	public void saveGrammar(DescriptionGrammarImpl grammar);

	public void saveTemporaryGrammar(String grammarId, String lexerGrammar, String parserGrammar) throws IOException;
	public void parseTemporaryGrammar(String grammarId) throws GrammarProcessingException;
	public void compileTemporaryGrammar(String grammarId) throws IOException, GrammarProcessingException;
}
