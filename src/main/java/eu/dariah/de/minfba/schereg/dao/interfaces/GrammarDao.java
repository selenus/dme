package eu.dariah.de.minfba.schereg.dao.interfaces;

import java.util.List;

import eu.dariah.de.minfba.core.metamodel.function.interfaces.DescriptionGrammar;
import eu.dariah.de.minfba.schereg.dao.base.TrackedEntityDao;

public interface GrammarDao extends TrackedEntityDao<DescriptionGrammar> {
	public List<DescriptionGrammar> findByEntityId(String entityId);
	
	public int deleteAll(String entityId);
}