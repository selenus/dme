package eu.dariah.de.minfba.schereg.dao;

import java.util.List;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.mongodb.WriteResult;

import eu.dariah.de.minfba.core.metamodel.function.DescriptionGrammarImpl;
import eu.dariah.de.minfba.core.metamodel.function.interfaces.DescriptionGrammar;
import eu.dariah.de.minfba.core.metamodel.tracking.Change;
import eu.dariah.de.minfba.core.metamodel.xml.XmlSchemaNature;
import eu.dariah.de.minfba.core.metamodel.xml.XmlTerminal;
import eu.dariah.de.minfba.schereg.dao.base.TrackedEntityDaoImpl;
import eu.dariah.de.minfba.schereg.dao.interfaces.GrammarDao;

@Repository
public class GrammarDaoImpl extends TrackedEntityDaoImpl<DescriptionGrammar> implements GrammarDao {
	public GrammarDaoImpl() {
		super(DescriptionGrammar.class);
	}
	
	@Override
	public List<DescriptionGrammar> findByEntityId(String entityId) {		
		Query q = Query.query(Criteria.where("entityId").is(entityId));
		
		// Exclude the verbous grammars when loading whole schema
		q.fields().exclude("grammarContainer");
		
		return this.find(q);
	}
	
	@Override
	public <S extends DescriptionGrammar> S save(S element, String userId, String sessionId) {
		List<Change> changes;
		DescriptionGrammarImpl g = (DescriptionGrammarImpl)element;
		if (g.getGrammarContainer()!=null) {
			changes = g.getGrammarContainer().flush();
			if (changes!=null) {
				this.createAndSaveChangeSet(changes, g.getId(), g.getEntityId(), userId, sessionId);
			}
		}
		return super.save(element, userId, sessionId);
	}
	
	@Override
	public int deleteAll(String entityId) {
		WriteResult result = mongoTemplate.remove(Query.query(Criteria.where(ENTITY_ID_FIELD).is(entityId)), this.getCollectionName());
		return result.getN();
	}
}
