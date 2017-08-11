package eu.dariah.de.minfba.schereg.dao;

import org.springframework.stereotype.Repository;

import de.unibamberg.minf.dme.model.datamodel.base.DatamodelNature;
import de.unibamberg.minf.dme.model.mapping.base.Mapping;
import eu.dariah.de.minfba.schereg.dao.base.RightsAssignedObjectDaoImpl;
import eu.dariah.de.minfba.schereg.dao.interfaces.MappingDao;
import eu.dariah.de.minfba.schereg.model.RightsContainer;

@Repository
public class MappingDaoImpl extends RightsAssignedObjectDaoImpl<Mapping> implements MappingDao {
	public MappingDaoImpl() {
		super(new RightsContainer<Mapping>().getClass(), "mapping");
	}
}
