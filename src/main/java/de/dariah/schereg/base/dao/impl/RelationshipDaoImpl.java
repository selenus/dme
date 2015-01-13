package de.dariah.schereg.base.dao.impl;

import java.util.Collection;

import org.springframework.stereotype.Repository;

import de.dariah.base.dao.base.PersistedSchemaElementDaoImpl;
import de.dariah.schereg.base.dao.RelationshipDao;
import de.dariah.schereg.base.model.Relationship;

@Repository
public class RelationshipDaoImpl extends PersistedSchemaElementDaoImpl<Relationship> implements RelationshipDao {

	public RelationshipDaoImpl() {
		super(Relationship.class);
	}
	
	@Override
	public int[] saveOrUpdate(Collection<Relationship> entities) {
		for (Relationship entity : entities) {
			setForeignIds(entity);
		}
		return super.saveOrUpdate(entities);
	}

	@Override
	public Relationship saveOrUpdate(Relationship entity) {
		return super.saveOrUpdate(setForeignIds(entity));
	}
	
	/**
	 * Ids need to be set manually, since the objects are transient.
	 * This is done since the individual ids could point to various types
	 */
	private Relationship setForeignIds(Relationship rel) {
		if (rel.getLeft()!=null) {
			rel.setLeftId(rel.getLeft().getId());
		}
		if (rel.getRight()!=null) {
			rel.setRightId(rel.getRight().getId());
		}
		return rel;
	}
	
	// TODO: FindById should include the transient objects based on a union select
}
