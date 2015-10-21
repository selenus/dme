package eu.dariah.de.minfba.schereg.dao;

import org.springframework.stereotype.Repository;

import eu.dariah.de.minfba.core.metamodel.interfaces.Mapping;
import eu.dariah.de.minfba.schereg.dao.base.TrackedEntityDaoImpl;
import eu.dariah.de.minfba.schereg.dao.interfaces.MappingDao;

@Repository
public class MappingDaoImpl extends TrackedEntityDaoImpl<Mapping> implements MappingDao {
	public MappingDaoImpl() {
		super(Mapping.class);
	}
}
