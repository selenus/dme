package eu.dariah.de.minfba.schereg.importer;

import java.util.List;

import eu.dariah.de.minfba.core.metamodel.Nonterminal;
import eu.dariah.de.minfba.core.metamodel.interfaces.Terminal;

public interface SchemaImporter extends Runnable {
	public void setSchemaId(String schemaId);
	public void setSchemaFilePath(String schemaFilePath);
	public void setRootElementNs(String rootElementNs);
	public void setRootElementName(String rootElementName);
	
	public boolean getIsSupported();
	
	public String[] getNamespaces();
	public Nonterminal getRootNonterminal();
	public void setListener(SchemaImportListener importWorker);
	public List<? extends Terminal> getPossibleRootTerminals();
}
