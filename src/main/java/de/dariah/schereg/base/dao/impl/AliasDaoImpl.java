package de.dariah.schereg.base.dao.impl;

import org.springframework.stereotype.Repository;

import de.dariah.base.dao.base.PersistedSchemaElementDaoImpl;
import de.dariah.schereg.base.dao.AliasDao;
import de.dariah.schereg.base.model.Alias;

@Repository
public class AliasDaoImpl extends PersistedSchemaElementDaoImpl<Alias> implements AliasDao {

	public AliasDaoImpl() {
		super(Alias.class);
	}
}
