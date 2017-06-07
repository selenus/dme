package eu.dariah.de.minfba.schereg.importer;

import java.util.List;

import eu.dariah.de.dariahsp.model.web.AuthPojo;
import eu.dariah.de.minfba.core.metamodel.interfaces.Nonterminal;
import eu.dariah.de.minfba.core.metamodel.interfaces.SchemaNature;
import eu.dariah.de.minfba.core.metamodel.interfaces.Terminal;

public interface SchemaImportListener {
	public void registerImportFailed(SchemaNature schema);
	public void registerImportFinished(SchemaNature schema, Nonterminal root, List<Nonterminal> additionalRootElements, AuthPojo auth);
}
