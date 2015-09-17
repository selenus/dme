package eu.dariah.de.minfba.schereg.importer;

import java.util.List;

import de.dariah.samlsp.model.pojo.AuthPojo;
import eu.dariah.de.minfba.core.metamodel.Nonterminal;
import eu.dariah.de.minfba.core.metamodel.interfaces.Schema;
import eu.dariah.de.minfba.core.metamodel.interfaces.Terminal;

public interface SchemaImportListener {
	public void registerImportFailed(Schema schema);
	public void registerImportFinished(Schema schema, Nonterminal root, AuthPojo auth);
}
