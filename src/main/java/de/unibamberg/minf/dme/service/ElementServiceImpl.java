package de.unibamberg.minf.dme.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import de.unibamberg.minf.dme.dao.base.DaoImpl;
import de.unibamberg.minf.dme.dao.interfaces.ElementDao;
import de.unibamberg.minf.dme.dao.interfaces.FunctionDao;
import de.unibamberg.minf.dme.dao.interfaces.GrammarDao;
import de.unibamberg.minf.dme.dao.interfaces.MappedConceptDao;
import de.unibamberg.minf.dme.dao.interfaces.SchemaDao;
import de.unibamberg.minf.dme.exception.GenericScheregException;
import de.unibamberg.minf.dme.model.RightsContainer;
import de.unibamberg.minf.dme.model.base.BaseElement;
import de.unibamberg.minf.dme.model.base.Element;
import de.unibamberg.minf.dme.model.base.Function;
import de.unibamberg.minf.dme.model.base.Grammar;
import de.unibamberg.minf.dme.model.base.Identifiable;
import de.unibamberg.minf.dme.model.base.Label;
import de.unibamberg.minf.dme.model.base.ModelElement;
import de.unibamberg.minf.dme.model.base.Nonterminal;
import de.unibamberg.minf.dme.model.base.Terminal;
import de.unibamberg.minf.dme.model.datamodel.LabelImpl;
import de.unibamberg.minf.dme.model.datamodel.NonterminalImpl;
import de.unibamberg.minf.dme.model.datamodel.base.Datamodel;
import de.unibamberg.minf.dme.model.datamodel.base.DatamodelNature;
import de.unibamberg.minf.dme.model.datamodel.natures.XmlDatamodelNature;
import de.unibamberg.minf.dme.model.datamodel.natures.xml.XmlTerminal;
import de.unibamberg.minf.dme.model.exception.MetamodelConsistencyException;
import de.unibamberg.minf.dme.model.mapping.MappedConceptImpl;
import de.unibamberg.minf.dme.model.serialization.Reference;
import de.unibamberg.minf.dme.service.base.BaseReferenceServiceImpl;
import de.unibamberg.minf.dme.service.interfaces.ElementService;
import de.unibamberg.minf.dme.service.interfaces.IdentifiableService;
import eu.dariah.de.dariahsp.model.web.AuthPojo;

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
					this.moveMainReferencesToProcessedRoot(rootRef);
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
		element.setId(null);
		element.setProcessingRoot(true);
		
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
			if (!result.contains(root)) {
				result.add(root);
			}
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

	private Reference findCloneReference(Reference root, String elementId) {
		List<Reference> parents = referenceDao.findParentsByChildId(root, elementId, null);
		for (Reference parent : parents) {
			for (String type : parent.getChildReferences().keySet()) {
				for (Reference child : parent.getChildReferences().get(type)) {
					if (child.getId().equals(elementId) && !child.isReuse()) {
						return child;
					}
				}
			}
		}
		return null;
	}
	
	@Override
	public void cloneElement(String elementId, String[] path, AuthPojo auth) {
		Element e = elementDao.findById(elementId);
		RightsContainer<Datamodel> m = schemaDao.findById(e.getEntityId());
		Reference rRoot = referenceDao.findById(e.getEntityId());
		
		/* Find subtree that should be cloned. The subtree might be any other occurance 
		 *  of the reused element or the to-clone element itself */
		Reference rCloneRef = this.findCloneReference(rRoot, elementId);
		
		/* Within the whole datamodel count the occurence of any element
		 *  This is required in order to determine when to stop clone
		 *  (as soon as a reoccurring subelement is found) */
		Map<String, Integer> referenceUsageMap = new HashMap<String, Integer>();
		this.countReferenceUsage(rRoot, referenceUsageMap);
		

		/* Load all elements of the datamodel in order to fill the element tree to clone
		 *  TODO: We could also not load all elements but only the ones within the relevant rCloneRef */
		List<Identifiable> elements = this.getAllElements(e.getEntityId());		
		Map<String, Identifiable> elementMap = new HashMap<String, Identifiable>(elements.size()); 
		for (Identifiable idE : elements) {
			elementMap.put(idE.getId(), idE);
		}
		
		// Fill element hierarchy for our clone blueprint
		Element eCloneRef = (Element)fillElement(rCloneRef, elementMap);
		
		/* Track association between original element ids and cloned nonterminals
		 *  This is required to later associate the new nonterminal ids to 
		 *  terminal ids in the datamodel natures */
		Map<String, Nonterminal> originalIdClonedNonterminalMap = new HashMap<String, Nonterminal>();
		
		// Clone the hierarchy keeping 
		ModelElement clonedE = this.cloneElementHierarchy(eCloneRef, referenceUsageMap, originalIdClonedNonterminalMap);
		
		// Save all cloned elements receiving a reference to put in the existing tree later 
		Reference clonedR = identifiableService.saveHierarchy(clonedE, auth, true);
		
		// Reassign terminal ids to cloned nonterminals
		this.assignTerminalIdsToClones(m.getElement(), originalIdClonedNonterminalMap);
		
		// Set references for the element to clone and its parent
		Reference rParent = rRoot;
		for (int i=0; i<path.length-1; i++) {
			rParent = this.navigateChild(rParent, path[i]);
		}
		
		// Replace old reused with cloned child in reference tree
		this.replaceClonedChild(rParent, elementId, clonedR);
		
		
		// Save reference tree and schema (terminalId mappings)
		this.referenceDao.save(rRoot);
		this.schemaDao.save(m);
	}
	
	@Override
	public void moveMainReferencesToProcessedRoot(Reference entityReference) {
		if (entityReference.getChildReferences()==null) {
			return;
		}
		
		Reference rootRef = null;
		List<Reference> altRoots = new ArrayList<Reference>();
		for (String type : entityReference.getChildReferences().keySet()) {
			for (Reference rChild : entityReference.getChildReferences().get(type)) {
				// Either first or designated root is assumed root
				if (rootRef==null || rChild.isRoot()) {
					rootRef = rChild;
				} else {
					altRoots.add(rChild);
				}
			}
		}
		
		if (rootRef==null || altRoots.size()==0) {
			// Nothing to move
			return;
		}
		
		for (String type : rootRef.getChildReferences().keySet()) {
			for (Reference r : rootRef.getChildReferences().get(type)) {
				this.detectAndMoveMissingReference(r, rootRef, rootRef, altRoots);
			}
		}
		
	}
	
	private void detectAndMoveMissingReference(Reference r, Reference parent, Reference root, List<Reference> altRoots) {
		
		if (r.isReuse()) {
			Reference rCloneRef = this.findReusedReferenceParent(root, r.getId());
			Reference rSwitch;
			if (rCloneRef==null) {
				// Need to move
				for (Reference altRoot : altRoots) {
					rCloneRef = this.findReusedReferenceParent(altRoot, r.getId());
					if (rCloneRef!=null) {
						rSwitch = this.navigateChild(rCloneRef, r.getId());
						
						this.replaceClonedChild(rCloneRef, r.getId(), r);
						this.replaceClonedChild(parent, r.getId(), rSwitch);
						
						logger.debug("Switched hidden reused nonterminal reference to visible position");
					}
				}
			}
			
		}
		
		if (r.getChildReferences()!=null && r.getChildReferences().size()>0) {
			for (String type : r.getChildReferences().keySet()) {
				for (Reference rChild : r.getChildReferences().get(type)) {
					this.detectAndMoveMissingReference(rChild, r, root, altRoots);
				}
			}
		}
		
	}
	
	private Reference findReusedReferenceParent(Reference searchRoot, String elementId) {
		if (searchRoot.getChildReferences()!=null) {
			Reference r;
			for (String type : searchRoot.getChildReferences().keySet()) {
				for (Reference rChild : searchRoot.getChildReferences().get(type)) {
					if (rChild.getId().equals(elementId) && !rChild.isReuse()) {
						return searchRoot;
					}
					r = this.findReusedReferenceParent(rChild, elementId);
					if (r!=null) {
						return r;
					}
				}
			}
		}
		return null;
	}
	
	private void assignTerminalIdsToClones(Datamodel m, Map<String, Nonterminal> originalIdClonedNonterminalMap) {
		String terminalId;
		for (String originalId : originalIdClonedNonterminalMap.keySet()) {
			for (DatamodelNature sn : m.getNatures()) {
				terminalId = sn.getTerminalId(originalId);
				if (terminalId!=null) {
					try {
						sn.mapNonterminal(originalIdClonedNonterminalMap.get(originalId).getId(), terminalId);
					} catch (MetamodelConsistencyException e1) {
						logger.warn("Failed to reassign terminal to cloned nonterminal");
					}
				} else {
					logger.warn("No terminal id detected for existing nonterminal [{}]", originalId);
				}
			}
			
		}
	}
	
	private ModelElement cloneElementHierarchy(ModelElement me, Map<String, Integer> referenceUsageMap, Map<String, Nonterminal> originalIdClonedNonterminalMap) {
		if (me==null) {
			return null;
		}
		if (Grammar.class.isAssignableFrom(me.getClass())) {
			Grammar g = (Grammar)me;
			if (!g.isPassthrough() && g.getGrammarContainer()==null) {
				g.setGrammarContainer(grammarDao.findById(me.getId()).getGrammarContainer());
			}
		}
		
		ModelElement clone = me.cloneElement();
		if (Element.class.isAssignableFrom(me.getClass())) {
			((Element)clone).setGrammars(this.cloneElementList(((Element)me).getGrammars(), referenceUsageMap, originalIdClonedNonterminalMap));
			if (Nonterminal.class.isAssignableFrom(me.getClass())) {
				originalIdClonedNonterminalMap.put(me.getId(), (Nonterminal)clone);
				((Nonterminal)clone).setChildNonterminals(this.cloneElementList(((Nonterminal)me).getChildNonterminals(), referenceUsageMap, originalIdClonedNonterminalMap));
			} else {
				((Label)clone).setSubLabels(this.cloneElementList(((Label)me).getSubLabels(), referenceUsageMap, originalIdClonedNonterminalMap));
			}
		} else if (Grammar.class.isAssignableFrom(me.getClass())) {
			((Grammar)clone).setFunctions(this.cloneElementList(((Grammar)me).getFunctions(), referenceUsageMap, originalIdClonedNonterminalMap));
		} else if (Function.class.isAssignableFrom(me.getClass())) {
			((Function)clone).setOutputElements(this.cloneElementList(((Function)me).getOutputElements(), referenceUsageMap, originalIdClonedNonterminalMap));
		}
		return clone;
	}

	
	@SuppressWarnings("unchecked")
	private <T extends ModelElement> List<T> cloneElementList(List<T> labels, Map<String, Integer> referenceUsageMap, Map<String, Nonterminal> originalIdClonedNonterminalMap) {
		List<T> clones = null;

		if (labels!=null) {
			clones = new ArrayList<T>(labels.size());
			for (T childL : labels) {
				if (referenceUsageMap.containsKey(childL.getId()) && referenceUsageMap.get(childL.getId()).intValue()>1) {
					// Another reuse here: Cloning terminates
					clones.add(childL);
				} else {
					clones.add((T)this.cloneElementHierarchy(childL, referenceUsageMap, originalIdClonedNonterminalMap));
				}
			}
		}
		return clones;
	}
	
	
	private void countReferenceUsage(Reference parent, Map<String, Integer> referenceUsageMap) {
		if (!referenceUsageMap.containsKey(parent.getId())) {
			referenceUsageMap.put(parent.getId(), new Integer(1));
		} else {
			referenceUsageMap.put(parent.getId(), new Integer(referenceUsageMap.get(parent.getId())+1));
		}
		if (parent.getChildReferences()!=null) {
			for (String type : parent.getChildReferences().keySet()) {
				for (Reference rChild : parent.getChildReferences().get(type)) {
					countReferenceUsage(rChild, referenceUsageMap);
				}
			}
		}
	}
	
	private Reference navigateChild(Reference parent, String childId) {
		for (String type : parent.getChildReferences().keySet()) {
			for (Reference r : parent.getChildReferences().get(type)) {
				if (r.getId().equals(childId)) {
					return r;
				}
			}
		}
		return null;
	}
	
	private void replaceClonedChild(Reference parent, String originalChildId, Reference clonedChild) {
		for (String type : parent.getChildReferences().keySet()) {
			Reference[] refs = parent.getChildReferences().get(type);
			for (int i=0; i<refs.length; i++) {
				if (refs[i].getId().equals(originalChildId)) {
					refs[i] = clonedChild;
					return;
				}
			}
		}
	}
}