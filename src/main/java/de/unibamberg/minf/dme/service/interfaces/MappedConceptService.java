package de.unibamberg.minf.dme.service.interfaces;

import java.util.List;

import de.unibamberg.minf.dme.exception.GenericScheregException;
import de.unibamberg.minf.dme.model.base.Function;
import de.unibamberg.minf.dme.model.mapping.base.MappedConcept;
import eu.dariah.de.dariahsp.model.web.AuthPojo;

public interface MappedConceptService {
	public List<MappedConcept> findAllByMappingId(String mappingId);
	public List<MappedConcept> findAllByMappingId(String mappingId, boolean eagerLoadHierarchy);	
	public MappedConcept findById(String mappingId, String mappedConceptId, boolean eagerLoadHierarchy);
	public MappedConcept findById(String conceptId);
	
	public void removeMappedConcept(String mappingId, String mappedConceptId, AuthPojo auth) throws GenericScheregException;
	public void saveMappedConcept(MappedConcept mappedConcept, String mappingId, AuthPojo auth);
	public void removeSourceElementById(AuthPojo auth, String mappingId, MappedConcept mc, String sourceId);
	public void removeSourceElementById(AuthPojo auth, String mappingId, String mappedConceptId, String sourceId);
}