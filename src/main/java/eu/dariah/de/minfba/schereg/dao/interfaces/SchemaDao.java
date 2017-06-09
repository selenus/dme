package eu.dariah.de.minfba.schereg.dao.interfaces;

import eu.dariah.de.minfba.core.metamodel.interfaces.Schema;
import eu.dariah.de.minfba.core.metamodel.interfaces.SchemaNature;
import eu.dariah.de.minfba.core.metamodel.xml.XmlNamespace;
import eu.dariah.de.minfba.schereg.dao.base.RightsAssignedObjectDao;

public interface SchemaDao extends RightsAssignedObjectDao<Schema> {
	public XmlNamespace findNamespaceByPrefix(String string);	
}