package eu.dariah.de.minfba.schereg.service;

import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.unibamberg.minf.gtf.vocabulary.VocabularyEngine;
import eu.dariah.de.minfba.schereg.dao.interfaces.PersistedVocabularyDao;
import eu.dariah.de.minfba.schereg.model.PersistedVocabulary;
import eu.dariah.de.minfba.schereg.service.interfaces.PersistedVocabularyService;

@Service
public class PersistedVocabularyServiceImpl implements InitializingBean, PersistedVocabularyService {
	@Autowired private PersistedVocabularyDao vocabularyDao;
	@Autowired private VocabularyEngine vocabularyEngine;

	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public PersistedVocabulary findById(String id) {
		return vocabularyDao.findById(id);
	}

	@Override
	public PersistedVocabulary save(PersistedVocabulary saveVocabulary) {
		return vocabularyDao.save(saveVocabulary);
	}

	@Override
	public List<PersistedVocabulary> findAll() {
		return vocabularyDao.findAll();
	}
}