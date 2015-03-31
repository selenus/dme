package eu.dariah.de.minfba.schereg.serialization;

import java.util.Map;

import eu.dariah.de.minfba.core.metamodel.interfaces.Identifiable;

public class Reference implements Identifiable {
	private static final long serialVersionUID = -7751339169245617143L;
	
	private String id;
	private Map<String, Reference[]> childReferences;
	
	public Reference() {}
	public Reference(String id) {
		this.id = id;
	}
	
	@Override public String getId() { return id; }
	@Override public void setId(String id) { this.id = id; }
	
	public Map<String, Reference[]> getChildReferences() { return childReferences; }
	public void setChildReferences(Map<String, Reference[]> childReferences) { this.childReferences = childReferences; }
}