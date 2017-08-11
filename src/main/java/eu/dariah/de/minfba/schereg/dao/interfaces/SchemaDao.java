package eu.dariah.de.minfba.schereg.dao.interfaces;

import de.unibamberg.minf.dme.model.datamodel.base.Datamodel;
import de.unibamberg.minf.dme.model.datamodel.base.DatamodelNature;
import de.unibamberg.minf.dme.model.datamodel.natures.xml.XmlNamespace;
import eu.dariah.de.minfba.schereg.dao.base.RightsAssignedObjectDao;

public interface SchemaDao extends RightsAssignedObjectDao<Datamodel> {
	public XmlNamespace findNamespaceByPrefix(String string);	
}