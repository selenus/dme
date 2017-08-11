package eu.dariah.de.minfba.schereg.dao;

import java.util.List;

import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Repository;

import de.unibamberg.minf.dme.model.datamodel.base.Datamodel;
import de.unibamberg.minf.dme.model.datamodel.base.DatamodelNature;
import de.unibamberg.minf.dme.model.datamodel.natures.XmlDatamodelNature;
import de.unibamberg.minf.dme.model.datamodel.natures.xml.XmlNamespace;
import de.unibamberg.minf.dme.model.datamodel.natures.xml.XmlTerminal;
import de.unibamberg.minf.dme.model.tracking.Change;
import eu.dariah.de.minfba.schereg.dao.base.RightsAssignedObjectDaoImpl;
import eu.dariah.de.minfba.schereg.dao.interfaces.SchemaDao;
import eu.dariah.de.minfba.schereg.model.RightsContainer;

@Repository
public class SchemaDaoImpl extends RightsAssignedObjectDaoImpl<Datamodel> implements SchemaDao {
	public SchemaDaoImpl() {
		super(new RightsContainer<DatamodelNature>().getClass(), "schema");
	}

	@Override
	public XmlNamespace findNamespaceByPrefix(String string) {
        // the query object
        Criteria findParentSchema = Criteria.where("element.namespaces.prefix").is(string);
        // the field object
        Criteria filterContainedNamespace = Criteria.where("element.namespaces").elemMatch(Criteria.where("prefix").is(string));
                
        BasicQuery query = new BasicQuery(findParentSchema.getCriteriaObject(), filterContainedNamespace.getCriteriaObject());	    
	    XmlDatamodelNature result = (XmlDatamodelNature)this.findOne(query).getElement();
		
		if (result.getNamespaces()!=null && result.getNamespaces().size()>0) {
			return result.getNamespaces().get(0);
		}
		return null;
	}
	
	@Override
	public <S extends RightsContainer<Datamodel>> S save(S element, String userId, String sessionId) {
		/* XmlTerminal and XmlNamespace objects are saved with the schema */
		if (element.getElement() instanceof XmlDatamodelNature) {
			List<Change> changes;
			XmlDatamodelNature s = (XmlDatamodelNature)element.getElement();
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
