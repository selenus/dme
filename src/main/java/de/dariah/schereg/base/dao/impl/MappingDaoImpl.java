package de.dariah.schereg.base.dao.impl;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import de.dariah.base.dao.base.PersistedEntityDaoImpl;
import de.dariah.schereg.base.dao.MappingDao;
import de.dariah.schereg.base.model.Mapping;

@Repository
@Transactional
public class MappingDaoImpl extends PersistedEntityDaoImpl<Mapping> implements MappingDao {

	public MappingDaoImpl() {
		super(Mapping.class);
	}
}