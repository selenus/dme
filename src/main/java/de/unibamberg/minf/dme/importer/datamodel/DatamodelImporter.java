package de.unibamberg.minf.dme.importer.datamodel;

import java.util.List;

import de.unibamberg.minf.dme.importer.Importer;
import de.unibamberg.minf.dme.model.base.Identifiable;
import de.unibamberg.minf.dme.model.base.ModelElement;
import de.unibamberg.minf.dme.model.datamodel.base.Datamodel;

public interface DatamodelImporter extends Importer {
	
	public Datamodel getDatamodel();
	public void setDatamodel(Datamodel schema);

	public String[] getNamespaces();
	public List<ModelElement> getRootElements();
	public List<ModelElement> getAdditionalRootElements();
	
	public void setListener(DatamodelImportListener importWorker);

	public void setRootElementName(String rootElementName);
	public void setRootElementType(String rootElementType);
	
	// Parent for subtree import
	public void setElementId(String elementId);
	
	public List<? extends Identifiable> getPossibleRootElements();
	public List<? extends ModelElement> getElementsByTypes(List<Class<? extends ModelElement>> allowedSubtreeRoots);	
}
