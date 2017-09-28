package de.unibamberg.minf.dme.importer;

import java.util.List;

import de.unibamberg.minf.dme.model.base.Identifiable;
import de.unibamberg.minf.dme.model.base.ModelElement;
import de.unibamberg.minf.dme.model.datamodel.base.Datamodel;
import eu.dariah.de.dariahsp.model.web.AuthPojo;

public interface SchemaImporter extends Runnable {
	
	public Datamodel getSchema();
	public void setSchema(Datamodel schema);
	
	public void setSchemaFilePath(String schemaFilePath);
	
	public boolean getIsSupported();
	
	public String[] getNamespaces();
	public List<ModelElement> getRootElements();
	public List<ModelElement> getAdditionalRootElements();
	public void setListener(SchemaImportListener importWorker);

	public void setRootElementName(String rootElementName);
	public void setRootElementType(String rootElementType);
	public void setElementId(String elementId);
	
	public void setAuth(AuthPojo auth);
	public AuthPojo getAuth();
	
	public List<? extends Identifiable> getPossibleRootElements();
	public List<? extends ModelElement> getElementsByTypes(List<Class<? extends ModelElement>> allowedSubtreeRoots);
	
	public boolean isKeepImportedIds();
	public void setKeepImportedIds(boolean keepImportedIds);
	
	public boolean isKeepImportedIdsSupported();
	public String getMainImporterType();
	public String getImporterSubtype();
}
