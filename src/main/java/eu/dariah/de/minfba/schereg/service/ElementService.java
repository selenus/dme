package eu.dariah.de.minfba.schereg.service;

import eu.dariah.de.minfba.core.metamodel.interfaces.Element;

public interface ElementService {
	public Element findRootBySchemaId(String schemaId);
	public Element findRootByElementId(String rootElementId);
	public void deleteByRootElementId(String rootElementId);
	public void deleteBySchemaId(String schemaId);
	public void saveElement(Element root);
}
