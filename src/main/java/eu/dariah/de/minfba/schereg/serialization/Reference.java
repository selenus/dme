package eu.dariah.de.minfba.schereg.serialization;

import java.util.Map;

import eu.dariah.de.minfba.core.metamodel.interfaces.Identifiable;

public class Reference implements Identifiable {
	private static final long serialVersionUID = -7751339169245617143L;
	
	private String id;
	private Map<Class<?>, Reference[]> childReferences;
	
	
	@Override public String getId() { return id; }
	@Override public void setId(String id) { this.id = id; }
	
	public Map<Class<?>, Reference[]> getChildReferences() { return childReferences; }
	public void setChildReferences(Map<Class<?>, Reference[]> childReferences) { this.childReferences = childReferences; }
}