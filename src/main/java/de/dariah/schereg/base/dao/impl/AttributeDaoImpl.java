package de.dariah.schereg.base.dao.impl;

import org.springframework.stereotype.Repository;

import de.dariah.base.dao.base.PersistedSchemaElementDaoImpl;
import de.dariah.schereg.base.dao.AttributeDao;
import de.dariah.schereg.base.model.Attribute;

@Repository
public class AttributeDaoImpl extends PersistedSchemaElementDaoImpl<Attribute> implements AttributeDao {

	public AttributeDaoImpl() {
		super(Attribute.class);
	}
}
