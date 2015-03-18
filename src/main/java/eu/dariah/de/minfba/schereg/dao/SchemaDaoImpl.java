package eu.dariah.de.minfba.schereg.dao;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Repository;

import eu.dariah.de.minfba.core.metamodel.interfaces.Schema;
import eu.dariah.de.minfba.core.metamodel.xml.XmlSchema;
import eu.dariah.de.minfba.schereg.dao.base.BaseDaoImpl;

@Repository
public class SchemaDaoImpl extends BaseDaoImpl<Schema> implements SchemaDao {
	@Autowired MongoTemplate mongoTemplate;
	
	public void loadAllSchemas() {
	    List<Schema> results = mongoTemplate.findAll(Schema.class, "collection");
	    
	    
	    XmlSchema xs = new XmlSchema();
	    xs.setLabel("label");
	    
	    mongoTemplate.insert(xs, "collection");
	    
	    logger.info("Total amount of schemas: {}", results.size());
	    logger.info("Results: {}", results);
	}
}
