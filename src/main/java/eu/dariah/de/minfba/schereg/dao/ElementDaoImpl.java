package eu.dariah.de.minfba.schereg.dao;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.joda.time.DateTime;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import eu.dariah.de.minfba.core.metamodel.interfaces.Element;
import eu.dariah.de.minfba.core.metamodel.tracking.Change;
import eu.dariah.de.minfba.core.metamodel.tracking.ChangeImpl;
import eu.dariah.de.minfba.core.metamodel.tracking.ChangeSet;
import eu.dariah.de.minfba.core.metamodel.tracking.ChangeType;
import eu.dariah.de.minfba.schereg.dao.base.TrackedEntityDaoImpl;
import eu.dariah.de.minfba.schereg.dao.interfaces.ElementDao;

@Repository
public class ElementDaoImpl extends TrackedEntityDaoImpl<Element> implements ElementDao {
	public ElementDaoImpl() {
		super(Element.class);
	}

	@Override
	public List<Element> findByEntityId(String entityId) {		
		Query q = Query.query(Criteria.where("entityId").is(entityId));
		return this.find(q);
	}

	@Override
	public void saveNew(List<Element> saveElements, String userId, String sessionId) {
		List<ChangeSet> changeSets = new ArrayList<ChangeSet>();
		ChangeSet c;
		
		for (Element e : saveElements) {
			List<Change> changes = e.flush();
			if (changes==null) {
				changes = new ArrayList<Change>();
				changes.add(new ChangeImpl<String>(ChangeType.NEW_OBJECT, this.getCollectionName(), null, e.getId(), DateTime.now()));
				
				if (changes!=null && changes.size()>0) {
					c = new ChangeSet();
					c.setUserId(userId);
					c.setSessionId(sessionId);
					c.setEntityId(e.getEntityId());
					c.setElementId(e.getId());
					c.setChanges(changes);
					
					changeSets.add(c);
				}
			}
		}
		
		mongoTemplate.insert(saveElements, this.getCollectionName());
		mongoTemplate.insert(changeSets, changeSetDao.getCollectionName());
	}

}