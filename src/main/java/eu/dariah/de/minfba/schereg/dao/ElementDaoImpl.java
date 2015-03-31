package eu.dariah.de.minfba.schereg.dao;

import org.springframework.stereotype.Repository;

import eu.dariah.de.minfba.core.metamodel.interfaces.Element;
import eu.dariah.de.minfba.core.metamodel.interfaces.Schema;
import eu.dariah.de.minfba.schereg.dao.base.BaseDaoImpl;

@Repository
public class ElementDaoImpl extends BaseDaoImpl<Element> implements ElementDao {
	public ElementDaoImpl() {
		super(Element.class);
	}
	
	@Override
	public <S extends Element> S save(Schema s, S entity) {
		return super.save(entity);
	}
}
