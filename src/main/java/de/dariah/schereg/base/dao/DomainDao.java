package de.dariah.schereg.base.dao;

import java.util.List;

import de.dariah.base.dao.base.PersistedSchemaElementDao;
import de.dariah.schereg.base.model.Domain;

public interface DomainDao extends PersistedSchemaElementDao<Domain> {
	public List<Domain> findGlobalDomains();
}
