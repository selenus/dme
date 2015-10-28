package eu.dariah.de.minfba.schereg.model;

import java.util.List;

public class MappableElement {
	private String id;
	private String label;
	private String type;
	private List<MappableElement> children;
	
	
	public String getId() { return id; }
	public void setId(String id) { this.id = id; }
	
	public String getLabel() { return label; }
	public void setLabel(String label) { this.label = label; }
	
	public List<MappableElement> getChildren() { return children; }
	public void setChildren(List<MappableElement> children) { this.children = children; }
	
	public String getType() { return type; }
	public void setType(String type) { this.type = type; }
}
