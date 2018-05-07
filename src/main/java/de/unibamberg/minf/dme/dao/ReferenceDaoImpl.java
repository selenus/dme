package de.unibamberg.minf.dme.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import de.unibamberg.minf.dme.dao.base.BaseDao;
import de.unibamberg.minf.dme.dao.base.BaseDaoImpl;
import de.unibamberg.minf.dme.dao.base.Dao;
import de.unibamberg.minf.dme.dao.base.TrackedEntityDao;
import de.unibamberg.minf.dme.dao.interfaces.ReferenceDao;
import de.unibamberg.minf.dme.model.base.Identifiable;
import de.unibamberg.minf.dme.model.reference.Reference;

@Repository
public class ReferenceDaoImpl extends BaseDaoImpl<Reference> implements ReferenceDao, ApplicationContextAware {
	private ApplicationContext appContext;
	
	public ReferenceDaoImpl() {
		super(Reference.class);
	}
	
	@Override
	public void setApplicationContext(ApplicationContext appContext) throws BeansException {
		this.appContext = appContext;		
	}
	
	@Override
	public Reference findParentByChildId(String rootId, String childId) {
		return findParentByChildId(this.findById(rootId), childId, null);
	}
	
	@Override
	public Reference findParentByChildId(Reference reference, String childId) {
		return findParentByChildId(reference, childId, null);
	}
	
	@Override
	public Reference findParentByChildId(String rootId, String childId, List<String> parentClassNames) {
		return findParentByChildId(this.findById(rootId), childId, parentClassNames);
	}
	
	/**
	 * Method finds parent for a given childId and specified root reference.
	 * * In case no parentClassNames are specified, the immediate parent of the childId is returned (if found)
	 * * In case there are desired class names specified, only matching parents are returned; however,
	 * 		if no matching parents are found, the specified root reference will be returned irrespective of
	 * 		the provided filter
	 */
	@Override
	public Reference findParentByChildId(Reference reference, String childId, List<String> parentClassNames) {
		if (reference.getChildReferences()!=null) {
			for (String type : reference.getChildReferences().keySet()) {
				if (reference.getChildReferences().get(type)!=null) {
					for (Reference r : reference.getChildReferences().get(type)) {
						if (r.getId().equals(childId)) {
							return reference; // Returning the parent, not the found element 
						} else {
							Reference subR = findParentByChildId(r, childId, parentClassNames);
							if (subR!=null) {
								if (parentClassNames==null || parentClassNames.size()==0) {
									return subR; // No filter
								} else if (subR.equals(r)) {
									if (parentClassNames.contains(type)) {
										return subR; // Type matches
									} else {
										return reference; // Type does not match, try with next level
									}
								} else {
									return subR; // Type already validated
								}
							}
						}
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * Note: This method deletes all occurrences of a particular reference id AND ITS SUBTREES!
	 */
	@Override
	public void removeById(Reference parentReference, String matchId) {
		List<Reference> references;
		List<Reference> removeReferences;
		List<String> removeTypes;
		if (parentReference.getChildReferences()!=null) {
			removeTypes = null;
			for (String type : parentReference.getChildReferences().keySet()) {
				if (parentReference.getChildReferences().get(type)!=null) {
					references = parentReference.getChildReferences().get(type);
					removeReferences = null;
					// Collect removable references
					for (Reference r : references) {
						if (r.getId().equals(matchId)) {
							if (removeReferences==null) {
								removeReferences = new ArrayList<Reference>();
							}
							removeReferences.add(r);
						} else {
							this.removeById(r, matchId);
						}
					}
					if (removeReferences!=null) {
						// Nothing left, remove the whole child type
						if (references.size()==removeReferences.size()) {
							if (removeTypes==null) {
								removeTypes = new ArrayList<String>();
							}
							removeTypes.add(type);
						} else {
							// Recreate child reference array
							List<Reference> newRefs = new ArrayList<Reference>(references.size()-removeReferences.size());
							int j=0;
							for (int i=0; i<references.size(); i++) {
								if (!removeReferences.contains(references.get(i))) {
									newRefs.add(references.get(i));
								}
							}
							parentReference.getChildReferences().put(type, newRefs);
						}
					}
				}
			}
			if (removeTypes!=null) {
				for (String removeType : removeTypes) {
					parentReference.getChildReferences().remove(removeType);
				}
			}
		}
	}
	
	@Override
	public Reference findById(Reference reference, String referenceId) {
		if (reference.getId().equals(referenceId)) {
			return reference; // No filter
		}
		Reference result = null;
		if (reference.getChildReferences()!=null) {
			for (String type : reference.getChildReferences().keySet()) {
				if (reference.getChildReferences().get(type)!=null) {
					for (Reference r : reference.getChildReferences().get(type)) {
						result = findById(r, referenceId);
						if (result!=null) {
							return result;
						}
					}
				}
			}
		}
		return null;
	}
	
	@Override
	public void deleteAll(Map<String, Reference[]> idMap, String userId, String sessionId) throws IllegalArgumentException, ClassNotFoundException {
		if (idMap==null) {
			return;
		}
		
		Dao matchingDao;
		Reference[] deleteReferences;
		List<String> deleteIds;
		Class<?> clazz;
		for (String type : idMap.keySet()) {
			deleteReferences = idMap.get(type);
			if (deleteReferences!=null && deleteReferences.length>0) {
				clazz = Class.forName(type);
				deleteIds = new ArrayList<String>(deleteReferences.length);
				for (int i=0; i<deleteReferences.length; i++) {
					Assert.isTrue(BaseDaoImpl.isValidObjectId(deleteReferences[i].getId()));
					deleteIds.add(deleteReferences[i].getId());
				}
				
				matchingDao = getMatchingDao(clazz);
				Assert.notNull(matchingDao);
				
				int result = 0;
				if (matchingDao instanceof TrackedEntityDao) {
					result = ((TrackedEntityDao<?>)matchingDao).delete(deleteIds, userId, sessionId);
				} else {
					result = ((BaseDao<?>)matchingDao).delete(deleteIds);
				}
				logger.info("Removed {} {} entities in consequence of a delete cascade", result, clazz.getSimpleName());
			}
		}
	}
	
	@Override
	public Identifiable findIdentifiableById(String id) {
		Map<String, Dao> daos = appContext.getBeansOfType(Dao.class);
		Identifiable result = null;
		for (String key : daos.keySet()) {
			Dao dao = daos.get(key);
			if (dao instanceof BaseDao) {
				result = ((BaseDao<?>)dao).findById(id);
				if (result != null) {
					return result;
				}
			}
		}
		return null;
	}
	
	private Dao getMatchingDao(Class<?> entityType) {
		Map<String, Dao> daos = appContext.getBeansOfType(Dao.class);
		for (String key : daos.keySet()) {
			Dao dao = daos.get(key);
			if (dao.getClazz().isAssignableFrom(entityType)) {
				return dao;
			}
		}
		return null;
	}

	@Override
	public List<Reference> findParentsByChildId(String rootId, String childId) {
		return this.findParentsByChildId(this.findById(rootId), childId, null);
	}
	
	@Override
	public List<Reference> findParentsByChildId(Reference reference, String childId, List<String> parentClassNames) {
		if (reference.getChildReferences()!=null) {
			List<Reference> result = new ArrayList<Reference>();
			for (String type : reference.getChildReferences().keySet()) {
				if (reference.getChildReferences().get(type)!=null) {
					for (Reference r : reference.getChildReferences().get(type)) {
						if (r.getId().equals(childId)) {
							result.add(reference); // Returning the parent, not the found element 
						} else {
							List<Reference> subRs = findParentsByChildId(r, childId, parentClassNames);
							if (subRs!=null) {
								for (Reference subR : subRs) {
									
									if (parentClassNames==null || parentClassNames.size()==0) {
										result.add(subR); // No filter
									} else if (subR.equals(r)) {
										if (parentClassNames.contains(type)) {
											result.add(subR); // Type matches
										} /*else {
											return reference; // Type does not match, try with next level
										} */
									} else {
										result.add(subR); // Type already validated
									}
									
								}
							}
						}
					}
				}
			}
			return result;
		}
		return null;
	}
}