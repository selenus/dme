package eu.dariah.de.minfba.schereg.service.interfaces;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import de.unibamberg.minf.gtf.exception.GrammarProcessingException;
import eu.dariah.de.minfba.core.metamodel.function.DescriptionGrammarImpl;
import eu.dariah.de.minfba.core.metamodel.function.interfaces.DescriptionGrammar;

public interface GrammarService {
	public DescriptionGrammar createAndAppendGrammar(String schemaId, String parentElementId, String label);
	
	public void deleteGrammarsBySchemaId(String schemaId);
	public DescriptionGrammar deleteGrammarById(String schemaId, String id);

	public DescriptionGrammar findById(String grammarId);

	public void saveGrammar(DescriptionGrammarImpl grammar);

	public Collection<String> saveTemporaryGrammar(DescriptionGrammar grammar, String lexerGrammar, String parserGrammar) throws IOException;
	public Collection<String> parseTemporaryGrammar(DescriptionGrammar grammar) throws GrammarProcessingException;
	public Collection<String> compileTemporaryGrammar(DescriptionGrammar grammar) throws IOException, GrammarProcessingException;

	public List<String> getParserRules(DescriptionGrammar grammar) throws GrammarProcessingException;

	public void clearTemporaryGrammar(DescriptionGrammar g);
}
