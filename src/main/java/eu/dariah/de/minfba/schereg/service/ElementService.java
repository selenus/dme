package eu.dariah.de.minfba.schereg.service;

import eu.dariah.de.minfba.core.metamodel.interfaces.Element;
import eu.dariah.de.minfba.schereg.serialization.Reference;

public interface ElementService {
	public Element findRootBySchemaId(String schemaId, boolean eagerLoadHierarchy);
	public Element findRootByElementId(String rootElementId, boolean eagerLoadHierarchy);
	public Element findRootBySchemaId(String schemaId);
	public Element findRootByElementId(String rootElementId);
	
	public Element findById(String elementId);
	
	public void deleteByRootElementId(String rootElementId);
	public void deleteBySchemaId(String schemaId);

	public Reference saveElementHierarchy(Element e);
	public Element saveElement(Element e);
	
	public Element createAndAppendElement(String schemaId, String parentElementId);
	public Element removeElement(String schemaId, String elementId);
}