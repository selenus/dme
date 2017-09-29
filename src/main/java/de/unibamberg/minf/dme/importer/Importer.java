package de.unibamberg.minf.dme.importer;

import eu.dariah.de.dariahsp.model.web.AuthPojo;

public interface Importer extends Runnable {
	
	public boolean isKeepImportedIdsSupported();
	public String getMainImporterType();
	public String getImporterSubtype();
	
	public boolean getIsSupported();
	
	
	public void setAuth(AuthPojo auth);
	
	public void setImportFilePath(String filePath);
	
	
	public boolean isKeepImportedIds();
	public void setKeepImportedIds(boolean keepImportedIds);
}
