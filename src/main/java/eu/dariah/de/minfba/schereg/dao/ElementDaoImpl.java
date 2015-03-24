package eu.dariah.de.minfba.schereg.dao;

import org.springframework.stereotype.Repository;

import eu.dariah.de.minfba.core.metamodel.interfaces.Element;
import eu.dariah.de.minfba.schereg.dao.base.BaseDaoImpl;

@Repository
public class ElementDaoImpl extends BaseDaoImpl<Element> implements ElementDao {
	public ElementDaoImpl() {
		super(Element.class);
	}
}
