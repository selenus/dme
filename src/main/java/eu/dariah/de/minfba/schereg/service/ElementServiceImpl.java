package eu.dariah.de.minfba.schereg.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import eu.dariah.de.minfba.core.metamodel.Label;
import eu.dariah.de.minfba.core.metamodel.Nonterminal;
import eu.dariah.de.minfba.core.metamodel.function.DescriptionGrammarImpl;
import eu.dariah.de.minfba.core.metamodel.interfaces.Element;
import eu.dariah.de.minfba.core.metamodel.interfaces.Identifiable;
import eu.dariah.de.minfba.core.metamodel.interfaces.Schema;
import eu.dariah.de.minfba.core.metamodel.interfaces.Terminal;
import eu.dariah.de.minfba.schereg.dao.interfaces.ElementDao;
import eu.dariah.de.minfba.schereg.dao.interfaces.GrammarDao;
import eu.dariah.de.minfba.schereg.dao.interfaces.SchemaDao;
import eu.dariah.de.minfba.schereg.serialization.Reference;
import eu.dariah.de.minfba.schereg.service.base.BaseReferenceServiceImpl;
import eu.dariah.de.minfba.schereg.service.interfaces.ElementService;

@Service
public class ElementServiceImpl extends BaseReferenceServiceImpl implements ElementService {
	private static Logger logger = LoggerFactory.getLogger(ElementServiceImpl.class);
	
	@Autowired private ElementDao elementDao;
	@Autowired private SchemaDao schemaDao;
	@Autowired private GrammarDao grammarDao;
	
	@Override
	public Element findRootBySchemaId(String schemaId) {
		return this.findRootBySchemaId(schemaId, false);
	}
	
	@Override
	public Element findRootByElementId(String rootElementId) {
		return this.findRootByElementId(rootElementId, false);
	}
	
	@Override
	public Element findRootBySchemaId(String schemaId, boolean eagerLoadHierarchy) {
		Schema s = schemaDao.findById(schemaId);
		if (s!=null && s.getRootNonterminalId()!=null) {
			return this.findRootByElementId(s.getRootNonterminalId(), eagerLoadHierarchy);
		}
		return null;
	}
	
	@Override
	public Element findRootByElementId(String rootElementId, boolean eagerLoadHierarchy) {
		Element root = elementDao.findById(rootElementId);
		if (!eagerLoadHierarchy) {
			return root;
		}
		
		List<Identifiable> elements = new ArrayList<Identifiable>();
		elements.addAll(elementDao.findBySchemaId(root.getSchemaId()));
		elements.addAll(grammarDao.findBySchemaId(root.getSchemaId()));
		
		Map<String, Identifiable> elementMap = new HashMap<String, Identifiable>(elements.size()); 
		for (Identifiable e : elements) {
			elementMap.put(e.getId(), e);
		}
		return (Element)fillElement(findRootReferenceById(rootElementId), elementMap);
	}
	
	public Identifiable fillElement(Reference r, Map<String, Identifiable> elementMap) {
		Identifiable e = elementMap.get(r.getId());
		
		if (r.getChildReferences()!=null) {
			if (e instanceof Nonterminal && r.getChildReferences().containsKey(Nonterminal.class.getName())) {
				Nonterminal n = (Nonterminal)e;
				n.setChildNonterminals(new ArrayList<Nonterminal>());
				for (Reference rChild : r.getChildReferences().get(Nonterminal.class.getName())) {
					n.getChildNonterminals().add((Nonterminal)fillElement(rChild, elementMap));
				}	
			} else if (e instanceof Label && r.getChildReferences().containsKey(Label.class.getName())) {
				Label l = (Label)e;
				l.setSubLabels(new ArrayList<Label>());
				for (Reference rChild : r.getChildReferences().get(Label.class.getName())) {
					l.getSubLabels().add((Label)fillElement(rChild, elementMap));
				}	
			}
			if ( (e instanceof Nonterminal || e instanceof Label) && 
					r.getChildReferences().containsKey(DescriptionGrammarImpl.class.getName())) {
				Element elem = (Element)e;
				elem.setFunctions(new ArrayList<DescriptionGrammarImpl>());
				for (Reference rChild : r.getChildReferences().get(DescriptionGrammarImpl.class.getName())) {
					elem.getFunctions().add((DescriptionGrammarImpl)fillElement(rChild, elementMap));
				}	
			}
		}
		return e;		
	}
	
	@Override
	public Element findById(String elementId) {
		return elementDao.findById(elementId);
	}
	
	/*@Override
	public void deleteByRootElementId(String rootElementId) {
		// TODO: What about the reference hierarchy?
		elementDao.delete(rootElementId);
	}
	
	@Override
	public void deleteBySchemaId(String schemaId) {
		// TODO: What about the reference hierarchy?
		
		Schema s = schemaDao.findById(schemaId);
		if (s!=null && s.getRootNonterminalId()!=null) {
			elementDao.delete(s.getRootNonterminalId());
		}
	}*/

	@Override
	public Reference saveElementHierarchy(Element e) {
		Reference rootReference = this.saveElementsInHierarchy(e);
		saveRootReference(rootReference);
		return rootReference;
	}
	
	@Override
	public Element saveElement(Element e) {
		if (e instanceof Nonterminal) {
			Nonterminal n = ((Nonterminal)e);
			List<Nonterminal> subelements = n.getChildNonterminals();
			n.setChildNonterminals(null);
			elementDao.save(e);
			
			n.setChildNonterminals(subelements);
		} else {
			Label l = ((Label)e);
			List<Label> subelements = l.getSubLabels();
			l.setSubLabels(null);
			elementDao.save(e);
			
			l.setSubLabels(subelements);			
		}
		return e;
	}
	
	private Reference saveElementsInHierarchy(Element e) {
		
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
			subelements = n.getChildNonterminals();
			n.setChildNonterminals(null);
			elementDao.save(e);
			
			n.setChildNonterminals((List<Nonterminal>)subelements);
			subelementClass = Nonterminal.class;
		} else {
			Label l = ((Label)e);
			subelements = l.getSubLabels();
			l.setSubLabels(null);
			elementDao.save(e);
			
			l.setSubLabels((List<Label>)subelements);			
			subelementClass = Label.class;
		}
				
		if (subelements!=null && subelements.size()>0) {
			r.setChildReferences(new HashMap<String, Reference[]>());
			
			Reference[] subreferences = new Reference[subelements.size()];
			for (int i=0; i<subreferences.length; i++) {
				subreferences[i] = saveElementsInHierarchy(subelements.get(i));
			}
			r.getChildReferences().put(subelementClass.getName(), subreferences);
		}
		
		// TODO Functions
		
		r.setId(e.getId());		
		return r;
	}

	@Override
	public Element createAndAppendElement(String schemaId, String parentElementId, String label) {
		String rootElementId = schemaDao.findById(schemaId).getRootNonterminalId();
		Reference rRoot = this.findRootReferenceById(rootElementId);
		Reference rParent = findSubreference(rRoot, parentElementId);
		Element eParent = elementDao.findById(parentElementId);
		
		Element element = null;
		if (rParent!=null) {
			if (eParent instanceof Nonterminal) {
				element = new Nonterminal(schemaId, this.getNonterminalLabel(label));
			} else {
				element = new Label(schemaId, this.getNonterminalLabel(label));
			}
			elementDao.save(element);
			
			addChildReference(rParent, element);
			saveRootReference(rRoot);
		}
		return element;
	}
		
	private String getNonterminalLabel(String label) {
		if (label==null || label.trim().isEmpty()) {
			return "???";
		} else {
			return label.substring(0,1).toUpperCase() + label.substring(1);
		}
	}
	
	@Override
	public Element removeElement(String schemaId, String elementId) {
		Element eRoot = findRootBySchemaId(schemaId);
		if (eRoot.getId().equals(elementId)) {
			return removeElementTree(schemaId);
		}
		Element eRemove = elementDao.findById(elementId);
		if (eRemove != null) {
			try {
				this.removeReference(eRoot.getId(), elementId);
				elementDao.delete(elementId);
				return eRemove;
			} catch (Exception e) {
				logger.warn("An error occurred while deleting an element or its references. "
						+ "The owning schema {} might be in an inconsistent state", schemaId, e);
			}
		}
		return null;
	}
	
	@Override
	public Element removeElementTree(String schemaId) {
		Schema s = schemaDao.findById(schemaId);
		Element eRoot = findRootBySchemaId(schemaId);
		if (eRoot!=null) {	
			try {
				this.removeTree(eRoot.getId());
				elementDao.delete(eRoot);
				
				s.setRootNonterminalId(null);
				schemaDao.save(s);
			} catch (IllegalArgumentException | ClassNotFoundException e) {
				logger.error("Failed to remove tree by schemaID", e);
			}
		}
		return eRoot;
	}

	@Override
	public Terminal removeTerminal(String schemaId, String terminalId) {
		Schema s = schemaDao.findById(schemaId);
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
			schemaDao.save(s);
						
			elementDao.updateMulti(
					Query.query(Criteria.where("schemaId").is(schemaId).and("terminalId").is(terminalId)), 
					Update.update("terminalId", ""));
			
			return tRemove;
		} else {
			return null;
		}
	}
}