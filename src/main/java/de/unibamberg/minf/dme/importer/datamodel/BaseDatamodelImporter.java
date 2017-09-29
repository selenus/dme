package de.unibamberg.minf.dme.importer.datamodel;

import java.util.ArrayList;
import java.util.List;

import de.unibamberg.minf.dme.importer.BaseImporter;
import de.unibamberg.minf.dme.model.base.ModelElement;
import de.unibamberg.minf.dme.model.datamodel.base.Datamodel;

public abstract class BaseDatamodelImporter extends BaseImporter implements DatamodelImporter {
	private DatamodelImportListener listener;
	
	private Datamodel datamodel;
	private String rootElementName; 
	private String rootElementType;
	private String elementId;
	
	private List<ModelElement> rootElements = new ArrayList<ModelElement>();
	private List<ModelElement> additionalRootElements;
	
	
	@Override public Datamodel getDatamodel() { return datamodel; }
	@Override public void setDatamodel(Datamodel datamodel) { this.datamodel = datamodel; }
	
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
	
	protected DatamodelImportListener getListener() { return listener; }
	@Override public void setListener(DatamodelImportListener listener) { this.listener = listener; }
		
	protected String createNonterminalName(String terminalName) {
		String name = terminalName.substring(0, 1).toUpperCase() + terminalName.substring(1);
		
		name = name.replaceAll("([^\\p{L}])([^\\p{L}\\p{N}-_.])*", "");
		
		return name; 
	}
}
