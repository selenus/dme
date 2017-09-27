package de.unibamberg.minf.dme.service;

import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.unibamberg.minf.dme.dao.interfaces.PersistedVocabularyDao;
import de.unibamberg.minf.dme.model.PersistedVocabulary;
import de.unibamberg.minf.dme.service.interfaces.PersistedVocabularyService;
import de.unibamberg.minf.gtf.vocabulary.VocabularyEngine;

@Service
public class PersistedVocabularyServiceImpl implements InitializingBean, PersistedVocabularyService {
	@Autowired private PersistedVocabularyDao vocabularyDao;
	@Autowired private VocabularyEngine vocabularyEngine;

	@Override
	public void afterPropertiesSet() throws Exception {
		vocabularyEngine.setVocabularies(vocabularyDao.findAll());
	}
	
	@Override
	public PersistedVocabulary findById(String id) {
		return vocabularyDao.findById(id);
	}

	@Override
	public PersistedVocabulary save(PersistedVocabulary saveVocabulary) {
		saveVocabulary = vocabularyDao.save(saveVocabulary);
		vocabularyEngine.setVocabularies(vocabularyDao.findAll());
		
		return saveVocabulary;
	}

	@Override
	public List<PersistedVocabulary> findAll() {
		return vocabularyDao.findAll();
	}
}