package eu.dariah.de.minfba.schereg.dao;

import java.util.List;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import eu.dariah.de.minfba.core.metamodel.interfaces.Element;
import eu.dariah.de.minfba.schereg.dao.base.TrackedEntityDaoImpl;
import eu.dariah.de.minfba.schereg.dao.interfaces.ElementDao;

@Repository
public class ElementDaoImpl extends TrackedEntityDaoImpl<Element> implements ElementDao {
	public ElementDaoImpl() {
		super(Element.class);
	}

	@Override
	public List<Element> findBySchemaId(String schemaId) {		
		return this.find(Query.query(Criteria.where("schemaId").is(schemaId)));
	}
}