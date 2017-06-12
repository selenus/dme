package eu.dariah.de.minfba.schereg.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import eu.dariah.de.dariahsp.model.web.AuthPojo;
import eu.dariah.de.minfba.core.metamodel.BaseElement;
import eu.dariah.de.minfba.core.metamodel.LabelImpl;
import eu.dariah.de.minfba.core.metamodel.NonterminalImpl;
import eu.dariah.de.minfba.core.metamodel.SchemaImpl;
import eu.dariah.de.minfba.core.metamodel.exception.MetamodelConsistencyException;
import eu.dariah.de.minfba.core.metamodel.function.DescriptionGrammarImpl;
import eu.dariah.de.minfba.core.metamodel.function.GrammarContainer;
import eu.dariah.de.minfba.core.metamodel.function.TransformationFunctionImpl;
import eu.dariah.de.minfba.core.metamodel.interfaces.Element;
import eu.dariah.de.minfba.core.metamodel.interfaces.Identifiable;
import eu.dariah.de.minfba.core.metamodel.interfaces.Label;
import eu.dariah.de.minfba.core.metamodel.interfaces.Nonterminal;
import eu.dariah.de.minfba.core.metamodel.interfaces.Schema;
import eu.dariah.de.minfba.core.metamodel.interfaces.SchemaNature;
import eu.dariah.de.minfba.core.metamodel.interfaces.Terminal;
import eu.dariah.de.minfba.core.metamodel.mapping.MappedConceptImpl;
import eu.dariah.de.minfba.core.metamodel.xml.XmlSchemaNature;
import eu.dariah.de.minfba.core.metamodel.xml.XmlTerminal;
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

@Service
public class ElementServiceImpl extends BaseReferenceServiceImpl implements ElementService {
	@Autowired private ElementDao elementDao;
	@Autowired private SchemaDao schemaDao;
	@Autowired private GrammarDao grammarDao;
	@Autowired private FunctionDao functionDao;
	@Autowired private MappedConceptDao mappedConceptDao;
	
	// TODO Get rid of this once we have a dedicated importer for json
	@Autowired private GrammarService grammarService;
	
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
			if (reference.getChildReferences().containsKey(Nonterminal.class.getName()) &&
					reference.getChildReferences().get(Nonterminal.class.getName()).length>0 ) {
				if (reference.getChildReferences().get(Nonterminal.class.getName()).length==1) {
					rootElementReference = reference.getChildReferences().get(Nonterminal.class.getName())[0];
				} else {
					for (int i=0; i<reference.getChildReferences().get(Nonterminal.class.getName()).length; i++) {
						if (reference.getChildReferences().get(Nonterminal.class.getName())[i].isRoot()) {
							rootElementReference = reference.getChildReferences().get(Nonterminal.class.getName())[i];
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
			for (DescriptionGrammarImpl g : searchElement.getGrammars()) {
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
		List<Element> saveElements = new ArrayList<Element>();	
		Reference r = this.saveElementsInHierarchy(e, saveElements);
		
		elementDao.saveNew(saveElements, auth.getUserId(), auth.getSessionId());
		
		return r;
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
	
	private Reference saveElementsInHierarchy(Element e, List<Element> saveElements) {
		
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
		
		if (e.getId()==null) {
			e.setId(DaoImpl.createNewObjectId());
		}
		
		if (e instanceof Nonterminal) {
			Nonterminal n = ((Nonterminal)e);
			n.setName(getNormalizedName(n.getName()));
			subelements = n.getChildNonterminals();
			n.setChildNonterminals(null);
			//elementDao.save(e, auth.getUserId(), auth.getSessionId());
			saveElements.add(e);
			
			n.setChildNonterminals((List<Nonterminal>)subelements);
			subelementClass = Nonterminal.class;
		} else {
			Label l = ((Label)e);
			l.setName(getNormalizedName(l.getName()));
			subelements = l.getSubLabels();
			l.setSubLabels(null);
			//elementDao.save(e, auth.getUserId(), auth.getSessionId());
			saveElements.add(e);
			
			l.setSubLabels((List<Label>)subelements);			
			subelementClass = Label.class;
		}
		
		// TODO: Collect grammars and functions just like elements to batch save?
		if (e.getGrammars()!=null) {
			Reference[] gSubrefs = new Reference[e.getGrammars().size()];
			DescriptionGrammarImpl g;
			for (int i=0; i<e.getGrammars().size(); i++) {
				g = e.getGrammars().get(i);
				grammarService.saveGrammar(g, null);
				gSubrefs[i] = new Reference(g.getId());
				if (g.getTransformationFunctions()!=null) {
					
					Reference[] fSubrefs = new Reference[g.getTransformationFunctions().size()];
					TransformationFunctionImpl f;
					for (int j=0; j<g.getTransformationFunctions().size(); j++) {
						f = g.getTransformationFunctions().get(j);
						functionDao.save(f);
						fSubrefs[j] = new Reference(f.getId());
						
						if (f.getOutputElements()!=null) {
							Reference[] labelReferences = new Reference[f.getOutputElements().size()];
							for (int k=0; k<f.getOutputElements().size(); k++) {
								labelReferences[k] = this.saveElementsInHierarchy(f.getOutputElements().get(k), saveElements);
							}
							fSubrefs[j].setChildReferences(new HashMap<String, Reference[]>());
							fSubrefs[j].getChildReferences().put(Label.class.getName(), labelReferences);
						}
						gSubrefs[i].setChildReferences(new HashMap<String, Reference[]>());
						gSubrefs[i].getChildReferences().put(TransformationFunctionImpl.class.getName(), fSubrefs);
					}
					
				}
				if (r.getChildReferences()==null) {
					r.setChildReferences(new HashMap<String, Reference[]>());
				}
				r.getChildReferences().put(DescriptionGrammarImpl.class.getName(), gSubrefs);
			}
			
			
			
		}
				
		if (subelements!=null && subelements.size()>0) {
			if (r.getChildReferences()==null) {
				r.setChildReferences(new HashMap<String, Reference[]>());
			}
			
			Reference[] subreferences = new Reference[subelements.size()];
			for (int i=0; i<subreferences.length; i++) {
				subreferences[i] = saveElementsInHierarchy(subelements.get(i), saveElements);
			}
			r.getChildReferences().put(subelementClass.getName(), subreferences);
		}
		
		
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
		
		List<XmlTerminal> terminals = s.getNature(XmlSchemaNature.class).getTerminals();
		
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
					
					s.getNature(XmlSchemaNature.class).removeTerminalFromMap(tRemove.getId());
					
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
	public List<Nonterminal> extractAllNonterminals(Nonterminal root) {
		List<Nonterminal> result = new ArrayList<Nonterminal>();
		if (root!=null) {
			result.add(root);
			if (root.getChildNonterminals()!=null) {
				for (Nonterminal childN : root.getChildNonterminals()) {
					result.addAll(this.extractAllNonterminals(childN));
				}
			}
		}
		return result;
	}

	@Override
	public void regenerateIds(SchemaNature nature, String entityId, Element element, Map<String, String> terminalIdMap, Map<String, GrammarContainer> grammarContainerMap) throws MetamodelConsistencyException {
		element.setEntityId(entityId);
		element.setId(null);
		if (element instanceof Nonterminal) {
			String terminalId = nature.getTerminalId(element.getId());
			nature.mapNonterminal(element.getId(), terminalIdMap.get(terminalId));
		}
		List<Element> children = element.getAllChildElements();
		if (children!=null) {
			for (Element child : children) {
				this.regenerateIds(nature, entityId, child, terminalIdMap, grammarContainerMap);
			}
		}
		if (element.getGrammars()!=null) {
			for (DescriptionGrammarImpl g : element.getGrammars()) {
				g.setEntityId(entityId);
				
				if (grammarContainerMap!=null && grammarContainerMap.containsKey(g.getId())) {
					g.setGrammarContainer(grammarContainerMap.get(g.getId()));
				}
				g.setId(null);
				
				
				if (g.getTransformationFunctions()!=null) {
					for (TransformationFunctionImpl f : g.getTransformationFunctions()) {
						f.setEntityId(entityId);
						f.setId(null);
					}
				}
			}
		}
	}

	@Override
	public Map<String, String> regenerateIds(String entityId, List<? extends Terminal> terminals) {
		if (terminals==null) {
			return null;
		}
		Map<String, String> terminalIdMap = new HashMap<String, String>();
		String idOld;
		for (Terminal xt : terminals) {
			idOld = xt.getId();
			xt.setId(new ObjectId().toString());
			terminalIdMap.put(idOld, xt.getId());
		}
		return terminalIdMap;
	}
}