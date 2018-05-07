package de.unibamberg.minf.dme.service.base;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import de.unibamberg.minf.dme.dao.base.BaseDaoImpl;
import de.unibamberg.minf.dme.dao.interfaces.ReferenceDao;
import de.unibamberg.minf.dme.model.base.Identifiable;
import de.unibamberg.minf.dme.model.reference.Reference;
import de.unibamberg.minf.dme.model.reference.ReferenceHelper;
import eu.dariah.de.dariahsp.model.web.AuthPojo;

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
	protected static Reference addChildReference(Reference parentReference, Identifiable child) {
		Assert.notNull(parentReference);
		Assert.isTrue(BaseDaoImpl.isValidObjectId(child.getId()), "Element must be saved when reference is created.");
		
		Reference childReference = new Reference(child.getId());
		ReferenceHelper.addChildReference(parentReference, childReference, child.getClass().getName());
		return childReference;
	}
	
	protected void addChildReference(Reference parentReference, Reference childReference) {
		Identifiable child = referenceDao.findIdentifiableById(childReference.getId());
		if (child!=null) {
			ReferenceHelper.addChildReference(parentReference, childReference, child.getClass().getName());
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
		
		Reference removeReference = ReferenceHelper.removeSubreference(entityReference, removeId);
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
		
		Map<String, List<Reference>> subordinateReferenceMap = new HashMap<String, List<Reference>>();
		ReferenceHelper.getAllSubordinateReferences(rootReference, subordinateReferenceMap);
		
		//referenceDao.deleteAll(subordinateReferenceMap, auth.getUserId(), auth.getSessionId());
		//referenceDao.delete(rootReference);
	}	
}
