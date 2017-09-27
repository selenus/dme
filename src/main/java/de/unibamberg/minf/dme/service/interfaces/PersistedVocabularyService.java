package de.unibamberg.minf.dme.service.interfaces;

import java.util.List;

import de.unibamberg.minf.dme.model.PersistedVocabulary;

public interface PersistedVocabularyService {
	public PersistedVocabulary findById(String id);
	public PersistedVocabulary save(PersistedVocabulary saveVocabulary);
	public List<PersistedVocabulary> findAll();	
}