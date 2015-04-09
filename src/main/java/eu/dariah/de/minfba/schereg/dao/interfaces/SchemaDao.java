package eu.dariah.de.minfba.schereg.dao.interfaces;

import eu.dariah.de.minfba.core.metamodel.interfaces.Schema;
import eu.dariah.de.minfba.core.metamodel.xml.XmlNamespace;
import eu.dariah.de.minfba.schereg.dao.base.BaseDao;

public interface SchemaDao extends BaseDao<Schema> {
	public XmlNamespace findNamespaceByPrefix(String string);
	public void updateNamespaceByPrefix(Schema s, String string, String string2);
}