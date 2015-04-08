package eu.dariah.de.minfba.schereg.dao;

import java.util.List;

import eu.dariah.de.minfba.core.metamodel.function.interfaces.DescriptionGrammar;
import eu.dariah.de.minfba.schereg.dao.base.BaseDao;

public interface GrammarDao extends BaseDao<DescriptionGrammar> {
	public List<DescriptionGrammar> findBySchemaId(String schemaId);
}