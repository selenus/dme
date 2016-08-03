package eu.dariah.de.minfba.schereg.service.interfaces;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import de.dariah.samlsp.model.pojo.AuthPojo;
import de.unibamberg.minf.gtf.exception.GrammarProcessingException;
import eu.dariah.de.minfba.core.metamodel.function.DescriptionGrammarImpl;
import eu.dariah.de.minfba.core.metamodel.function.interfaces.DescriptionGrammar;
import eu.dariah.de.minfba.schereg.service.base.BaseService;

public interface GrammarService extends BaseService {
	public DescriptionGrammar createAndAppendGrammar(String schemaId, String parentElementId, String label, AuthPojo auth);
	
	public void deleteGrammarsBySchemaId(String schemaId, AuthPojo auth);
	public DescriptionGrammar deleteGrammarById(String schemaId, String id, AuthPojo auth);

	public DescriptionGrammar findById(String grammarId);

	public void saveGrammar(DescriptionGrammarImpl grammar, AuthPojo auth);

	public Collection<String> saveTemporaryGrammar(DescriptionGrammar grammar, String lexerGrammar, String parserGrammar) throws IOException;
	public Collection<String> parseTemporaryGrammar(DescriptionGrammar grammar) throws GrammarProcessingException;
	public Collection<String> compileTemporaryGrammar(DescriptionGrammar grammar) throws IOException, GrammarProcessingException;

	public List<String> getParserRules(DescriptionGrammar grammar) throws GrammarProcessingException;

	public void clearGrammar(DescriptionGrammar g);

	public List<DescriptionGrammar> findByEntityId(String entityId);
}
