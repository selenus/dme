package eu.dariah.de.minfba.schereg.pojo;

import java.util.List;

import eu.dariah.de.minfba.core.metamodel.interfaces.Identifiable;

public class ModelElementPojo implements Identifiable {
	private static final long serialVersionUID = -2307699438052322526L;
	
	public enum ModelElementState { OK, ERROR, WARNING }
	
	private String id;
	private String label;
	private String type;
	private ModelElementState state;
	private List<ModelElementPojo> childElements;
	

	@Override public String getId() { return id; }
	@Override public void setId(String id) { this.id = id; }
	
	public String getLabel() { return label; }
	public void setLabel(String label) { this.label = label; }
	
	public String getType() { return type; }
	public void setType(String type) { this.type = type; }
	
	public ModelElementState getState() { return state; }
	public void setState(ModelElementState state) { this.state = state; }
	
	public List<ModelElementPojo> getChildElements() { return childElements; }
	public void setChildElements(List<ModelElementPojo> childElements) { this.childElements = childElements; }
}