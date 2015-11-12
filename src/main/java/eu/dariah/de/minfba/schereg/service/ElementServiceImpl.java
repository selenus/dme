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

import de.dariah.samlsp.model.pojo.AuthPojo;
import eu.dariah.de.minfba.core.metamodel.Label;
import eu.dariah.de.minfba.core.metamodel.Nonterminal;
import eu.dariah.de.minfba.core.metamodel.function.DescriptionGrammarImpl;
import eu.dariah.de.minfba.core.metamodel.function.TransformationFunctionImpl;
import eu.dariah.de.minfba.core.metamodel.interfaces.Element;
import eu.dariah.de.minfba.core.metamodel.interfaces.Identifiable;
import eu.dariah.de.minfba.core.metamodel.interfaces.Schema;
import eu.dariah.de.minfba.core.metamodel.interfaces.Terminal;
import eu.dariah.de.minfba.schereg.dao.interfaces.ElementDao;
import eu.dariah.de.minfba.schereg.dao.interfaces.FunctionDao;
import eu.dariah.de.minfba.schereg.dao.interfaces.GrammarDao;
import eu.dariah.de.minfba.schereg.dao.interfaces.SchemaDao;
import eu.dariah.de.minfba.schereg.exception.GenericScheregException;
import eu.dariah.de.minfba.schereg.model.MappableElement;
import eu.dariah.de.minfba.schereg.serialization.Reference;
import eu.dariah.de.minfba.schereg.service.base.BaseReferenceServiceImpl;
import eu.dariah.de.minfba.schereg.service.interfaces.ElementService;

@Service
public class ElementServiceImpl extends BaseReferenceServiceImpl implements ElementService {
	@Autowired private ElementDao elementDao;
	@Autowired private SchemaDao schemaDao;
	@Autowired private GrammarDao grammarDao;
	@Autowired private FunctionDao functionDao;
	
	
	@Override
	public Element findRootBySchemaId(String schemaId) {
		return this.findRootBySchemaId(schemaId, false);
	}
		
	@Override
	public Element findRootBySchemaId(String schemaId, boolean eagerLoadHierarchy) {
		Reference reference = this.findReferenceById(schemaId);
		Reference rootElementReference = null;
		if (reference.getChildReferences()!=null && reference.getChildReferences().containsKey(Nonterminal.class.getName()) &&
				reference.getChildReferences().get(Nonterminal.class.getName()).length>0 ) {
			rootElementReference = reference.getChildReferences().get(Nonterminal.class.getName())[0];
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
	
	public static MappableElement convertElement(Element e, boolean deep) {
		if (e==null) {
			return null;
		}
		MappableElement eMap = new MappableElement();
		eMap.setId(e.getId());
		eMap.setLabel(e.getName());
		eMap.setType(e.getClass().getSimpleName());
		
		if (deep) {
			List<Element> eChildren = e.getAllChildElements();
			if (eChildren!=null && eChildren.size()>0) {
				eMap.setChildren(new ArrayList<MappableElement>(eChildren.size()));
				for (Element eChild : eChildren) {
					eMap.getChildren().add(convertElement(eChild, true));
				}
			}
		}
		return eMap;
	}
	
	@Override
	public void saveOrReplaceRoot(String schemaId, Nonterminal element, AuthPojo auth) {
		this.clearElementTree(schemaId, auth);
		element.setId(null);
		
		Reference r = this.saveElementHierarchy(element, auth);
		Reference root = this.findReferenceById(schemaId);
	
		Reference[] childArray = new Reference[1];
		childArray[0] = r;
		
		root.setChildReferences(new HashMap<String, Reference[]>());
		root.getChildReferences().put(element.getClass().getName(), childArray);
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
		if (searchElement.getFunctions()!=null) {
			for (DescriptionGrammarImpl g : searchElement.getFunctions()) {
				if (g.getId().equals(matchElementId)) {
					return g;
				} else {
					if (g.hasTransformationFunctions()) {
						for (TransformationFunctionImpl f : g.getTransformationFunctions()) {
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
	public Reference saveElementHierarchy(Element e, AuthPojo auth) {
		return this.saveElementsInHierarchy(e, auth);
	}
	
	@Override
	public Element saveElement(Element e, AuthPojo auth) {
		if (e instanceof Nonterminal) {
			Nonterminal n = ((Nonterminal)e);
			n.setName(getNormalizedName(n.getName()));
			List<Nonterminal> subelements = n.getChildNonterminals();
			n.setChildNonterminals(null);
			elementDao.save(e, auth.getUserId(), auth.getSessionId());
			
			n.setChildNonterminals(subelements);
		} else {
			Label l = ((Label)e);
			l.setName(getNormalizedName(l.getName()));
			List<Label> subelements = l.getSubLabels();
			l.setSubLabels(null);
			elementDao.save(e, auth.getUserId(), auth.getSessionId());
			
			l.setSubLabels(subelements);			
		}
		return e;
	}
	
	private Reference saveElementsInHierarchy(Element e, AuthPojo auth) {
		
		/*
		 *	TODO: What if the element e is not root but exists as node in another 
		 *		  reference tree? -> Find the tree and merge!
		 *
		 * 	TODO: What if the element has an id (is saved) but the reference can 
		 * 		  nowhere be found? 
		 */
		Reference r = new Reference();
		
		List<? extends Element> subelements;
		Class<? extends Element> subelementClass;
		
		if (e instanceof Nonterminal) {
			Nonterminal n = ((Nonterminal)e);
			n.setName(getNormalizedName(n.getName()));
			subelements = n.getChildNonterminals();
			n.setChildNonterminals(null);
			elementDao.save(e, auth.getUserId(), auth.getSessionId());
			
			n.setChildNonterminals((List<Nonterminal>)subelements);
			subelementClass = Nonterminal.class;
		} else {
			Label l = ((Label)e);
			l.setName(getNormalizedName(l.getName()));
			subelements = l.getSubLabels();
			l.setSubLabels(null);
			elementDao.save(e, auth.getUserId(), auth.getSessionId());
			
			l.setSubLabels((List<Label>)subelements);			
			subelementClass = Label.class;
		}
				
		if (subelements!=null && subelements.size()>0) {
			r.setChildReferences(new HashMap<String, Reference[]>());
			
			Reference[] subreferences = new Reference[subelements.size()];
			for (int i=0; i<subreferences.length; i++) {
				subreferences[i] = saveElementsInHierarchy(subelements.get(i), auth);
			}
			r.getChildReferences().put(subelementClass.getName(), subreferences);
		}
		
		// TODO Functions
		
		r.setId(e.getId());		
		return r;
	}

	@Override
	public Element createAndAppendElement(String schemaId, String parentElementId, String label, AuthPojo auth) {
		Reference rRoot = this.findReferenceById(schemaId);
		Reference rParent = findSubreference(rRoot, parentElementId);
		Element eParent = elementDao.findById(parentElementId);
		
		Element element = null;
		if (rParent!=null) {
			if (eParent instanceof Nonterminal) {
				element = new Nonterminal(schemaId, getNormalizedName(label));
			} else {
				element = new Label(schemaId, getNormalizedName(label));
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
				elementDao.delete(eRemove, auth.getUserId(), auth.getSessionId());
			} catch (Exception e) {
				logger.warn("An error occurred while deleting an element or its references. "
						+ "The owning schema {} might be in an inconsistent state", schemaId, e);
			}
		}
	}
	
	@Override
	public void clearElementTree(String schemaId, AuthPojo auth) {
		Schema s = schemaDao.findEnclosedById(schemaId);
		
		if (s!=null) {	
			try {
				this.clearReferenceTree(schemaId, auth);
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

	@Override
	public Terminal removeTerminal(String schemaId, String terminalId, AuthPojo auth) {
		Schema s = schemaDao.findEnclosedById(schemaId);
		Terminal tRemove = null;
		if (s.getTerminals()!=null) {
			for (Terminal t : s.getTerminals()) {
				if (t.getId().equals(terminalId)) {
					tRemove = t;
					break;
				}
			}
		}
		if (tRemove!=null) {
			s.getTerminals().remove(tRemove);
			try {
				schemaDao.updateContained(s, auth.getUserId(), auth.getSessionId());
			} catch (GenericScheregException e) {
				logger.error("Failed to save schema", e);
			};
				
			List<Element> elements = elementDao.find(Query.query(Criteria.where("schemaId").is(schemaId).and("terminalId").is(terminalId)));
			if (elements!=null) {
				for (Element e : elements) {
					((Nonterminal)e).setTerminalId("");
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
		return elements;
	}
}