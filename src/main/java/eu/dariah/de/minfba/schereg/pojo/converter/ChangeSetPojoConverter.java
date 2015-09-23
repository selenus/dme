package eu.dariah.de.minfba.schereg.pojo.converter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import eu.dariah.de.minfba.core.metamodel.tracking.Change;
import eu.dariah.de.minfba.core.metamodel.tracking.ChangeSet;
import eu.dariah.de.minfba.core.metamodel.tracking.ChangeType;
import eu.dariah.de.minfba.schereg.dao.interfaces.PersistedUserDetailsDao;
import eu.dariah.de.minfba.schereg.pojo.ChangeSetPojo;

@Component
public class ChangeSetPojoConverter {
	@Autowired private PersistedUserDetailsDao userDetailsDao;
	
	public Collection<ChangeSetPojo> convert(List<ChangeSet> changeSets) {
		Map<String, ChangeSetPojo> bySessionMap = new HashMap<String, ChangeSetPojo>();
		ChangeSetPojo cPojo;
		if (changeSets!=null) {
			for (ChangeSet c : changeSets) {
				if (bySessionMap.containsKey(c.getSessionId())) {
					cPojo = bySessionMap.get(c.getSessionId());
					if (c.getTimestamp()!=null && (cPojo.getTimestamp()==null || c.getTimestamp().isAfter(cPojo.getTimestamp()))) {
						cPojo.setTimestamp(c.getTimestamp());
					}
				} else {
					cPojo = new ChangeSetPojo();
					cPojo.setUser(userDetailsDao.findById(c.getUserId()).getUsername());
					cPojo.setTimestamp(c.getTimestamp());
				}
				if (cPojo.getChanges()==null) {
					cPojo.setChanges(new HashMap<String, List<Change>>());
				}
				List<Change> elementChanges = new ArrayList<Change>();
				if (cPojo.getChanges().containsKey(c.getElementId())) {
					elementChanges.addAll(cPojo.getChanges().get(c.getElementId()));
				}
				
				for (Change newC : c.getChanges()) {
					elementChanges.add(newC);
					if (newC.getChangeType()==ChangeType.EDIT_VALUE) {
						cPojo.setEdits(cPojo.getEdits()+1);
					} else if (newC.getChangeType()==ChangeType.NEW_OBJECT) {
						cPojo.setNews(cPojo.getNews()+1);
					} else if (newC.getChangeType()==ChangeType.DELETE_OBJECT) {
						cPojo.setDeletes(cPojo.getDeletes()+1);
					}
				}
				cPojo.getChanges().put(c.getElementId(), c.getChanges());
				
				bySessionMap.put(c.getSessionId(), cPojo);
			}
		}
		return bySessionMap.values();
	}
}
