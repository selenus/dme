package de.unibamberg.minf.dme.service.interfaces;

import java.util.List;

import de.unibamberg.minf.dme.model.RightsContainer;
import de.unibamberg.minf.dme.model.mapping.base.Mapping;
import de.unibamberg.minf.dme.model.tracking.ChangeSet;
import de.unibamberg.minf.dme.pojo.AuthWrappedPojo;
import de.unibamberg.minf.dme.service.base.BaseEntityService;
import eu.dariah.de.dariahsp.model.web.AuthPojo;

public interface MappingService extends BaseEntityService {
	public List<RightsContainer<Mapping>> findAllByAuth(AuthPojo auth);
	public RightsContainer<Mapping> findByIdAndAuth(String id, AuthPojo auth);
	
	public Mapping findMappingById(String id);
	public void saveMapping(AuthWrappedPojo<Mapping> authWrappedPojo, AuthPojo auth);
	
	public void deleteMappingById(String id, AuthPojo auth);
	public List<RightsContainer<Mapping>> findAllByAuth(AuthPojo auth, boolean view);
	public List<RightsContainer<Mapping>> getMappings(String entityId);
	public List<RightsContainer<Mapping>> findAllByAuthAndSourceId(AuthPojo auth, String sourceId);
	public List<RightsContainer<Mapping>> findAllByAuthAndTargetId(AuthPojo auth, String targetId);
	public RightsContainer<Mapping> findByAuthAndSourceAndTargetId(AuthPojo auth, String sourceId, String targetId);
	public ChangeSet getLatestChangeSetForEntity(String id);
	public void changeDatamodelId(String currentId, String newId);
	public boolean changeId(String currentId, String id);	
}
