package de.unibamberg.minf.dme.importer;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.unibamberg.minf.dme.model.base.ModelElement;
import de.unibamberg.minf.dme.model.datamodel.base.Datamodel;
import eu.dariah.de.dariahsp.model.web.AuthPojo;

public abstract class BaseSchemaImporter implements SchemaImporter {
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private SchemaImportListener listener;
	
	private Datamodel schema;
	private String schemaFilePath;
	private String rootElementName; 
	private String rootElementType;
	private String elementId;
	
	private boolean keepImportedIds;
	
	private AuthPojo auth;
	
	private List<ModelElement> rootElements = new ArrayList<ModelElement>();
	private List<ModelElement> additionalRootElements;
	
	
	@Override public Datamodel getSchema() { return schema; }
	@Override public void setSchema(Datamodel schema) { this.schema = schema; }
	
	protected String getSchemaFilePath() { return schemaFilePath; }
	@Override public void setSchemaFilePath(String schemaFilePath) { this.schemaFilePath = schemaFilePath; }
 	
 	protected String getRootElementName() { return rootElementName; }
	@Override public void setRootElementName(String rootElementName) { this.rootElementName = rootElementName; }
	
	public String getRootElementType() { return rootElementType; }
	@Override public void setRootElementType(String rootElementType) { this.rootElementType = rootElementType; }
	
	public String getElementId() { return elementId; }
	@Override public void setElementId(String elementId) { this.elementId = elementId; }
	
	@Override public List<ModelElement> getRootElements() { return rootElements; }
	public void setRootElements(List<ModelElement> rootElements) { this.rootElements = rootElements; }
	
	@Override public List<ModelElement> getAdditionalRootElements() { return additionalRootElements; }
	public void setAdditionalRootElements(List<ModelElement> additionalRootElements) { this.additionalRootElements = additionalRootElements; }
	
	protected SchemaImportListener getListener() { return listener; }
	@Override public void setListener(SchemaImportListener listener) { this.listener = listener; }
	
	@Override public boolean isKeepImportedIds() { return keepImportedIds; }
	@Override public void setKeepImportedIds(boolean keepImportedIds) { this.keepImportedIds = keepImportedIds; }
	
	@Override public void setAuth(AuthPojo auth) { this.auth = auth; }
	@Override public AuthPojo getAuth() { return this.auth; }
	
	
	protected String createNonterminalName(String terminalName) {
		String name = terminalName.substring(0, 1).toUpperCase() + terminalName.substring(1);
		
		name = name.replaceAll("([^\\p{L}])([^\\p{L}\\p{N}-_.])*", "");
		
		return name; 
	}
}
