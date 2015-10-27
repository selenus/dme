package eu.dariah.de.minfba.schereg.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import de.dariah.samlsp.model.pojo.AuthPojo;
import eu.dariah.de.minfba.core.metamodel.interfaces.Schema;
import eu.dariah.de.minfba.core.metamodel.tracking.ChangeSet;
import eu.dariah.de.minfba.core.metamodel.xml.XmlSchema;
import eu.dariah.de.minfba.core.metamodel.xml.XmlTerminal;
import eu.dariah.de.minfba.schereg.dao.base.BaseDaoImpl;
import eu.dariah.de.minfba.schereg.dao.interfaces.SchemaDao;
import eu.dariah.de.minfba.schereg.model.RightsContainer;
import eu.dariah.de.minfba.schereg.pojo.AuthWrappedPojo;
import eu.dariah.de.minfba.schereg.service.base.BaseServiceImpl;
import eu.dariah.de.minfba.schereg.service.interfaces.ElementService;
import eu.dariah.de.minfba.schereg.service.interfaces.SchemaService;

@Service
public class SchemaServiceImpl extends BaseServiceImpl implements SchemaService {
	@Autowired private ElementService elementService;
	@Autowired private SchemaDao schemaDao;

	@Override
	public List<Schema> findAllSchemas() {
		return schemaDao.findAllEnclosed();
	}

	@Override
	public void saveSchema(AuthWrappedPojo<? extends Schema> schema, AuthPojo auth) {
		RightsContainer<Schema> container = null;
		if (schema.getId()!=null) {
			container = schemaDao.findById(schema.getId());
		}
		if (container==null) {
			container = createContainer(auth.getUserId());
		}
		container.setElement(schema.getPojo());
		container.setDraft(schema.isDraft());
		schemaDao.save(container, auth.getUserId(), auth.getSessionId());
	}
	
	@Override
	public void saveSchema(Schema schema, AuthPojo auth) {
		RightsContainer<Schema> container = null;
		if (schema.getId()!=null) {
			container = schemaDao.findById(schema.getId());
		}
		if (container==null) {
			container = createContainer(auth.getUserId());
		}
		container.setElement(schema);
		schemaDao.save(container, auth.getUserId(), auth.getSessionId());
	}
	
	private RightsContainer<Schema> createContainer(String userId) {
		RightsContainer<Schema> container = new RightsContainer<Schema>();
		container.setOwnerId(userId);
		container.setId(new ObjectId().toString());
		container.setDraft(true);
		return container;
	}

	@Override
	public Schema findSchemaById(String id) {
		return schemaDao.findEnclosedById(id);
	}

	@Override
	public void deleteSchemaById(String id, AuthPojo auth) {
		RightsContainer<Schema> s = schemaDao.findById(id);
		if (s != null) {
			if (this.getHasWriteAccess(s, auth.getUserId())) {
				if (BaseDaoImpl.isValidObjectId(s.getElement().getRootNonterminalId())) {
					elementService.removeElement(s.getId(), s.getElement().getRootNonterminalId(), auth);
				}
				schemaDao.delete(s, auth.getUserId(), auth.getSessionId());
			}
		}
	}
	
	/*@Override
	public void upsertSchema(Query query, Update update) {
		schemaDao.upsert(query, update);
	}*/

	@Override
	public <T extends Schema> T convertSchema(T newSchema, Schema original) {
		newSchema.setId(original.getId());
		newSchema.setLabel(original.getLabel());
		newSchema.setDescription(original.getDescription());
		newSchema.setRootNonterminalId(original.getRootNonterminalId());
		return newSchema;
	}
	
	@Override
	public Map<String, String> getAvailableTerminals(String schemaId) {
		Map<String,String> availableTerminals = new HashMap<String,String>();
		Schema s = this.findSchemaById(schemaId);
		if (s instanceof XmlSchema) {	
			if (((XmlSchema)s).getTerminals()!=null) {
				for (XmlTerminal t : ((XmlSchema)s).getTerminals()) {
					availableTerminals.put(t.getId(), t.getName() + " (" + t.getNamespace() + ")");
				}
			}
		}
		return availableTerminals;
	}

	@Override
	public List<RightsContainer<Schema>> findAllByAuth(AuthPojo auth) {
		return schemaDao.findAllByUserId(auth.getUserId());
	}

	@Override
	public RightsContainer<Schema> findByIdAndAuth(String schemaId, AuthPojo auth) {
		return schemaDao.findByIdAndUserId(schemaId, auth.getUserId());
	}

	@Override
	public boolean getHasWriteAccess(String id, String userId) {
		/* User is logged in (has an ID) and creates a new schema (no ID) */
		if ( (id==null || id.isEmpty()) && (userId!=null && !userId.isEmpty()) ) {
			return true;
		}
		RightsContainer<Schema> s = schemaDao.findByIdAndUserId(id, userId, true);
		return this.getHasWriteAccess(s, userId);
	}
	
	@Override
	public boolean getHasWriteAccess(RightsContainer<Schema> s, String userId) {
		if (s!=null && ( s.getOwnerId().equals(userId) || ( s.getWriteIds()!=null && s.getWriteIds().contains(userId)) ) ) {
			return true;
		}
		return false;
	}
		
	@Override
	public boolean getHasShareAccess(String id, String userId) {
		RightsContainer<Schema> s = schemaDao.findByIdAndUserId(id, userId, true);
		return this.getHasShareAccess(s, userId);
	}
	
	@Override
	public boolean getHasShareAccess(RightsContainer<Schema> s, String userId) {
		if (s!=null && ( s.getOwnerId().equals(userId) || ( s.getShareIds()!=null && s.getShareIds().contains(userId)) ) ) {
			return true;
		}
		return false;
	}

	@Override
	public List<ChangeSet> getChangeSetForAllSchemas() {
		Query q = new Query();
		q.fields().include("_id");
		List<RightsContainer<Schema>> schemas = schemaDao.find(q);
		List<String> ids = new ArrayList<String>();
		if (schemas!=null) {
			for (RightsContainer<Schema> s : schemas) {
				ids.add(s.getId());
			}
		}
	
		return super.getChangeSetForElements(ids);
	}
}
