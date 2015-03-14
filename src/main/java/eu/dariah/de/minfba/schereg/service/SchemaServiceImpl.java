package eu.dariah.de.minfba.schereg.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import eu.dariah.de.minfba.core.metamodel.interfaces.Schema;
import eu.dariah.de.minfba.core.metamodel.xml.XmlSchema;
import eu.dariah.de.minfba.schereg.dao.BaseSchemaDao;

@Service
@Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
public class SchemaServiceImpl implements SchemaService {

	@Autowired private BaseSchemaDao baseSchemaDao;
	
	@Override
	public List<Schema> findAllSchemata() {
		return baseSchemaDao.findAll();
	}

	@Override
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public void saveSchema(Schema s) {
		baseSchemaDao.saveOrUpdate(s);
	}

	@Override
	@Transactional(propagation=Propagation.REQUIRES_NEW, readOnly=false)
	public void deleteSchema(Schema s) {
		baseSchemaDao.delete(s);		
	}
	
}
