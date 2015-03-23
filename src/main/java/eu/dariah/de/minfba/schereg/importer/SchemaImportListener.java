package eu.dariah.de.minfba.schereg.importer;

import eu.dariah.de.minfba.core.metamodel.Nonterminal;

public interface SchemaImportListener {
	public void registerImportFinished(String schemaId, Nonterminal root);
	public void registerImportFailed(String schemaId);
}
