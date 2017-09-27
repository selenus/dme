package de.unibamberg.minf.dme.dao.interfaces;

import de.unibamberg.minf.dme.dao.base.RightsAssignedObjectDao;
import de.unibamberg.minf.dme.model.datamodel.base.Datamodel;
import de.unibamberg.minf.dme.model.datamodel.base.DatamodelNature;
import de.unibamberg.minf.dme.model.datamodel.natures.xml.XmlNamespace;

public interface SchemaDao extends RightsAssignedObjectDao<Datamodel> {
	public XmlNamespace findNamespaceByPrefix(String string);	
}