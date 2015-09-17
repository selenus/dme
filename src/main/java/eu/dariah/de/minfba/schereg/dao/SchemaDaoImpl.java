package eu.dariah.de.minfba.schereg.dao;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import eu.dariah.de.minfba.core.metamodel.interfaces.Schema;
import eu.dariah.de.minfba.core.metamodel.xml.XmlNamespace;
import eu.dariah.de.minfba.core.metamodel.xml.XmlSchema;
import eu.dariah.de.minfba.schereg.dao.base.BaseDaoImpl;
import eu.dariah.de.minfba.schereg.dao.interfaces.SchemaDao;
import eu.dariah.de.minfba.schereg.exception.GenericScheregException;
import eu.dariah.de.minfba.schereg.model.RightsContainer;

@Repository
public class SchemaDaoImpl extends BaseDaoImpl<RightsContainer<Schema>> implements SchemaDao {
	public SchemaDaoImpl() {
		super(new RightsContainer<Schema>().getClass());
	}

	@Override
	public List<Schema> findAllSchemas() {
		List<RightsContainer<Schema>> wrapped = this.findAll();
		List<Schema> schemas = new ArrayList<Schema>(wrapped.size());
		for (RightsContainer<Schema> w : wrapped) {
			schemas.add(w.getElement());
		}
		return schemas;
	}
	
	@Override
	public Schema findSchemaById(String id) {
		RightsContainer<Schema> schema = this.findById(id);
		if (schema!=null) {
			return schema.getElement();
		}
		return null;
	}
	
	
	@Override
	public void updateContained(Schema s) throws GenericScheregException {
		if (s.getId()==null) {
			throw new GenericScheregException("Contained update only allowed for existing schemata (no ID provided)");
		}
		RightsContainer<Schema> saveS = this.findById(s.getId());
		if (saveS==null) {
			throw new GenericScheregException("Contained update only allowed for existing schemata (unknown ID provided)");
		}
		saveS.setElement(s);
		this.save(saveS);
	}
	
	@Override
	public List<RightsContainer<Schema>> findAll() {
		Query q = new Query();
		q.fields().exclude("element.namespaces");
		q.addCriteria(Criteria.where("draft").is(false));
		
		return this.find(q);
	}
	
	@Override
	public XmlNamespace findNamespaceByPrefix(String string) {
        // the query object
        Criteria findParentSchema = Criteria.where("element.namespaces.prefix").is(string);
        // the field object
        Criteria filterContainedNamespace = Criteria.where("element.namespaces").elemMatch(Criteria.where("prefix").is(string));
                
        BasicQuery query = new BasicQuery(findParentSchema.getCriteriaObject(), filterContainedNamespace.getCriteriaObject());	    
	    XmlSchema result = (XmlSchema)this.findOne(query).getElement();
		
		if (result.getNamespaces()!=null && result.getNamespaces().size()>0) {
			return result.getNamespaces().get(0);
		}
		return null;
	}

	@Override
	public void updateNamespaceByPrefix(Schema s, String string, String string2) {
		// the query object
        DBObject obj = new BasicDBObject();
        obj.put("namespaces.prefix", string);
        obj.put("_id", s.getId());
        
        // the field object
        Criteria filterContainedNamespace = Criteria.where("namespaces").elemMatch(Criteria.where("prefix").is(string));
                
        BasicQuery query = new BasicQuery(obj, filterContainedNamespace.getCriteriaObject());	    
	    
        
        
        
	    Update update = new Update(); 
	    update.set("namespaces.$.url", string2);
	    	    
	    this.findAndModify(query, update);
	}
}
