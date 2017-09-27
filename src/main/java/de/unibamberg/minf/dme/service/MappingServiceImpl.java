package de.unibamberg.minf.dme.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import de.unibamberg.minf.dme.dao.base.DaoImpl;
import de.unibamberg.minf.dme.dao.interfaces.MappingDao;
import de.unibamberg.minf.dme.dao.interfaces.SchemaDao;
import de.unibamberg.minf.dme.model.MappingWithSchemasImpl;
import de.unibamberg.minf.dme.model.RightsContainer;
import de.unibamberg.minf.dme.model.datamodel.base.Datamodel;
import de.unibamberg.minf.dme.model.datamodel.base.DatamodelNature;
import de.unibamberg.minf.dme.model.mapping.base.Mapping;
import de.unibamberg.minf.dme.pojo.AuthWrappedPojo;
import de.unibamberg.minf.dme.serialization.Reference;
import de.unibamberg.minf.dme.service.base.BaseEntityServiceImpl;
import de.unibamberg.minf.dme.service.base.BaseReferenceServiceImpl;
import de.unibamberg.minf.dme.service.interfaces.MappingService;
import eu.dariah.de.dariahsp.model.web.AuthPojo;

@Service
public class MappingServiceImpl extends BaseEntityServiceImpl implements MappingService {

	@Override
	public List<RightsContainer<Mapping>> findAllByAuth(AuthPojo auth) {
		return mappingDao.findAllByUserId(auth.getUserId());
	}

	@Override
	public RightsContainer<Mapping> findByIdAndAuth(String id, AuthPojo auth) {
		return mappingDao.findByIdAndUserId(id, auth.getUserId());
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
		container.setReadOnly(mapping.isReadOnly());
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
			if (this.getUserCanWriteEntity(s, auth.getUserId())) {
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
		List<RightsContainer<Datamodel>> requiredSchemas = schemaDao.find(qSchema);
		HashMap<String, String> schemaIdLabelMap = new HashMap<String, String>(requiredSchemas.size());
		for (RightsContainer<Datamodel> s : requiredSchemas) {
			schemaIdLabelMap.put(s.getId(), s.getElement().getName());
		}
		
		for (RightsContainer<Mapping> m : mappings) {
			MappingWithSchemasImpl mExt = new MappingWithSchemasImpl();
			mExt.setId(m.getElement().getId());
			mExt.setDescription(m.getElement().getDescription());
			mExt.setSourceId(m.getElement().getSourceId());
			mExt.setSourceLabel(schemaIdLabelMap.get(mExt.getSourceId()));
			mExt.setTargetId(m.getElement().getTargetId());
			mExt.setTargetLabel(schemaIdLabelMap.get(mExt.getTargetId()));
			
			m.setElement(mExt);
		}
		
		return mappings;
	}

	@Override
	public List<RightsContainer<Mapping>> getMappings(String entityId) {
		Query q = new Query();
		q.addCriteria(new Criteria().orOperator(Criteria.where("element.sourceId").is(entityId),
				Criteria.where("element.targetId").is(entityId)));
		
		return mappingDao.find(q);
	}

	@Override
	public List<RightsContainer<Mapping>> findAllByAuthAndSourceId(AuthPojo auth, String sourceId) {
		return mappingDao.findByCriteriaAndUserId(Criteria.where("element.sourceId").is(sourceId), auth.getUserId());
	}

	@Override
	public List<RightsContainer<Mapping>> findAllByAuthAndTargetId(AuthPojo auth, String targetId) {
		return mappingDao.findByCriteriaAndUserId(Criteria.where("element.targetId").is(targetId), auth.getUserId());
	}

	@Override
	public RightsContainer<Mapping> findByAuthAndSourceAndTargetId(AuthPojo auth, String sourceId, String targetId) {
		Criteria c = new Criteria().andOperator(
				Criteria.where("element.sourceId").is(sourceId),
				Criteria.where("element.targetId").is(targetId));

		// TODO: This is an issue (#522) and needs to be solved otherwise
		List<RightsContainer<Mapping>> mappings = mappingDao.findByCriteriaAndUserId(c, auth.getUserId());
		if (mappings==null || mappings.size()==0) {
			return null;
		}
		return mappings.get(0);
	}
}