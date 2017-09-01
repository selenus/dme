package eu.dariah.de.minfba.schereg.dao.interfaces;

import java.util.List;
import java.util.Map;

import de.unibamberg.minf.dme.model.base.BaseIdentifiable;
import de.unibamberg.minf.dme.model.base.Identifiable;
import eu.dariah.de.minfba.schereg.dao.base.BaseDao;
import eu.dariah.de.minfba.schereg.serialization.Reference;

public interface ReferenceDao extends BaseDao<Reference> {
	/**
	 * Removes entities from the database according to the reference map. 
	 * Keys are thereby utilized to identify entityClass and collectionName properties of delete actions, 
	 * the values contain the removable IDs.
	 * 
	 * @param referenceMap - A map with class names as keys and reference arrays as values
	 * @throws IllegalArgumentException Thrown if any of the specified references has an invalid ID
	 * @throws ClassNotFoundException Thrown if a key specifies a class name that cannot be found by the current classloader 
	 */
	public void deleteAll(Map<String, Reference[]> referenceMap, String userId, String sessionId) throws IllegalArgumentException, ClassNotFoundException;

	public Reference findParentByChildId(String rootId, String childId);
	public Reference findParentByChildId(Reference reference, String childId);

	public Reference findParentByChildId(Reference reference, String childId, List<String> parentClassNames);
	public Reference findParentByChildId(String rootId, String childId, List<String> parentClassNames);

	public Identifiable findIdentifiableById(String id);

	public Reference findById(Reference root, String referenceId);
	
	public List<Reference> findParentsByChildId(Reference reference, String childId, List<String> parentClassNames);
	public List<Reference> findParentsByChildId(String rootId, String childId);
}
