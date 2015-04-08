package eu.dariah.de.minfba.schereg.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import eu.dariah.de.minfba.core.metamodel.interfaces.Identifiable;
import eu.dariah.de.minfba.schereg.dao.ReferenceDao;
import eu.dariah.de.minfba.schereg.dao.base.BaseDaoImpl;
import eu.dariah.de.minfba.schereg.serialization.Reference;

public abstract class BaseReferenceServiceImpl {
	@Autowired private ReferenceDao referenceDao;
	
	protected Reference findRootReferenceById(String referenceId) {
		return referenceDao.findById(referenceId);
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
	protected static void addChildReference(Reference parentReference, Identifiable child) {
		Assert.notNull(parentReference);
		Assert.isTrue(BaseDaoImpl.isValidObjectId(child.getId()), "Element must be saved when reference is created.");
		
		if (parentReference.getChildReferences()==null) {
			parentReference.setChildReferences(new HashMap<String, Reference[]>());
			parentReference.getChildReferences().put(child.getClass().getName(), new Reference[]{ new Reference(child.getId()) });
		} else if (!parentReference.getChildReferences().containsKey(child.getClass().getName())) {
			parentReference.getChildReferences().put(child.getClass().getName(), new Reference[]{ new Reference(child.getId()) });
		} else {
			Reference[] subRefs = parentReference.getChildReferences().get(child.getClass().getName());
			Reference[] newRefs = new Reference[subRefs.length + 1];
			int i = 0;
			while (i<subRefs.length) {
				newRefs[i] = subRefs[i++];
			}
			newRefs[i] = new Reference(child.getId());
			parentReference.getChildReferences().put(child.getClass().getName(), newRefs);
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
	protected void removeReference(String rootReferenceId, String removeId) throws IllegalArgumentException, ClassNotFoundException {
		Reference rootReference = referenceDao.findById(rootReferenceId);
		Assert.notNull(rootReference);
		
		Reference removeReference = removeSubreference(rootReference, removeId);
		if (removeReference!=null) {
			// Also delete all elements that are referenced in the deleted subtree
			Map<String, Reference[]> subordinateReferenceMap = new HashMap<String, Reference[]>();
			getAllSubordinateReferences(removeReference, subordinateReferenceMap);
			
			referenceDao.deleteAll(subordinateReferenceMap);
			
			// Delete the removable element from the tree
			referenceDao.delete(removeReference);
		}
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
	
	
	
}
