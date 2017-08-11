package eu.dariah.de.minfba.schereg.dao.base;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import de.unibamberg.minf.dme.model.base.Identifiable;
import de.unibamberg.minf.dme.model.tracking.TrackedEntity;
import eu.dariah.de.minfba.schereg.exception.GenericScheregException;
import eu.dariah.de.minfba.schereg.model.RightsContainer;

public class RightsAssignedObjectDaoImpl<T extends TrackedEntity> extends TrackedEntityDaoImpl<RightsContainer<T>> implements RightsAssignedObjectDao<T> {

	public RightsAssignedObjectDaoImpl(Class<?> clazz) {
		super(clazz);
	}
	
	public RightsAssignedObjectDaoImpl(Class<?> clazz, String collectionName) {
		super(clazz, collectionName);
	}

	@Override
	public List<RightsContainer<T>> findAll() {
		Query q = new Query();
		q.fields().exclude("element.namespaces");
		q.addCriteria(Criteria.where("draft").is(false));
		
		return this.find(q);
	}
	
	@Override
	public List<T> findAllEnclosed() {
		List<RightsContainer<T>> wrapped = this.findAll();
		List<T> result = new ArrayList<T>(wrapped.size());
		for (RightsContainer<T> w : wrapped) {
			result.add(w.getElement());
		}
		return result;
	}
	
	@Override
	public T findEnclosedById(String id) {
		RightsContainer<T> e = this.findById(id);
		if (e!=null) {
			return e.getElement();
		}
		return null;
	}
	
	@Override
	public List<RightsContainer<T>> findAllByUserId(String userId) {
		Query q = new Query();
		/* User can see	- all no-draft objects
		 * 				- all owned objects
		 * 				- all shared drafts
		 */
		Criteria cNoDraft = Criteria.where("draft").is(false);
		if (userId==null) {
			q.addCriteria(cNoDraft);
		} else {
			Criteria cOwner = Criteria.where("ownerId").is(userId);
			Criteria sharedDraft = Criteria.where("readIds").is(userId);
			q.addCriteria(new Criteria().orOperator(cOwner, cNoDraft, sharedDraft));
		}
		return this.find(q);
	}
	
	@Override
	public RightsContainer<T> findByIdAndUserId(String id, String userId) {
		return findByIdAndUserId(id, userId, false);
	}
	
	@Override
	public RightsContainer<T> findByIdAndUserId(String id, String userId, boolean excludeContained) {
		Query q = new Query();
		if (excludeContained) {
			q.fields().exclude("element");
		}
		Criteria cId = Criteria.where(ID_FIELD).is(id);
		Criteria cNoDraft = Criteria.where("draft").is(false);
		if (userId==null) {
			q.addCriteria(new Criteria().andOperator(cId, cNoDraft));
		} else {
			Criteria cOwner = Criteria.where("ownerId").is(userId);
			Criteria sharedDraft = Criteria.where("readIds").is(userId);
			q.addCriteria(new Criteria().andOperator(cId, new Criteria().orOperator(cOwner, cNoDraft, sharedDraft)));
		}
		return this.findOne(q);	
	}
	
	@Override
	public List<RightsContainer<T>> findByCriteriaAndUserId(Criteria c, String userId) {
		Query q = new Query();
		
		Criteria cNoDraft = Criteria.where("draft").is(false);
		if (userId==null) {
			q.addCriteria(new Criteria().andOperator(c, cNoDraft));
		} else {
			Criteria cOwner = Criteria.where("ownerId").is(userId);
			Criteria sharedDraft = Criteria.where("readIds").is(userId);
			q.addCriteria(new Criteria().andOperator(c, new Criteria().orOperator(cOwner, cNoDraft, sharedDraft)));
		}
		return this.find(q);	
	}
	
	@Override
	public void updateContained(T e, String userId, String sessionId) throws GenericScheregException {
		if (e.getId()==null) {
			throw new GenericScheregException("Contained update only allowed for existing element (no ID provided)");
		}
		RightsContainer<T> saveE = this.findById(e.getId());
		if (saveE==null) {
			throw new GenericScheregException("Contained update only allowed for existing elements (unknown ID provided)");
		}
		saveE.setElement(e);
		this.save(saveE, userId, sessionId);
	}
}
