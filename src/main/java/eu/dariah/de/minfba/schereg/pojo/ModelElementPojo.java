package eu.dariah.de.minfba.schereg.pojo;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import de.unibamberg.minf.dme.model.base.Identifiable;

public class ModelElementPojo implements Identifiable {
	private static final long serialVersionUID = -2307699438052322526L;
	
	public enum ModelElementState { OK, ERROR, WARNING, REUSED, REUSING }
	
	private String id;
	private String label;
	private String type;
	private boolean processingRoot;
	private boolean disabled;
	private ModelElementState state;
	private List<ModelElementPojo> childElements;
	

	@Override public String getId() { return id; }
	@Override public void setId(String id) { this.id = id; }
	
	public String getLabel() { return label; }
	public void setLabel(String label) { this.label = label; }
	
	public String getType() { return type; }
	public void setType(String type) { this.type = type; }
	
	@JsonProperty(value="pRoot")
	public boolean isProcessingRoot() { return processingRoot; }
	public void setProcessingRoot(boolean processingRoot) { this.processingRoot = processingRoot; }
	
	public boolean isDisabled() { return disabled; }
	public void setDisabled(boolean disabled) { this.disabled = disabled; }
	
	public ModelElementState getState() { return state; }
	public void setState(ModelElementState state) { this.state = state; }
	
	public List<ModelElementPojo> getChildElements() { return childElements; }
	public void setChildElements(List<ModelElementPojo> childElements) { this.childElements = childElements; }
}