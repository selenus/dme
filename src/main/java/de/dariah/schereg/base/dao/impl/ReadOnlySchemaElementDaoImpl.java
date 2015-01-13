package de.dariah.schereg.base.dao.impl;

import org.springframework.stereotype.Repository;

import de.dariah.base.dao.base.BaseEntityDaoImpl;
import de.dariah.schereg.base.dao.ReadOnlySchemaElementDao;
import de.dariah.schereg.base.model.ReadOnlySchemaElement;

@Repository
public class ReadOnlySchemaElementDaoImpl extends BaseEntityDaoImpl<ReadOnlySchemaElement> implements ReadOnlySchemaElementDao {

	public ReadOnlySchemaElementDaoImpl() {
		super(ReadOnlySchemaElement.class);
	}
}