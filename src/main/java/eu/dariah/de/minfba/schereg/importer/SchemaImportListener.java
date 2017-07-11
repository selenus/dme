package eu.dariah.de.minfba.schereg.importer;

import java.util.List;

import eu.dariah.de.dariahsp.model.web.AuthPojo;
import eu.dariah.de.minfba.core.metamodel.interfaces.Nonterminal;
import eu.dariah.de.minfba.core.metamodel.interfaces.Schema;

public interface SchemaImportListener {
	public void registerImportFailed(Schema schema);
	public void registerImportFinished(Schema schema, Nonterminal root, List<Nonterminal> additionalRootElements, AuthPojo auth);
}
