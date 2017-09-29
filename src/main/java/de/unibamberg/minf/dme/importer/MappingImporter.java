package de.unibamberg.minf.dme.importer;

import de.unibamberg.minf.dme.importer.mapping.MappingImportListener;
import de.unibamberg.minf.dme.model.mapping.base.Mapping;
import eu.dariah.de.dariahsp.model.web.AuthPojo;

public interface MappingImporter extends Runnable {
	public boolean isKeepImportedIdsSupported();
	public String getMainImporterType();
	public boolean getIsSupported();
	public void setMapping(Mapping mapping);
	public void setMappingFilePath(String mappingFilePath);
	public void setImportListener(MappingImportListener importListener);
	public void setAuth(AuthPojo auth);
}