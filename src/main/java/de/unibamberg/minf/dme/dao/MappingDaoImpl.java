package de.unibamberg.minf.dme.dao;

import org.springframework.stereotype.Repository;

import de.unibamberg.minf.dme.dao.base.RightsAssignedObjectDaoImpl;
import de.unibamberg.minf.dme.dao.interfaces.MappingDao;
import de.unibamberg.minf.dme.model.RightsContainer;
import de.unibamberg.minf.dme.model.datamodel.base.DatamodelNature;
import de.unibamberg.minf.dme.model.mapping.base.Mapping;

@Repository
public class MappingDaoImpl extends RightsAssignedObjectDaoImpl<Mapping> implements MappingDao {
	public MappingDaoImpl() {
		super(new RightsContainer<Mapping>().getClass(), "mapping");
	}
}
