package eu.dariah.de.minfba.schereg.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.dariah.de.minfba.core.metamodel.interfaces.Schema;
import eu.dariah.de.minfba.schereg.dao.SchemaDao;

@Service
public class SchemaServiceImpl implements SchemaService {
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
		schemaDao.delete(id);
	}

	@Override
	public <T extends Schema> T convertSchema(T newSchema, Schema original) {
		newSchema.setId(original.getId());
		newSchema.setLabel(original.getLabel());
		newSchema.setDescription(original.getDescription());
		newSchema.setRootNonterminalId(original.getRootNonterminalId());
		return newSchema;
	}
}
