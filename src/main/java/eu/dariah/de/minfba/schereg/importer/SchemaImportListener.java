package eu.dariah.de.minfba.schereg.importer;

import java.util.List;

import eu.dariah.de.minfba.core.metamodel.Nonterminal;
import eu.dariah.de.minfba.core.metamodel.interfaces.Schema;
import eu.dariah.de.minfba.core.metamodel.interfaces.Terminal;

public interface SchemaImportListener {
	public void registerImportFinished(Schema schema, Nonterminal root);
	public void registerImportFailed(Schema schema);
}
