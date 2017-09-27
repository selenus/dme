package de.unibamberg.minf.dme.dao;

import java.util.List;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import com.mongodb.WriteResult;

import de.unibamberg.minf.dme.dao.base.TrackedEntityDaoImpl;
import de.unibamberg.minf.dme.dao.interfaces.GrammarDao;
import de.unibamberg.minf.dme.model.base.Grammar;
import de.unibamberg.minf.dme.model.datamodel.natures.XmlDatamodelNature;
import de.unibamberg.minf.dme.model.datamodel.natures.xml.XmlTerminal;
import de.unibamberg.minf.dme.model.grammar.GrammarImpl;
import de.unibamberg.minf.dme.model.tracking.Change;

@Repository
public class GrammarDaoImpl extends TrackedEntityDaoImpl<Grammar> implements GrammarDao {
	public GrammarDaoImpl() {
		super(Grammar.class);
	}
	
	@Override
	public List<Grammar> findByEntityId(String entityId) {		
		Query q = Query.query(Criteria.where("entityId").is(entityId));
		
		// Exclude the verbous grammars when loading whole schema
		q.fields().exclude("grammarContainer");
		
		return this.find(q);
	}
	
	@Override
	public <S extends Grammar> S save(S element, String userId, String sessionId) {
		List<Change> changes;
		GrammarImpl g = (GrammarImpl)element;
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
