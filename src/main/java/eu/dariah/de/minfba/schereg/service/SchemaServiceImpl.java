package eu.dariah.de.minfba.schereg.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import eu.dariah.de.minfba.core.metamodel.interfaces.Schema;
import eu.dariah.de.minfba.core.metamodel.xml.XmlSchema;
import eu.dariah.de.minfba.core.metamodel.xml.XmlTerminal;
import eu.dariah.de.minfba.schereg.dao.base.BaseDaoImpl;
import eu.dariah.de.minfba.schereg.dao.interfaces.SchemaDao;
import eu.dariah.de.minfba.schereg.service.base.BaseService;
import eu.dariah.de.minfba.schereg.service.interfaces.ElementService;
import eu.dariah.de.minfba.schereg.service.interfaces.SchemaService;

@Service
public class SchemaServiceImpl extends BaseService implements SchemaService {
	@Autowired private ElementService elementService;
	@Autowired private SchemaDao schemaDao;

	@Override
	public List<Schema> findAllSchemas() {
		return schemaDao.findAll();
	}

	@Override
	public void saveSchema(Schema schema) {
		schemaDao.save(schema);
	}

	@Override
	public Schema findSchemaById(String id) {
		return schemaDao.findById(id);
	}

	@Override
	public void deleteSchemaById(String id) {
		Schema s = findSchemaById(id);
		if (s != null) {
			if (BaseDaoImpl.isValidObjectId(s.getRootNonterminalId())) {
				elementService.removeElement(s.getId(), s.getRootNonterminalId());
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
}
