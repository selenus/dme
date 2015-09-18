package eu.dariah.de.minfba.schereg.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import de.dariah.samlsp.model.pojo.AuthPojo;
import eu.dariah.de.minfba.core.metamodel.interfaces.Schema;
import eu.dariah.de.minfba.core.metamodel.xml.XmlSchema;
import eu.dariah.de.minfba.core.metamodel.xml.XmlTerminal;
import eu.dariah.de.minfba.schereg.dao.base.BaseDaoImpl;
import eu.dariah.de.minfba.schereg.dao.interfaces.SchemaDao;
import eu.dariah.de.minfba.schereg.model.RightsContainer;
import eu.dariah.de.minfba.schereg.service.base.BaseService;
import eu.dariah.de.minfba.schereg.service.interfaces.ElementService;
import eu.dariah.de.minfba.schereg.service.interfaces.SchemaService;

@Service
public class SchemaServiceImpl extends BaseService implements SchemaService {
	@Autowired private ElementService elementService;
	@Autowired private SchemaDao schemaDao;

	@Override
	public List<Schema> findAllSchemas() {
		return schemaDao.findAllSchemas();
	}

	@Override
	public void saveSchema(RightsContainer<Schema> schema) {
		schemaDao.save(schema);
	}
	
	@Override
	public void saveSchema(Schema schema, AuthPojo auth) {
		RightsContainer<Schema> container = null;
		if (schema.getId()!=null) {
			container = schemaDao.findById(schema.getId());
		}
		if (container==null) {
			container = new RightsContainer<Schema>();
			container.setOwnerId(auth.getUserId());
			container.setId(new ObjectId().toString());
			container.setDraft(true);
		}
		container.setElement(schema);
		schemaDao.save(container);
	}

	@Override
	public Schema findSchemaById(String id) {
		return schemaDao.findSchemaById(id);
	}

	@Override
	public void deleteSchemaById(String id) {
		RightsContainer<Schema> s = schemaDao.findById(id);
		if (s != null) {
			if (BaseDaoImpl.isValidObjectId(s.getElement().getRootNonterminalId())) {
				elementService.removeElement(s.getId(), s.getElement().getRootNonterminalId());
			}
			schemaDao.delete(s);
		}
	}
	
	@Override
	public void upsertSchema(Query query, Update update) {
		schemaDao.upsert(query, update);
	}

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
}
