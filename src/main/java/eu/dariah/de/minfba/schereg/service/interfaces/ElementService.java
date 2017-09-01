package eu.dariah.de.minfba.schereg.service.interfaces;

import java.util.List;

import de.unibamberg.minf.dme.model.base.Element;
import de.unibamberg.minf.dme.model.base.Identifiable;
import de.unibamberg.minf.dme.model.base.Label;
import de.unibamberg.minf.dme.model.base.Nonterminal;
import de.unibamberg.minf.dme.model.base.Terminal;
import eu.dariah.de.dariahsp.model.web.AuthPojo;
import eu.dariah.de.minfba.schereg.serialization.Reference;
import eu.dariah.de.minfba.schereg.service.base.BaseService;

public interface ElementService extends BaseService {
	public Element findRootBySchemaId(String schemaId);
	public Element findRootBySchemaId(String schemaId, boolean eagerLoadHierarchy);
	
	public Element findById(String elementId);
	
	public Identifiable getElementSubtree(String schemaId, String elementId);
	
	/*public void deleteByRootElementId(String rootElementId);
	public void deleteBySchemaId(String schemaId);*/

	
	public Element saveElement(Element e, AuthPojo auth);
	
	public Element createAndAppendElement(String schemaId, String parentElementId, String label, AuthPojo auth);
	public void removeElement(String schemaId, String elementId, AuthPojo auth);
	public Terminal removeTerminal(String schemaId, String terminalId, AuthPojo auth);
	public void saveOrReplaceRoot(String schemaId, Nonterminal element, AuthPojo auth);
	public void clearElementTree(String schemaId, AuthPojo auth);
	
	public List<Identifiable> getElementTrees(String schemaId, List<String> elementIds);
	public <T extends Identifiable> List<Label> convertToLabels(List<T> elements);
	
	public List<Element> findByIds(List<Object> elementIds);
	public List<Element> findBySchemaId(String schemaId);
		
	public Reference assignChildTreeToParent(String entityId, String elementId, String childId);
		
	public void unsetSchemaProcessingRoot(String schemaId);
	public void cloneElement(String elementId, String[] path, AuthPojo auth);
}