package de.unibamberg.minf.dme.importer;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.dariah.de.dariahsp.model.web.AuthPojo;

public abstract class BaseImporter implements Importer {
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	protected boolean keepImportedIds;
	protected String importFilePath;
	
	protected AuthPojo auth;
	
	@Override public boolean isKeepImportedIds() { return keepImportedIds; }
	@Override public void setKeepImportedIds(boolean keepImportedIds) { this.keepImportedIds = keepImportedIds; }
	
	@Override public void setAuth(AuthPojo auth) { this.auth = auth; }
	
	@Override public void setImportFilePath(String importFilePath) { this.importFilePath = importFilePath; }
	
	
	protected String getOrCreateId(String id) {
		return this.keepImportedIds ? id : new ObjectId().toString();
	}
}
