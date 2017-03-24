package eu.dariah.de.minfba.schereg.importer;

import java.util.List;

import eu.dariah.de.dariahsp.model.web.AuthPojo;
import eu.dariah.de.minfba.core.metamodel.Nonterminal;
import eu.dariah.de.minfba.core.metamodel.interfaces.Schema;
import eu.dariah.de.minfba.core.metamodel.interfaces.Terminal;

public interface SchemaImporter<T extends Schema> extends Runnable {
	public void setSchema(T schema);
	public void setSchemaFilePath(String schemaFilePath);
	
	public boolean getIsSupported();
	
	public String[] getNamespaces();
	public Nonterminal getRootNonterminal();
	public List<Nonterminal> getAdditionalRootElements();
	public void setListener(SchemaImportListener importWorker);
	public List<? extends Terminal> getPossibleRootTerminals();
	public void setRootElementNs(String rootElementNs);
	public void setRootElementName(String rootElementName);
	
	public void setAuth(AuthPojo auth);
	public AuthPojo getAuth();
}
