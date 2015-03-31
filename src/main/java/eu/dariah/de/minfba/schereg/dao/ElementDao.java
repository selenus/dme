package eu.dariah.de.minfba.schereg.dao;

import java.util.List;

import eu.dariah.de.minfba.core.metamodel.interfaces.Element;
import eu.dariah.de.minfba.schereg.dao.base.BaseDao;

public interface ElementDao extends BaseDao<Element> {
	public List<Element> findBySchemaId(String schemaId);
}