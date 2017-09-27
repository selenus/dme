package de.unibamberg.minf.dme.serialization;

import java.util.Map;

import de.unibamberg.minf.dme.model.base.Identifiable;

public class Reference implements Identifiable {
	private static final long serialVersionUID = -7751339169245617143L;
	
	private String id;
	private Map<String, Reference[]> childReferences;
	private boolean root;
	private boolean reuse;
	
	public Reference() {}
	public Reference(String id) {
		this.id = id;
	}
	
	@Override public String getId() { return id; }
	@Override public void setId(String id) { this.id = id; }
	
	public Map<String, Reference[]> getChildReferences() { return childReferences; }
	public void setChildReferences(Map<String, Reference[]> childReferences) { this.childReferences = childReferences; }
	
	public boolean isRoot() { return root; }
	public void setRoot(boolean root) { this.root = root; }
	
	public boolean isReuse() { return reuse; }
	public void setReuse(boolean reuse) { this.reuse = reuse; }	
}