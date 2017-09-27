package de.unibamberg.minf.dme.dao;

import org.springframework.stereotype.Repository;

import de.unibamberg.minf.dme.dao.base.BaseDaoImpl;
import de.unibamberg.minf.dme.dao.interfaces.PersistedVocabularyDao;
import de.unibamberg.minf.dme.model.PersistedVocabulary;

@Repository
public class PersistedVocabularyDaoImpl extends BaseDaoImpl<PersistedVocabulary> implements PersistedVocabularyDao {
	public PersistedVocabularyDaoImpl() {
		super(PersistedVocabulary.class);
	}
}
