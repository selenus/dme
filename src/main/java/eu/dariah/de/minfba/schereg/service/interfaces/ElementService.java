package eu.dariah.de.minfba.schereg.service.interfaces;

import java.util.List;
import java.util.Map;

import eu.dariah.de.dariahsp.model.web.AuthPojo;
import eu.dariah.de.minfba.core.metamodel.function.GrammarContainer;
import eu.dariah.de.minfba.core.metamodel.interfaces.Element;
import eu.dariah.de.minfba.core.metamodel.interfaces.Identifiable;
import eu.dariah.de.minfba.core.metamodel.interfaces.Label;
import eu.dariah.de.minfba.core.metamodel.interfaces.Nonterminal;
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
	
	public List<Identifiable> getElementTrees(String schemaId, List<String> elementIds);
	public <T extends Identifiable> List<Label> convertToLabels(List<T> elements);
	
	public List<Element> findByIds(List<Object> elementIds);
	public List<Element> findBySchemaId(String schemaId);
	
	public List<Nonterminal> extractAllNonterminals(Nonterminal root);
	
	public Reference assignChildTreeToParent(String entityId, String elementId, String childId);
	public void regenerateIds(String entityId, Element element, Map<String, String> terminalIdMap, Map<String, GrammarContainer> grammarContainerMap);
	public Map<String, String> regenerateIds(String entityId, List<? extends Terminal> terminals);
}