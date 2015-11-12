package eu.dariah.de.minfba.schereg.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import de.dariah.samlsp.model.pojo.AuthPojo;
import eu.dariah.de.minfba.core.metamodel.interfaces.Mapping;
import eu.dariah.de.minfba.core.metamodel.interfaces.Schema;
import eu.dariah.de.minfba.schereg.dao.interfaces.MappingDao;
import eu.dariah.de.minfba.schereg.dao.interfaces.SchemaDao;
import eu.dariah.de.minfba.schereg.model.MappingWithSchemasImpl;
import eu.dariah.de.minfba.schereg.model.RightsContainer;
import eu.dariah.de.minfba.schereg.pojo.AuthWrappedPojo;
import eu.dariah.de.minfba.schereg.serialization.Reference;
import eu.dariah.de.minfba.schereg.service.base.BaseReferenceServiceImpl;
import eu.dariah.de.minfba.schereg.service.interfaces.MappingService;

@Service
public class MappingServiceImpl extends BaseReferenceServiceImpl implements MappingService {
	@Autowired private MappingDao mappingDao;
	@Autowired private SchemaDao schemaDao;

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
		if (isNewId(id)) {
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
		boolean isNew = mapping.getId()==null || mapping.getId().equals("") || mapping.getId().equals("undefined");
		if (!isNew) {
			container = mappingDao.findById(mapping.getId());
		}
		if (container==null) {
			isNew = true;
			container = createContainer(auth.getUserId());
		}
		container.setElement(mapping.getPojo());
		container.setDraft(mapping.isDraft());
		mappingDao.save(container, auth.getUserId(), auth.getSessionId());
		if (isNew) {
			this.saveRootReference(new Reference(mapping.getId()));
		}
	}

	@Override
	public void deleteMappingById(String id, AuthPojo auth) {
		RightsContainer<Mapping> s = mappingDao.findById(id);
		if (s != null) {
			if (this.getHasWriteAccess(s, auth.getUserId())) {
				mappingDao.delete(s, auth.getUserId(), auth.getSessionId());
			}
		}
	}
	
	private RightsContainer<Mapping> createContainer(String userId) {
		RightsContainer<Mapping> container = new RightsContainer<Mapping>();
		container.setOwnerId(userId);
		container.setId(new ObjectId().toString());
		container.setDraft(true);
		return container;
	}

	@Override
	public List<RightsContainer<Mapping>> findAllByAuth(AuthPojo auth, boolean view) {
		List<RightsContainer<Mapping>> mappings = this.findAllByAuth(auth); 
		if (view==false) {
			return mappings;
		}
		
		/* Collect required schema ids */
		Collection<String> schemaIds = new ArrayList<String>();
		for (RightsContainer<Mapping> m : mappings) {
			if (!schemaIds.contains(m.getElement().getSourceId())) {
				schemaIds.add(m.getElement().getSourceId());
			}
			if (!schemaIds.contains(m.getElement().getTargetId())) {
				schemaIds.add(m.getElement().getTargetId());
			}
		}
		
		/* Prepare schemas for faster access while minimizing DB access */
		Query qSchema = new Query();
		qSchema.fields().include("id");
		qSchema.fields().include("element");
		qSchema.addCriteria(Criteria.where("id").in(schemaIds));
		List<RightsContainer<Schema>> requiredSchemas = schemaDao.find(qSchema);
		HashMap<String, String> schemaIdLabelMap = new HashMap<String, String>(requiredSchemas.size());
		for (RightsContainer<Schema> s : requiredSchemas) {
			schemaIdLabelMap.put(s.getId(), s.getElement().getLabel());
		}
		
		for (RightsContainer<Mapping> m : mappings) {
			MappingWithSchemasImpl mExt = new MappingWithSchemasImpl();
			mExt.setId(m.getElement().getId());
			mExt.setDescription(m.getElement().getDescription());
			mExt.setEntityId(m.getElement().getEntityId());
			mExt.setSourceId(m.getElement().getSourceId());
			mExt.setSourceLabel(schemaIdLabelMap.get(mExt.getSourceId()));
			mExt.setTargetId(m.getElement().getTargetId());
			mExt.setTargetLabel(schemaIdLabelMap.get(mExt.getTargetId()));
			
			m.setElement(mExt);
		}
		
		return mappings;
	}
}