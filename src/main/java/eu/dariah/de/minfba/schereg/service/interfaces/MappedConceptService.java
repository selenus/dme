package eu.dariah.de.minfba.schereg.service.interfaces;

import java.util.List;

import de.dariah.samlsp.model.pojo.AuthPojo;
import eu.dariah.de.minfba.core.metamodel.interfaces.MappedConcept;

public interface MappedConceptService {

	public List<MappedConcept> findAll(String id);
	public MappedConcept findById(String conceptId);
	
	public void saveMappedConcept(MappedConcept mappedConcept, AuthPojo auth);
}