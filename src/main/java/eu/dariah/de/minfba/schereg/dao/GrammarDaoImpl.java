package eu.dariah.de.minfba.schereg.dao;

import java.util.List;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import eu.dariah.de.minfba.core.metamodel.function.DescriptionGrammarImpl;
import eu.dariah.de.minfba.core.metamodel.function.interfaces.DescriptionGrammar;
import eu.dariah.de.minfba.core.metamodel.tracking.Change;
import eu.dariah.de.minfba.core.metamodel.xml.XmlSchema;
import eu.dariah.de.minfba.core.metamodel.xml.XmlTerminal;
import eu.dariah.de.minfba.schereg.dao.base.TrackedEntityDaoImpl;
import eu.dariah.de.minfba.schereg.dao.interfaces.GrammarDao;

@Repository
public class GrammarDaoImpl extends TrackedEntityDaoImpl<DescriptionGrammar> implements GrammarDao {
	public GrammarDaoImpl() {
		super(DescriptionGrammar.class);
	}
	
	@Override
	public List<DescriptionGrammar> findBySchemaId(String schemaId) {		
		Query q = Query.query(Criteria.where("schemaId").is(schemaId));
		
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
				this.createAndSaveChangeSet(changes, g.getId(), g.getSchemaId(), userId, sessionId);
			}
		}
		return super.save(element, userId, sessionId);
	}
}
