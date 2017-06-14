package eu.dariah.de.minfba.schereg.service.base;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import eu.dariah.de.dariahsp.model.web.AuthPojo;
import eu.dariah.de.minfba.core.metamodel.LabelImpl;
import eu.dariah.de.minfba.core.metamodel.function.DescriptionGrammarImpl;
import eu.dariah.de.minfba.core.metamodel.function.TransformationFunctionImpl;
import eu.dariah.de.minfba.core.metamodel.interfaces.Element;
import eu.dariah.de.minfba.core.metamodel.interfaces.Identifiable;
import eu.dariah.de.minfba.core.metamodel.interfaces.Label;
import eu.dariah.de.minfba.core.metamodel.interfaces.MappedConcept;
import eu.dariah.de.minfba.core.metamodel.interfaces.Nonterminal;
import eu.dariah.de.minfba.core.metamodel.mapping.MappedConceptImpl;
import eu.dariah.de.minfba.schereg.dao.base.BaseDaoImpl;
import eu.dariah.de.minfba.schereg.dao.interfaces.ReferenceDao;
import eu.dariah.de.minfba.schereg.serialization.Reference;

public abstract class BaseReferenceServiceImpl extends BaseServiceImpl {
	@Autowired protected ReferenceDao referenceDao;
	
	protected Reference findReferenceById(String schemaId) {
		return referenceDao.findById(schemaId);
	}
	
	protected void saveRootReference(Reference reference) {
		referenceDao.save(reference);
	}
	
	/**
	 * Creates a reference from a provided entity and assigns it to the provided parent reference
	 * 
	 * @param parentReference - The reference to which the new subreference is added
	 * @param child - The entity for which the subreference is created
	 */
	protected Reference addChildReference(Reference parentReference, Identifiable child) {
		Assert.notNull(parentReference);
		Assert.isTrue(BaseDaoImpl.isValidObjectId(child.getId()), "Element must be saved when reference is created.");
		
		Reference childReference = new Reference(child.getId());
		addChildReference(parentReference, childReference, child.getClass().getName());
		return childReference;
	}
	
	protected void addChildReference(Reference parentReference, Reference childReference) {
		Identifiable child = referenceDao.findIdentifiableById(childReference.getId());
		if (child!=null) {
			this.addChildReference(parentReference, childReference, child.getClass().getName());
		}
	}
	
	protected void addChildReference(Reference parentReference, Reference childReference, String childClass) {
		if (parentReference.getChildReferences()==null) {
			parentReference.setChildReferences(new HashMap<String, Reference[]>());
			parentReference.getChildReferences().put(childClass, new Reference[]{ childReference });
		} else if (!parentReference.getChildReferences().containsKey(childClass)) {
			parentReference.getChildReferences().put(childClass, new Reference[]{ childReference });
		} else {
			Reference[] subRefs = parentReference.getChildReferences().get(childClass);
			Reference[] newRefs = new Reference[subRefs.length + 1];
			int i = 0;
			while (i<subRefs.length) {
				newRefs[i] = subRefs[i++];
			}
			newRefs[i] = childReference;
			parentReference.getChildReferences().put(childClass, newRefs);
		}
	}
	
	
	/**
	 * Removes the specified reference from a loaded tree, saves the tree and deletes all entities referenced by 
	 * any of the references in the removed reference subtree (not the element with the removeId, however).
	 * 
	 * @param rootReferenceId - The ID of the root reference
	 * @param removeId - The ID of the reference that needs to be removed
	 * @throws IllegalArgumentException Thrown if any of the references in the deleted subtree has an invalid ID
	 * @throws ClassNotFoundException Thrown if a class name is specified that cannot be found by the current classloader 
	 */
	protected void removeReference(String schemaId, String removeId, AuthPojo auth) throws IllegalArgumentException, ClassNotFoundException {
		Reference entityReference = referenceDao.findById(schemaId);
		Assert.notNull(entityReference);
		
		Reference removeReference = removeSubreference(entityReference, removeId);
		if (removeReference!=null) {
			// Also delete all elements that are referenced in the deleted subtree
			
			/** TODO: This requires some rework since elements could be referenced multiply 
			 *  		also in inherited schemata 
			 */
			/*Map<String, Reference[]> subordinateReferenceMap = new HashMap<String, Reference[]>();
			getAllSubordinateReferences(removeReference, subordinateReferenceMap);
			
			referenceDao.deleteAll(subordinateReferenceMap, auth.getUserId(), auth.getSessionId());
			*/
			// Delete the removable element from the tree
			referenceDao.save(entityReference);
		}
	}
	
	/**
	 * Removes the specified tree and deletes all entities referenced by within any of the references. 
	 * Does not delete the root element and does not update the schema
	 * 
	 * @param rootReferenceId - The ID of the root reference
	 * @throws IllegalArgumentException Thrown if any of the references in the deleted subtree has an invalid ID
	 * @throws ClassNotFoundException Thrown if a class name is specified that cannot be found by the current classloader 
	 */
	protected void clearReferenceTree(String schemaId, AuthPojo auth) throws IllegalArgumentException, ClassNotFoundException {
		Reference rootReference = referenceDao.findById(schemaId);
		if(rootReference==null) {
			return;
		}
		
		Map<String, Reference[]> subordinateReferenceMap = new HashMap<String, Reference[]>();
		getAllSubordinateReferences(rootReference, subordinateReferenceMap);
		
		referenceDao.deleteAll(subordinateReferenceMap, auth.getUserId(), auth.getSessionId());
		//referenceDao.delete(rootReference);
	}
	
	/**
	 * Fills the provided Map with all subordinate references mapped by their type
	 * 
	 * @param reference - The root reference for which all subreferences are collected
	 * @param subordinateReferenceMap - An initially empty map that is recursively filled
	 */
	protected static void getAllSubordinateReferences(Reference reference, Map<String, Reference[]> subordinateReferenceMap) {
		if (reference==null || subordinateReferenceMap==null) {
			return;
		}
		if (reference.getChildReferences()!=null) {
			for (String type : reference.getChildReferences().keySet()) {
				if (!subordinateReferenceMap.containsKey(type)) {
					subordinateReferenceMap.put(type, reference.getChildReferences().get(type));
				} else {
					subordinateReferenceMap.put(type, ArrayUtils.addAll(subordinateReferenceMap.get(type), reference.getChildReferences().get(type)));
				}
				for (Reference rSub : reference.getChildReferences().get(type)) {
					getAllSubordinateReferences(rSub, subordinateReferenceMap);
				}
			}
		}
	}
	
	/**
	 * Fills the provided List with the IDs of all subordinate references
	 * 
	 * @param reference - The root reference for which all subreferenced IDs are collected
	 * @param subordinateIds - An initially empty list that is recursively filled with IDs
	 */
	protected static void getAllSubordinateIds(Reference reference, List<String> subordinateIds) {
		if (reference==null || subordinateIds==null) {
			return;
		}
		if (reference.getChildReferences()!=null) {
			for (String type : reference.getChildReferences().keySet()) {
				for (Reference rSub : reference.getChildReferences().get(type)) {
					if (!subordinateIds.contains(rSub.getId())) {
						subordinateIds.add(rSub.getId());
					}					
					getAllSubordinateIds(rSub, subordinateIds);
				}
			}
		}
	}
	
	/**
	 * Finds a particular reference by its ID in a reference subtree 
	 * 
	 * @param reference - The root reference to search
	 * @param findId - The ID of the queried reference
	 * @return The found reference or NULL if nothing found 
	 */
	protected static Reference findSubreference(Reference reference, String findId) {
		if (reference.getId().equals(findId)) {
			return reference;			
		} 
		if (reference.getChildReferences()!=null) {
			Reference match;
			for (String subelemClass : reference.getChildReferences().keySet()) {
				Reference[] subelem = reference.getChildReferences().get(subelemClass);
				if (subelem != null) {
					for (Reference rSub : subelem) {
						match = findSubreference(rSub, findId);
						if (match!=null) {
							return match;
						}
					}
				}
			}
		}
		return null;
	}
		
	/**
	 * Finds a particular reference by its ID in a reference subtree and removes it from the tree.
	 * The reference is removed from the tree, which is, however, not saved by this method.
	 * 
	 * @param reference - The root reference to search
	 * @param removeId - The ID of the queried reference
	 * @return The removed reference or NULL if nothing found
	 */
	protected static Reference removeSubreference(Reference reference, String removeId) {
		Reference rRemove = null;
		if (reference.getChildReferences()!=null) {
			for (String subelemClass : reference.getChildReferences().keySet()) {
				Reference[] subelem = reference.getChildReferences().get(subelemClass);
				if (subelem != null) {
					for (int i=0; i<subelem.length; i++) {
						if (subelem[i].getId().equals(removeId)) {
							if (subelem.length==1) {
								// Remove entry if last entry
								reference.getChildReferences().remove(subelemClass);
							} else {								
								Reference[] newSubelem = new Reference[subelem.length-1];
								// Copy all subreferences except the removable one
								int j = 0;
								for (Reference rCopy : subelem) {
									if (!rCopy.equals(subelem[i])) {
										newSubelem[j++] = rCopy;
									}
								}
								reference.getChildReferences().put(subelemClass, newSubelem);
							}
							return subelem[i];
						} else {
							rRemove = removeSubreference(subelem[i], removeId);
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
	
	protected Identifiable fillElement(Reference r, Map<String, Identifiable> elementMap) {
		Identifiable e = elementMap.get(r.getId());
		
		if (r.getChildReferences()!=null) {
			if (e instanceof Nonterminal && r.getChildReferences().containsKey(Nonterminal.class.getName())) {
				Nonterminal n = (Nonterminal)e;
				n.setChildNonterminals(new ArrayList<Nonterminal>());
				for (Reference rChild : r.getChildReferences().get(Nonterminal.class.getName())) {
					n.getChildNonterminals().add((Nonterminal)fillElement(rChild, elementMap));
				}	
			} else if (e instanceof Label && r.getChildReferences().containsKey(LabelImpl.class.getName())) {
				Label l = (Label)e;
				l.setSubLabels(new ArrayList<Label>());
				for (Reference rChild : r.getChildReferences().get(LabelImpl.class.getName())) {
					l.getSubLabels().add((Label)fillElement(rChild, elementMap));
				}	
			}
			if ( (e instanceof Nonterminal || e instanceof Label) && 
					r.getChildReferences().containsKey(DescriptionGrammarImpl.class.getName())) {
				Element elem = (Element)e;
				elem.setGrammars(new ArrayList<DescriptionGrammarImpl>());
				for (Reference rChild : r.getChildReferences().get(DescriptionGrammarImpl.class.getName())) {
					elem.getGrammars().add((DescriptionGrammarImpl)fillElement(rChild, elementMap));
				}	
			}
			if (e instanceof DescriptionGrammarImpl && r.getChildReferences().containsKey(TransformationFunctionImpl.class.getName())) {
				DescriptionGrammarImpl g = (DescriptionGrammarImpl)e;
				g.setTransformationFunctions(new ArrayList<TransformationFunctionImpl>());
				for (Reference rChild : r.getChildReferences().get(TransformationFunctionImpl.class.getName())) {
					g.getTransformationFunctions().add((TransformationFunctionImpl)fillElement(rChild, elementMap));
				}	
			}
			if (e instanceof TransformationFunctionImpl && r.getChildReferences().containsKey(LabelImpl.class.getName())) {
				TransformationFunctionImpl f = (TransformationFunctionImpl)e;
				f.setOutputElements(new ArrayList<Label>());
				for (Reference rChild : r.getChildReferences().get(LabelImpl.class.getName())) {
					f.getOutputElements().add((Label)fillElement(rChild, elementMap));
				}	
			}
			
			/*if (e instanceof MappedConceptImpl && r.getChildReferences().containsKey(DescriptionGrammarImpl.class.getName())) {
				MappedConceptImpl c = (MappedConceptImpl)e;
				if (c.getElementGrammarIdsMap().size()>0) {
					c.setSourceElementMap(new HashMap<String, DescriptionGrammarImpl>());
					
					for (String element : c.getElementGrammarIdsMap().keySet()) {						
						String grammarId = c.getElementGrammarIdsMap().get(element);
						for (Reference rChild : r.getChildReferences().get(DescriptionGrammarImpl.class.getName())) {
							if (grammarId.equals(rChild.getId())) {
								c.getSourceElementMap().put(element, (DescriptionGrammarImpl)fillElement(rChild, elementMap));
								break;
							}
						}
					}
				}

			}*/
		}
		return e;		
	}
	
}
