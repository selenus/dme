package eu.dariah.de.minfba.schereg.importer;

import eu.dariah.de.minfba.core.metamodel.Nonterminal;

public interface SchemaImporter extends Runnable {
	public void setSchemaId(String schemaId);
	public void setSchemaFilePath(String schemaFilePath);
	public void setRootElementNs(String rootElementNs);
	public void setRootElementName(String rootElementName);
	
	public String[] getNamespaces();
	public Nonterminal getRootNonterminal();
	public void setListener(SchemaImportListener importWorker);
}
