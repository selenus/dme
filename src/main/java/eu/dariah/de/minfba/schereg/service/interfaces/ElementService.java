package eu.dariah.de.minfba.schereg.service.interfaces;

import de.dariah.samlsp.model.pojo.AuthPojo;
import eu.dariah.de.minfba.core.metamodel.Nonterminal;
import eu.dariah.de.minfba.core.metamodel.interfaces.Element;
import eu.dariah.de.minfba.core.metamodel.interfaces.Identifiable;
import eu.dariah.de.minfba.core.metamodel.interfaces.Terminal;
import eu.dariah.de.minfba.schereg.serialization.Reference;
import eu.dariah.de.minfba.schereg.service.base.BaseService;

public interface ElementService extends BaseService {
	public Element findRootBySchemaId(String schemaId, boolean eagerLoadHierarchy);
	public Element findRootByElementId(String rootElementId, boolean eagerLoadHierarchy);
	public Element findRootBySchemaId(String schemaId);
	public Element findRootByElementId(String rootElementId);
	
	public Element findById(String elementId);
	
	public Identifiable getElementSubtree(String schemaId, String elementId);
	
	/*public void deleteByRootElementId(String rootElementId);
	public void deleteBySchemaId(String schemaId);*/

	public Reference saveElementHierarchy(Element e, AuthPojo auth);
	public Element saveElement(Element e, AuthPojo auth);
	
	public Element createAndAppendElement(String schemaId, String parentElementId, String label, AuthPojo auth);
	public Element removeElement(String schemaId, String elementId, AuthPojo auth);
	public Terminal removeTerminal(String schemaId, String terminalId, AuthPojo auth);
	public Element removeElementTree(String id, AuthPojo auth);
	public void saveOrReplaceRoot(String schemaId, Nonterminal element, AuthPojo auth);
	
}