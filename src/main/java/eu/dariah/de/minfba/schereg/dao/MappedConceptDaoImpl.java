package eu.dariah.de.minfba.schereg.dao;

import java.util.LinkedHashMap;
import java.util.List;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import eu.dariah.de.minfba.core.metamodel.function.DescriptionGrammarImpl;
import eu.dariah.de.minfba.core.metamodel.interfaces.MappedConcept;
import eu.dariah.de.minfba.schereg.dao.base.TrackedEntityDaoImpl;
import eu.dariah.de.minfba.schereg.dao.interfaces.MappedConceptDao;

@Repository
public class MappedConceptDaoImpl extends TrackedEntityDaoImpl<MappedConcept> implements MappedConceptDao {
	public MappedConceptDaoImpl() {
		super(MappedConcept.class);
	}
	
	@Override
	public List<MappedConcept> findByEntityId(String entityId) {		
		Query q = Query.query(Criteria.where("entityId").is(entityId));
		return this.find(q);
	}
	
	@Override
	public <S extends MappedConcept> S save(S element, String userId, String sessionId) {
		
		/*element.setElementGrammarIdsMap(null);
		if (element.getSourceElementMap()!=null && element.getSourceElementMap().size()>0) {
			element.setElementGrammarIdsMap(new LinkedHashMap<String, String>());
			DescriptionGrammarImpl g;
			for (String key : element.getSourceElementMap().keySet()) {
				g = element.getSourceElementMap().get(key);
				if (g==null) {
					continue;
				} else if (isNewId(g.getId())) {
					logger.warn("Reference to unsaved grammar not persisted for mapped concept.");
				} else {
					element.getElementGrammarIdsMap().put(key, g.getId());
				}
			}
			
		} */
		
		return super.save(element, userId, sessionId);
	}
}
