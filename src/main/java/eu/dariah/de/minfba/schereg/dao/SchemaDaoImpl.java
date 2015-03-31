package eu.dariah.de.minfba.schereg.dao;

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

@Repository
public class SchemaDaoImpl extends BaseDaoImpl<Schema> implements SchemaDao {
	public SchemaDaoImpl() {
		super(Schema.class);
	}

	@Override
	public List<Schema> findAll() {
		Query q = new Query();
		q.fields().exclude("namespaces");
		
		return this.find(q);
	}
	
	@Override
	public XmlNamespace findNamespaceByPrefix(String string) {
        // the query object
        Criteria findParentSchema = Criteria.where("namespaces.prefix").is(string);
        // the field object
        Criteria filterContainedNamespace = Criteria.where("namespaces").elemMatch(Criteria.where("prefix").is(string));
                
        BasicQuery query = new BasicQuery(findParentSchema.getCriteriaObject(), filterContainedNamespace.getCriteriaObject());	    
	    XmlSchema result = (XmlSchema)this.findOne(query);
		
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
