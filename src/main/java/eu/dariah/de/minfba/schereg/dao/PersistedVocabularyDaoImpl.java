package eu.dariah.de.minfba.schereg.dao;

import org.springframework.stereotype.Repository;

import eu.dariah.de.minfba.schereg.dao.base.BaseDaoImpl;
import eu.dariah.de.minfba.schereg.dao.interfaces.PersistedVocabularyDao;
import eu.dariah.de.minfba.schereg.model.PersistedVocabulary;

@Repository
public class PersistedVocabularyDaoImpl extends BaseDaoImpl<PersistedVocabulary> implements PersistedVocabularyDao {
	public PersistedVocabularyDaoImpl() {
		super(PersistedVocabulary.class);
	}
}
