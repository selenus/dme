package eu.dariah.de.minfba.schereg.service.interfaces;

import java.util.List;

import eu.dariah.de.minfba.schereg.model.PersistedVocabulary;

public interface PersistedVocabularyService {
	public PersistedVocabulary findById(String id);
	public PersistedVocabulary save(PersistedVocabulary saveVocabulary);
	public List<PersistedVocabulary> findAll();	
}