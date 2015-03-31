package eu.dariah.de.minfba.schereg.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.dariah.de.minfba.core.metamodel.Label;
import eu.dariah.de.minfba.core.metamodel.Nonterminal;
import eu.dariah.de.minfba.core.metamodel.interfaces.Element;
import eu.dariah.de.minfba.core.metamodel.interfaces.Schema;
import eu.dariah.de.minfba.schereg.dao.ElementDao;
import eu.dariah.de.minfba.schereg.dao.ReferenceDao;
import eu.dariah.de.minfba.schereg.dao.SchemaDao;
import eu.dariah.de.minfba.schereg.serialization.Reference;

@Service
public class ElementServiceImpl implements ElementService {
	@Autowired private ElementDao elementDao;
	@Autowired private SchemaDao schemaDao;
	@Autowired private ReferenceDao referenceDao;
	
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
		
		List<Element> elements = elementDao.findBySchemaId(root.getSchemaId());
		Map<String, Element> elementMap = new HashMap<String, Element>(elements.size()); 
		for (Element e : elements) {
			elementMap.put(e.getId(), e);
		}
		
		Reference r = referenceDao.findById(rootElementId);
		return fillElement(r, elementMap);
	}
	
	public Element fillElement(Reference r, Map<String, Element> elementMap) {
		Element e = elementMap.get(r.getId());
		
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
		}
		return e;		
	}
	
	@Override
	public Element findById(String elementId) {
		return elementDao.findById(elementId);
	}
	
	@Override
	public void deleteByRootElementId(String rootElementId) {
		elementDao.delete(rootElementId);
	}
	
	@Override
	public void deleteBySchemaId(String schemaId) {
		Schema s = schemaDao.findById(schemaId);
		if (s!=null && s.getRootNonterminalId()!=null) {
			elementDao.delete(s.getRootNonterminalId());
		}
	}

	@Override
	public Reference saveElementHierarchy(Element e) {
		return referenceDao.save(this.saveElementsInHierarchy(e));
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
	public Element createAndAppendElement(String schemaId, String parentElementId) {
		Element eRoot = findRootBySchemaId(schemaId);
		Element eParent = elementDao.findById(parentElementId);
		Reference rRoot = referenceDao.findById(eRoot.getId());
		Reference rParent = findSubreference(rRoot, parentElementId);
		
		Element eNew = null;
		if (rParent!=null) {
			if (eParent instanceof Nonterminal) {
				eNew = new Nonterminal(schemaId, "new Nonterminal...");
			} else {
				eNew = new Label(schemaId, "new Label...");
			}
			elementDao.save(eNew);
			
			if (rParent.getChildReferences()==null) {
				rParent.setChildReferences(new HashMap<String, Reference[]>());
			}
			if (!rParent.getChildReferences().containsKey(eNew.getClass().getName())) {
				rParent.getChildReferences().put(eNew.getClass().getName(), new Reference[]{ new Reference(eNew.getId()) });
			} else {
				Reference[] subRefs = rParent.getChildReferences().get(eNew.getClass().getName());
				Reference[] newRefs = new Reference[subRefs.length + 1];
				int i = 0;
				while (i<subRefs.length) {
					newRefs[i] = subRefs[i++];
				}
				newRefs[i] = new Reference(eNew.getId());
				rParent.getChildReferences().put(eNew.getClass().getName(), newRefs);
			}
			
			referenceDao.save(rRoot);
		}
		return eNew;
	}
	
	@Override
	public Element removeElement(String schemaId, String elementId) {
		Element eRoot = findRootBySchemaId(schemaId);
		Reference rRoot = referenceDao.findById(eRoot.getId());
		Element eRemove = elementDao.findById(elementId);
		
		Reference rRemove = removeSubreference(rRoot, elementId); 
		if (rRemove != null) {
			// remove the selected element
			elementDao.delete(elementId);
			
			// save the modified reference tree
			referenceDao.save(rRoot);
			
			// based on the removed reference, find all subordinate elements and remove them
			Map<String, Reference[]> removeMap = new HashMap<String, Reference[]>();
			getAllSubordinateReferences(removeMap, rRemove);
			for (String type : removeMap.keySet()) {
				if (type.equals(Nonterminal.class.getName()) || type.equals(Label.class.getName())) {
					for (Reference rDel : removeMap.get(type)) {
						elementDao.delete(rDel.getId());
					}
				}
			}			
		}
		return eRemove;
	}
	
	public void getAllSubordinateReferences(Map<String, Reference[]> result, Reference reference) {
		if (reference.getChildReferences()!=null) {
			for (String type : reference.getChildReferences().keySet()) {
				if (!result.containsKey(type)) {
					result.put(type, reference.getChildReferences().get(type));
				} else {
					result.put(type, ArrayUtils.addAll(result.get(type), reference.getChildReferences().get(type)));
				}
				for (Reference rSub : reference.getChildReferences().get(type)) {
					getAllSubordinateReferences(result, rSub);
				}
			}
		}
	}
		
	private Reference removeSubreference(Reference parent, String childId) {
		Reference rRemove = null;
		if (parent.getChildReferences()!=null) {
			for (String subelemClass : parent.getChildReferences().keySet()) {
				Reference[] subelem = parent.getChildReferences().get(subelemClass);
				if (subelem != null) {
					for (int i=0; i<subelem.length; i++) {
						if (subelem[i].getId().equals(childId)) {
							if (subelem.length==1) {
								parent.getChildReferences().remove(subelemClass);
							} else {
								Reference[] newSubelem = new Reference[subelem.length-1];
								int j = 0;
								for (Reference rCopy : subelem) {
									if (!rCopy.equals(subelem[i])) {
										newSubelem[j++] = rCopy;
									}
								}
								parent.getChildReferences().put(subelemClass, newSubelem);
							}
							return subelem[i];
						} else {
							rRemove = this.removeSubreference(subelem[i], childId);
							if (rRemove!=null) {
								return rRemove;
							}
						}
					}
				}
			}
		}
		return rRemove;
	}
	
	private Reference findSubreference(Reference tree, String id) {
		if (tree.getId().equals(id)) {
			return tree;
		} else if (tree.getChildReferences()!=null) {
			Reference match;
			for (String subelemClass : tree.getChildReferences().keySet()) {
				Reference[] subelem = tree.getChildReferences().get(subelemClass);
				if (subelem != null) {
					for (Reference rSub : subelem) {
						match = this.findSubreference(rSub, id);
						if (match!=null) {
							return match;
						}
					}
				}
			}
		}
		return null;
	}
}