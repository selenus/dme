package eu.dariah.de.minfba.schereg.service.interfaces;

import java.util.List;

import de.dariah.samlsp.model.pojo.AuthPojo;
import eu.dariah.de.minfba.core.metamodel.interfaces.Mapping;
import eu.dariah.de.minfba.schereg.model.RightsContainer;
import eu.dariah.de.minfba.schereg.pojo.AuthWrappedPojo;

public interface MappingService {
	public List<RightsContainer<Mapping>> findAllByAuth(AuthPojo auth);
	public RightsContainer<Mapping> findByIdAndAuth(String id, AuthPojo auth);
	public boolean getHasWriteAccess(String id, String userId);
	public Mapping findMappingById(String id);
	public void saveMapping(AuthWrappedPojo<Mapping> authWrappedPojo, AuthPojo auth);
	boolean getHasWriteAccess(RightsContainer<Mapping> m, String userId);
}