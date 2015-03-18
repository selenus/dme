package eu.dariah.de.minfba.schereg.dao;

import org.springframework.stereotype.Repository;

import de.dariah.base.dao.base.BaseDaoImpl;
import eu.dariah.de.minfba.core.metamodel.function.DescriptionGrammarImpl;
import eu.dariah.de.minfba.core.metamodel.function.interfaces.DescriptionGrammar;

@Repository
public class DescriptionGrammarDaoImpl extends BaseDaoImpl<DescriptionGrammar> implements DescriptionGrammarDao {
	public DescriptionGrammarDaoImpl() {
		super(DescriptionGrammarImpl.class);
	}
}