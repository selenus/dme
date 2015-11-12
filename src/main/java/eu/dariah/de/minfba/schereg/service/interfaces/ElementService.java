package eu.dariah.de.minfba.schereg.service.interfaces;

import de.dariah.samlsp.model.pojo.AuthPojo;
import eu.dariah.de.minfba.core.metamodel.Nonterminal;
import eu.dariah.de.minfba.core.metamodel.interfaces.Element;
import eu.dariah.de.minfba.core.metamodel.interfaces.Identifiable;
import eu.dariah.de.minfba.core.metamodel.interfaces.Terminal;
import eu.dariah.de.minfba.schereg.serialization.Reference;
import eu.dariah.de.minfba.schereg.service.base.BaseService;

public interface ElementService extends BaseService {
	public Element findRootBySchemaId(String schemaId);
	public Element findRootBySchemaId(String schemaId, boolean eagerLoadHierarchy);
	
	public Element findById(String elementId);
	
	public Identifiable getElementSubtree(String schemaId, String elementId);
	
	/*public void deleteByRootElementId(String rootElementId);
	public void deleteBySchemaId(String schemaId);*/

	public Reference saveElementHierarchy(Element e, AuthPojo auth);
	public Element saveElement(Element e, AuthPojo auth);
	
	public Element createAndAppendElement(String schemaId, String parentElementId, String label, AuthPojo auth);
	public void removeElement(String schemaId, String elementId, AuthPojo auth);
	public Terminal removeTerminal(String schemaId, String terminalId, AuthPojo auth);
	public void saveOrReplaceRoot(String schemaId, Nonterminal element, AuthPojo auth);
	public void clearElementTree(String schemaId, AuthPojo auth);

	
}