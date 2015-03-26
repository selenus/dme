package eu.dariah.de.minfba.schereg.importer;

import java.util.List;

import eu.dariah.de.minfba.core.metamodel.Nonterminal;
import eu.dariah.de.minfba.core.metamodel.interfaces.Terminal;

public interface SchemaImportListener {
	public void registerImportFinished(String schemaId, Nonterminal root, List<? extends Terminal> terminals);
	public void registerImportFailed(String schemaId);
}
