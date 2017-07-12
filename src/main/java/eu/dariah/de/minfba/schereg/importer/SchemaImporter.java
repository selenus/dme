package eu.dariah.de.minfba.schereg.importer;

import java.util.List;

import eu.dariah.de.dariahsp.model.web.AuthPojo;
import eu.dariah.de.minfba.core.metamodel.interfaces.Identifiable;
import eu.dariah.de.minfba.core.metamodel.interfaces.Nonterminal;
import eu.dariah.de.minfba.core.metamodel.interfaces.Schema;

public interface SchemaImporter extends Runnable {
	
	public Schema getSchema();
	public void setSchema(Schema schema);
	
	public void setSchemaFilePath(String schemaFilePath);
	
	public boolean getIsSupported();
	
	public String[] getNamespaces();
	public Nonterminal getRootNonterminal();
	public List<Nonterminal> getAdditionalRootElements();
	public void setListener(SchemaImportListener importWorker);

	public void setRootElementName(String rootElementName);
	
	public void setAuth(AuthPojo auth);
	public AuthPojo getAuth();
	
	public List<? extends Identifiable> getPossibleRootElements();
	public List<? extends Identifiable> getElementsByTypes(List<Class<? extends Identifiable>> allowedSubtreeRoots);

}
