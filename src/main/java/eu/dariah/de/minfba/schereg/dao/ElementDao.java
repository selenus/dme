package eu.dariah.de.minfba.schereg.dao;

import eu.dariah.de.minfba.core.metamodel.interfaces.Element;
import eu.dariah.de.minfba.core.metamodel.interfaces.Schema;
import eu.dariah.de.minfba.schereg.dao.base.BaseDao;

public interface ElementDao extends BaseDao<Element> {
	public <S extends Element> S save(Schema s, S entity);
}
