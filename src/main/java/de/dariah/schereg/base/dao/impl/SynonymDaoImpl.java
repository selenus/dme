package de.dariah.schereg.base.dao.impl;

import org.springframework.stereotype.Repository;

import de.dariah.base.dao.base.PersistedSchemaElementDaoImpl;
import de.dariah.schereg.base.dao.SynonymDao;
import de.dariah.schereg.base.model.Synonym;

@Repository
public class SynonymDaoImpl extends PersistedSchemaElementDaoImpl<Synonym> implements SynonymDao {

	public SynonymDaoImpl() {
		super(Synonym.class);
	}
}
