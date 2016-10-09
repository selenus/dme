package eu.dariah.de.minfba.schereg.service.interfaces;

import java.util.List;

import de.dariah.samlsp.model.pojo.AuthPojo;
import eu.dariah.de.minfba.core.metamodel.interfaces.MappedConcept;
import eu.dariah.de.minfba.core.metamodel.function.interfaces.TransformationFunction;
import eu.dariah.de.minfba.schereg.exception.GenericScheregException;

public interface MappedConceptService {
	public List<MappedConcept> findAllByMappingId(String mappingId);
	public List<MappedConcept> findAllByMappingId(String mappingId, boolean eagerLoadHierarchy);	
	public MappedConcept findById(String mappingId, String mappedConceptId, boolean eagerLoadHierarchy);
	public MappedConcept findById(String conceptId);
	
	public void removeMappedConcept(String mappingId, String mappedConceptId, AuthPojo auth) throws GenericScheregException;
	public void saveMappedConcept(MappedConcept mappedConcept, String mappingId, AuthPojo auth);
	public void removeSourceElementById(AuthPojo auth, String mappingId, MappedConcept mc, String sourceId);
	public void removeSourceElementById(AuthPojo auth, String mappingId, String mappedConceptId, String sourceId);
	public TransformationFunction getConceptFunction(String mappingId, String conceptId);	
}