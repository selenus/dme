package de.dariah.schereg.base.dao.impl;

import org.springframework.stereotype.Repository;

import de.dariah.base.dao.base.PersistedSchemaElementDaoImpl;
import de.dariah.schereg.base.dao.EntityDao;
import de.dariah.schereg.base.model.Entity;

@Repository
public class EntityDaoImpl extends PersistedSchemaElementDaoImpl<Entity> implements EntityDao {

	public EntityDaoImpl() {
		super(Entity.class);
	}
}
