package eu.dariah.de.minfba.schereg.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import de.unibamberg.minf.dme.model.base.BaseElement;
import de.unibamberg.minf.dme.model.base.Element;
import de.unibamberg.minf.dme.model.base.Function;
import de.unibamberg.minf.dme.model.base.Grammar;
import de.unibamberg.minf.dme.model.base.Identifiable;
import de.unibamberg.minf.dme.model.base.Label;
import de.unibamberg.minf.dme.model.base.Nonterminal;
import de.unibamberg.minf.dme.model.base.Terminal;
import de.unibamberg.minf.dme.model.datamodel.LabelImpl;
import de.unibamberg.minf.dme.model.datamodel.NonterminalImpl;
import de.unibamberg.minf.dme.model.datamodel.base.Datamodel;
import de.unibamberg.minf.dme.model.datamodel.natures.XmlDatamodelNature;
import de.unibamberg.minf.dme.model.datamodel.natures.xml.XmlTerminal;
import de.unibamberg.minf.dme.model.function.FunctionImpl;
import de.unibamberg.minf.dme.model.grammar.GrammarImpl;
import de.unibamberg.minf.dme.model.mapping.MappedConceptImpl;
import eu.dariah.de.dariahsp.model.web.AuthPojo;
import eu.dariah.de.minfba.schereg.dao.base.DaoImpl;
import eu.dariah.de.minfba.schereg.dao.interfaces.ElementDao;
import eu.dariah.de.minfba.schereg.dao.interfaces.FunctionDao;
import eu.dariah.de.minfba.schereg.dao.interfaces.GrammarDao;
import eu.dariah.de.minfba.schereg.dao.interfaces.MappedConceptDao;
import eu.dariah.de.minfba.schereg.dao.interfaces.SchemaDao;
import eu.dariah.de.minfba.schereg.exception.GenericScheregException;
import eu.dariah.de.minfba.schereg.serialization.Reference;
import eu.dariah.de.minfba.schereg.service.base.BaseReferenceServiceImpl;
import eu.dariah.de.minfba.schereg.service.interfaces.ElementService;
import eu.dariah.de.minfba.schereg.service.interfaces.GrammarService;
import eu.dariah.de.minfba.schereg.service.interfaces.IdentifiableService;

@Service
public class ElementServiceImpl extends BaseReferenceServiceImpl implements ElementService {
	@Autowired private ElementDao elementDao;
	@Autowired private SchemaDao schemaDao;
	@Autowired private GrammarDao grammarDao;
	@Autowired private FunctionDao functionDao;
	@Autowired private MappedConceptDao mappedConceptDao;
	
	@Autowired private IdentifiableService identifiableService;
	
	@Override
	public Element findRootBySchemaId(String schemaId) {
		return this.findRootBySchemaId(schemaId, false);
	}
	
	@Override
	public List<Element> findByIds(List<Object> elementIds) {
		return elementDao.find(Query.query(Criteria.where("_id").in(elementIds)));
	}
	
	@Override
	public List<Element> findBySchemaId(String schemaId) {
		return elementDao.find(Query.query(Criteria.where("entityId").is(schemaId)));
	}
		
	@Override
	public Reference assignChildTreeToParent(String entityId, String elementId, String childId) {
		Reference rootRef = this.findReferenceById(entityId);
		if (rootRef.getChildReferences()==null) {
			return null;
		}
				
		Reference parent = null;
		Reference child = null;
		Reference[] references;
		
		for (String className : rootRef.getChildReferences().keySet()) {
			references = rootRef.getChildReferences().get(className);
			if (references==null) {
				continue;
			}
			for (Reference ref : references) {
				if (parent==null) {
					parent = findSubreference(ref, elementId);
				}
				if (child==null) {
					child = findSubreference(ref, childId);
				}
				if (child!=null && parent!=null) {
					addChildReference(parent, child);
					saveRootReference(rootRef);
					return parent;
				}
			}
		}
		return null;
	}

	@Override
	public Element findRootBySchemaId(String schemaId, boolean eagerLoadHierarchy) {
		Reference reference = this.findReferenceById(schemaId);
		Reference rootElementReference = null;
		if (reference.getChildReferences()!=null) {
			if (reference.getChildReferences().containsKey(NonterminalImpl.class.getName()) &&
					reference.getChildReferences().get(NonterminalImpl.class.getName()).length>0 ) {
				if (reference.getChildReferences().get(NonterminalImpl.class.getName()).length==1) {
					rootElementReference = reference.getChildReferences().get(NonterminalImpl.class.getName())[0];
				} else {
					for (int i=0; i<reference.getChildReferences().get(NonterminalImpl.class.getName()).length; i++) {
						if (reference.getChildReferences().get(NonterminalImpl.class.getName())[i].isRoot()) {
							rootElementReference = reference.getChildReferences().get(NonterminalImpl.class.getName())[i];
							break;
						}
					}
				}
			} else if (reference.getChildReferences().containsKey(MappedConceptImpl.class.getName()) &&
					reference.getChildReferences().get(MappedConceptImpl.class.getName()).length>0 ) {
				rootElementReference = reference.getChildReferences().get(MappedConceptImpl.class.getName())[0];
			}
		}
		if (rootElementReference==null) {
			return null;
		}
		
		Element root = findById(rootElementReference.getId());
		if (!eagerLoadHierarchy) {
			 return root;
		}
		
		List<Identifiable> elements = this.getAllElements(schemaId);		
		Map<String, Identifiable> elementMap = new HashMap<String, Identifiable>(elements.size()); 
		for (Identifiable e : elements) {
			elementMap.put(e.getId(), e);
		}
		return (Element)fillElement(rootElementReference, elementMap);
	}
		
	@Override
	public void saveOrReplaceRoot(String schemaId, Nonterminal element, AuthPojo auth) {
		this.clearElementTree(schemaId, auth);
		element.setId(null);;
		
		Reference r = identifiableService.saveHierarchy(element, auth);
		Reference root = this.findReferenceById(schemaId);
	
		Reference[] childArray = new Reference[1];
		childArray[0] = r;
		
		root.setChildReferences(new HashMap<String, Reference[]>());
		root.getChildReferences().put(element.getClass().getName(), childArray);;
		this.saveRootReference(root);
	}
	
	@Override
	public Element findById(String elementId) {
		return elementDao.findById(elementId);
	}
	
	@Override
	public Identifiable getElementSubtree(String schemaId, String elementId) {
		Element root = this.findRootBySchemaId(schemaId, true);
		return this.getElementSubtree(root, elementId);
	}
	
	@Override
	public List<Identifiable> getElementTrees(String schemaId, List<String> elementIds) {
		Element root = this.findRootBySchemaId(schemaId, true);
		List<Identifiable> result = new ArrayList<Identifiable>(elementIds.size());
		for (String elementId : elementIds) {
			result.add(this.getElementSubtree(root, elementId));
		}
		return result;
	}
	
	@Override
	public <T extends Identifiable> List<Label> convertToLabels(List<T> elements) {
		List<Label> result = new ArrayList<Label>(elements.size());
		Label convert;
		List<Element> subelements;
		for (Identifiable i : elements) {
			if (i instanceof BaseElement) {
				BaseElement e = (BaseElement)i;
				convert = new LabelImpl(e.getEntityId(), e.getName());
				convert.setId(e.getId());
				
				subelements = e.getAllChildElements();
				if (subelements!=null && subelements.size()>0) {
					convert.setSubLabels(convertToLabels(subelements));
				}
				result.add(convert);
			}
		}
		return result;
	}
	
	public static List<Nonterminal> extractAllNonterminals(Element element) {
		if (!Nonterminal.class.isAssignableFrom(element.getClass())) {
			return new ArrayList<Nonterminal>();
		}
		Nonterminal root = (Nonterminal)element;
		List<Nonterminal> result = new ArrayList<Nonterminal>();
		if (root!=null) {
			result.add(root);
			if (root.getChildNonterminals()!=null) {
				for (Nonterminal childN : root.getChildNonterminals()) {
					result.addAll(extractAllNonterminals(childN));
				}
			}
		}
		return result;
	}
	
	
	private Identifiable getElementSubtree(Element searchElement, String matchElementId) {
		if (searchElement.getId().equals(matchElementId)) {
			return searchElement;
		}
		if (searchElement.getAllChildElements()!=null) {
			Identifiable result;
			for (Element subElem : searchElement.getAllChildElements()) {
				result = this.getElementSubtree(subElem, matchElementId);
				if (result!=null) {
					return result;
				}
			}
		}
		// Produced subelements of the grammars/functions are contained in getAllChildElements() above
		if (searchElement.getGrammars()!=null) {
			for (Grammar g : searchElement.getGrammars()) {
				if (g.getId().equals(matchElementId)) {
					return g;
				} else {
					if (g.hasFunctions()) {
						for (Function f : g.getFunctions()) {
							if (f.getId().equals(matchElementId)) {
								return f;
							}
						}
					}
				}
			}
		}
		return null;
	}
		
	@Override
	public Element saveElement(Element e, AuthPojo auth) {
		if (e instanceof NonterminalImpl) {
			NonterminalImpl n = ((NonterminalImpl)e);
			n.setName(getNormalizedName(n.getName()));
			List<Nonterminal> subelements = n.getChildNonterminals();
			n.setChildNonterminals(null);
			elementDao.save(e, auth.getUserId(), auth.getSessionId());
			
			n.setChildNonterminals(subelements);
		} else {
			LabelImpl l = ((LabelImpl)e);
			l.setName(getNormalizedName(l.getName()));
			List<Label> subelements = l.getSubLabels();
			l.setSubLabels(null);
			elementDao.save(e, auth.getUserId(), auth.getSessionId());
			
			l.setSubLabels(subelements);			
		}
		return e;
	}
	
	@Override
	public Element createAndAppendElement(String schemaId, String parentElementId, String label, AuthPojo auth) {
		Reference rRoot = this.findReferenceById(schemaId);
		Reference rParent = findSubreference(rRoot, parentElementId);
		Element eParent = elementDao.findById(parentElementId);
		
		Element element = null;
		if (rParent!=null) {
			if (eParent instanceof Nonterminal) {
				element = new NonterminalImpl(schemaId, getNormalizedName(label));
			} else {
				element = new LabelImpl(schemaId, getNormalizedName(label));
			}
			elementDao.save(element, auth.getUserId(), auth.getSessionId());
			
			addChildReference(rParent, element);
			saveRootReference(rRoot);
		}
		return element;
	}
			
	@Override
	public void removeElement(String schemaId, String elementId, AuthPojo auth) {
		Element eRemove = elementDao.findById(elementId);
		if (eRemove != null) {
			try {
				this.removeReference(schemaId, elementId, auth);
				//elementDao.delete(eRemove, auth.getUserId(), auth.getSessionId());
			} catch (Exception e) {
				logger.warn("An error occurred while deleting an element or its references. "
						+ "The owning schema {} might be in an inconsistent state", schemaId, e);
			}
		}
	}
	
	@Override
	public void clearElementTree(String schemaId, AuthPojo auth) {
		Datamodel s = schemaDao.findEnclosedById(schemaId);
		
		if (s!=null) {	
			try {
				this.clearReferenceTree(schemaId, auth);
				this.deleteAllElements(schemaId);
				
				try {
					schemaDao.updateContained(s, auth.getUserId(), auth.getSessionId());
				} catch (GenericScheregException e) {
					logger.error("Failed to save schema", e);
				}
			} catch (IllegalArgumentException | ClassNotFoundException e) {
				logger.error("Failed to remove tree by schemaID", e);
			}
		}
	}

	private int deleteAllElements(String entityId) {
		int result = elementDao.deleteAll(entityId);
		result += grammarDao.deleteAll(entityId);
		result += functionDao.deleteAll(entityId);
		
		logger.info("Deleted all {} elements of model {}", result, entityId);
		return result;
	}

	@Override
	public Terminal removeTerminal(String schemaId, String terminalId, AuthPojo auth) {
		Datamodel s = schemaDao.findEnclosedById(schemaId);
		Terminal tRemove = null;
		
		List<XmlTerminal> terminals = s.getNature(XmlDatamodelNature.class).getTerminals();
		
		if (terminals!=null) {
			for (Terminal t : terminals) {
				if (t.getId().equals(terminalId)) {
					tRemove = t;
					break;
				}
			}
		}
		if (tRemove!=null) {
			terminals.remove(tRemove);
			try {
				schemaDao.updateContained(s, auth.getUserId(), auth.getSessionId());
			} catch (GenericScheregException e) {
				logger.error("Failed to save schema", e);
			};
				
			List<Element> elements = elementDao.find(Query.query(Criteria.where("schemaId").is(schemaId).and("terminalId").is(terminalId)));
			if (elements!=null) {
				for (Element e : elements) {
					
					s.getNature(XmlDatamodelNature.class).removeTerminalFromMap(tRemove.getId());
					
					elementDao.save(e, auth.getUserId(), auth.getSessionId());
				}
			}
			
			/*elementDao.updateMulti(
					Query.query(Criteria.where("schemaId").is(schemaId).and("terminalId").is(terminalId)), 
					Update.update("terminalId", ""));*/
			
			return tRemove;
		} else {
			return null;
		}
	}
	
	private List<Identifiable> getAllElements(String schemaId) {
		List<Identifiable> elements = new ArrayList<Identifiable>();
		elements.addAll(elementDao.findByEntityId(schemaId));
		elements.addAll(grammarDao.findByEntityId(schemaId));
		elements.addAll(functionDao.findByEntityId(schemaId));
		elements.addAll(mappedConceptDao.findByEntityId(schemaId));
		return elements;
	}

	@Override
	public void unsetSchemaProcessingRoot(String schemaId) {
		elementDao.updateByQuery(Query.query(Criteria.where(DaoImpl.ENTITY_ID_FIELD).is(schemaId).and("_class").is(NonterminalImpl.class.getName())), Update.update("processingRoot", false));
	}
}