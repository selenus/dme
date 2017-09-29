package de.unibamberg.minf.dme.importer.mapping;

import de.unibamberg.minf.dme.model.mapping.base.Mapping;
import eu.dariah.de.dariahsp.model.web.AuthPojo;

public interface MappingImportListener {
	public void registerImportFailed(Mapping mapping);
	public void registerImportFinished(Mapping importedMapping, AuthPojo auth);
}
