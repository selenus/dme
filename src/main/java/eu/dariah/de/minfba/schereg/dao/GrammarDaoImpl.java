package eu.dariah.de.minfba.schereg.dao;

import java.util.List;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import eu.dariah.de.minfba.core.metamodel.function.interfaces.DescriptionGrammar;
import eu.dariah.de.minfba.schereg.dao.base.BaseDaoImpl;
import eu.dariah.de.minfba.schereg.dao.interfaces.GrammarDao;

@Repository
public class GrammarDaoImpl extends BaseDaoImpl<DescriptionGrammar> implements GrammarDao {
	public GrammarDaoImpl() {
		super(DescriptionGrammar.class);
	}
	
	@Override
	public List<DescriptionGrammar> findBySchemaId(String schemaId) {		
		return this.find(Query.query(Criteria.where("schemaId").is(schemaId)));
	}
}
