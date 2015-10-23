package eu.dariah.de.minfba.schereg.dao;

import java.util.List;

import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import eu.dariah.de.minfba.core.metamodel.interfaces.Schema;
import eu.dariah.de.minfba.core.metamodel.tracking.Change;
import eu.dariah.de.minfba.core.metamodel.xml.XmlNamespace;
import eu.dariah.de.minfba.core.metamodel.xml.XmlSchema;
import eu.dariah.de.minfba.core.metamodel.xml.XmlTerminal;
import eu.dariah.de.minfba.schereg.dao.base.RightsAssignedObjectDaoImpl;
import eu.dariah.de.minfba.schereg.dao.interfaces.SchemaDao;
import eu.dariah.de.minfba.schereg.model.RightsContainer;

@Repository
public class SchemaDaoImpl extends RightsAssignedObjectDaoImpl<Schema> implements SchemaDao {
	public SchemaDaoImpl() {
		super(new RightsContainer<Schema>().getClass()/*, "schema"*/);
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
	public <S extends RightsContainer<Schema>> S save(S element, String userId, String sessionId) {
		/* XmlTerminal and XmlNamespace objects are saved with the schema */
		if (element.getElement() instanceof XmlSchema) {
			List<Change> changes;
			XmlSchema s = (XmlSchema)element.getElement();
			if (s.getTerminals()!=null) {
				for (XmlTerminal xmlT : s.getTerminals()) {
					changes = xmlT.flush();
					if (changes!=null) {
						this.createAndSaveChangeSet(changes, xmlT.getId(), s.getId(), userId, sessionId);
					}
				}
			}
			if (s.getNamespaces()!=null) {
				for (XmlNamespace xmlNs : s.getNamespaces()) {
					changes = xmlNs.flush();
					if (changes!=null) {
						this.createAndSaveChangeSet(changes, xmlNs.getId(), s.getId(), userId, sessionId);
					}
				}
			}
		}
		
		return super.save(element, userId, sessionId);
	}
}
