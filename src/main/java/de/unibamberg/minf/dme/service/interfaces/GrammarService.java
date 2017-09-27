package de.unibamberg.minf.dme.service.interfaces;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import de.unibamberg.minf.dme.model.base.Grammar;
import de.unibamberg.minf.dme.model.grammar.GrammarContainer;
import de.unibamberg.minf.dme.model.grammar.GrammarImpl;
import de.unibamberg.minf.dme.service.base.BaseService;
import de.unibamberg.minf.gtf.exceptions.GrammarProcessingException;
import eu.dariah.de.dariahsp.model.web.AuthPojo;

public interface GrammarService extends BaseService {
	public Grammar createAndAppendGrammar(String schemaId, String parentElementId, String label, AuthPojo auth);
	
	public void deleteGrammarsBySchemaId(String schemaId, AuthPojo auth);
	public Grammar deleteGrammarById(String schemaId, String id, AuthPojo auth);

	public Grammar findById(String grammarId);

	public void saveGrammar(GrammarImpl grammar, AuthPojo auth);

	public Collection<String> saveTemporaryGrammar(Grammar grammar, String lexerGrammar, String parserGrammar) throws IOException;
	public Collection<String> parseTemporaryGrammar(Grammar grammar) throws GrammarProcessingException;
	public Collection<String> compileTemporaryGrammar(Grammar grammar) throws IOException, GrammarProcessingException;

	public List<String> getParserRules(Grammar grammar) throws GrammarProcessingException;

	public void clearGrammar(Grammar g);

	public List<Grammar> findByEntityId(String entityId, boolean includeSources);

	public List<Grammar> findByIds(List<Object> grammarIds);

	public void moveGrammar(String entityId, String grammarId, int delta, AuthPojo auth);
	
	public Map<String, GrammarContainer> serializeGrammarSources(String entityId);

	public Map<String, GrammarContainer> serializeGrammarSources(List<Grammar> grammars);
}
