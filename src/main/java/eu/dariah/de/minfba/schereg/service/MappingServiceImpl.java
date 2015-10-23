package eu.dariah.de.minfba.schereg.service;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.dariah.samlsp.model.pojo.AuthPojo;
import eu.dariah.de.minfba.core.metamodel.interfaces.Mapping;
import eu.dariah.de.minfba.core.metamodel.interfaces.Schema;
import eu.dariah.de.minfba.schereg.dao.interfaces.MappingDao;
import eu.dariah.de.minfba.schereg.model.RightsContainer;
import eu.dariah.de.minfba.schereg.pojo.AuthWrappedPojo;
import eu.dariah.de.minfba.schereg.service.interfaces.MappingService;

@Service
public class MappingServiceImpl implements MappingService {
	@Autowired private MappingDao mappingDao;

	@Override
	public List<RightsContainer<Mapping>> findAllByAuth(AuthPojo auth) {
		return mappingDao.findAllByUserId(auth.getUserId());
	}

	@Override
	public RightsContainer<Mapping> findByIdAndAuth(String id, AuthPojo auth) {
		return mappingDao.findByIdAndUserId(id, auth.getUserId());
	}

	@Override
	public boolean getHasWriteAccess(String id, String userId) {
		/* User is logged in (has an ID) and creates a new mapping (no ID) */
		if ( (id==null || id.isEmpty()) && (userId!=null && !userId.isEmpty()) ) {
			return true;
		}
		RightsContainer<Mapping> m = mappingDao.findByIdAndUserId(id, userId, true);
		return this.getHasWriteAccess(m, userId);
	}
	
	@Override
	public boolean getHasWriteAccess(RightsContainer<Mapping> m, String userId) {
		if (m!=null && ( m.getOwnerId().equals(userId) || ( m.getWriteIds()!=null && m.getWriteIds().contains(userId)) ) ) {
			return true;
		}
		return false;
	}

	@Override
	public Mapping findMappingById(String id) {
		return mappingDao.findEnclosedById(id);
	}

	@Override
	public void saveMapping(AuthWrappedPojo<Mapping> mapping, AuthPojo auth) {
		RightsContainer<Mapping> container = null;
		if (mapping.getId()!=null) {
			container = mappingDao.findById(mapping.getId());
		}
		if (container==null) {
			container = createContainer(auth.getUserId());
		}
		container.setElement(mapping.getPojo());
		container.setDraft(mapping.isDraft());
		mappingDao.save(container, auth.getUserId(), auth.getSessionId());
	}	
	
	private RightsContainer<Mapping> createContainer(String userId) {
		RightsContainer<Mapping> container = new RightsContainer<Mapping>();
		container.setOwnerId(userId);
		container.setId(new ObjectId().toString());
		container.setDraft(true);
		return container;
	}
}