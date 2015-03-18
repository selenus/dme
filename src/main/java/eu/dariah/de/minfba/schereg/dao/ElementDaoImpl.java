package eu.dariah.de.minfba.schereg.dao;

import org.springframework.stereotype.Repository;

import de.dariah.base.dao.base.BaseDaoImpl;
import eu.dariah.de.minfba.core.metamodel.BaseElement;
import eu.dariah.de.minfba.core.metamodel.interfaces.Element;

@Repository
public class ElementDaoImpl extends BaseDaoImpl<Element> implements ElementDao {
	public ElementDaoImpl() {
		super(BaseElement.class);
	}
}
