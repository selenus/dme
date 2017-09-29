package de.unibamberg.minf.dme.importer.datamodel;

import java.util.List;

import de.unibamberg.minf.dme.model.base.ModelElement;
import de.unibamberg.minf.dme.model.datamodel.base.Datamodel;
import eu.dariah.de.dariahsp.model.web.AuthPojo;

public interface DatamodelImportListener {
	public void registerImportFailed(Datamodel schema);
	public void registerImportFinished(Datamodel importedSchema, String parentElementId, List<ModelElement> rootElements, List<ModelElement> additionalRootElements, AuthPojo auth);
}
