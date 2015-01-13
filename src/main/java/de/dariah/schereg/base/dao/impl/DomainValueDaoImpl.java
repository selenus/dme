package de.dariah.schereg.base.dao.impl;

import org.springframework.stereotype.Repository;

import de.dariah.base.dao.base.PersistedSchemaElementDaoImpl;
import de.dariah.schereg.base.dao.DomainValueDao;
import de.dariah.schereg.base.model.DomainValue;

@Repository
public class DomainValueDaoImpl extends PersistedSchemaElementDaoImpl<DomainValue> implements DomainValueDao {

	public DomainValueDaoImpl() {
		super(DomainValue.class);
	}
}
