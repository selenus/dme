package eu.dariah.de.minfba.schereg.dao;

import org.springframework.stereotype.Repository;

import eu.dariah.de.minfba.core.metamodel.interfaces.Mapping;
import eu.dariah.de.minfba.core.metamodel.interfaces.SchemaNature;
import eu.dariah.de.minfba.schereg.dao.base.RightsAssignedObjectDaoImpl;
import eu.dariah.de.minfba.schereg.dao.interfaces.MappingDao;
import eu.dariah.de.minfba.schereg.model.RightsContainer;

@Repository
public class MappingDaoImpl extends RightsAssignedObjectDaoImpl<Mapping> implements MappingDao {
	public MappingDaoImpl() {
		super(new RightsContainer<Mapping>().getClass(), "mapping");
	}
}
