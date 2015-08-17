package eu.dariah.de.minfba.schereg.pojo;

import java.util.List;

public class TreeElementPojo {
	private String id;
	private String label;
	private Object value;
	private List<TreeElementPojo> children;
	
	public String getId() { return id; }
	public void setId(String id) { this.id = id; }
	
	public String getLabel() { return label; }
	public void setLabel(String label) {  
		this.label = label;
	}
	public Object getValue() {
		return value;
	}
	public void setValue(Object value) {
		this.value = value;
	}
	public List<TreeElementPojo> getChildren() {
		return children;
	}
	public void setChildren(List<TreeElementPojo> children) {
		this.children = children;
	}
	
	
}
