package de.dariah.schereg.base.dao.impl;

import java.util.Collection;

import org.springframework.stereotype.Repository;

import de.dariah.base.dao.base.PersistedSchemaElementDaoImpl;
import de.dariah.schereg.base.dao.ContainmentDao;
import de.dariah.schereg.base.model.Containment;

@Repository
public class ContainmentDaoImpl extends PersistedSchemaElementDaoImpl<Containment> implements ContainmentDao {

	public ContainmentDaoImpl() {
		super(Containment.class);
	}
	
	@Override
	public int[] saveOrUpdate(Collection<Containment> entities) {
		for (Containment entity : entities) {
			setForeignIds(entity);
		}
		return super.saveOrUpdate(entities);
	}

	@Override
	public Containment saveOrUpdate(Containment entity) {
		return super.saveOrUpdate(setForeignIds(entity));
	}
	
	/**
	 * Ids need to be set manually, since the objects are transient.
	 * This is done since the individual ids could point to various types
	 */
	private Containment setForeignIds(Containment cont) {
		if (cont.getParent()!=null) {
			cont.setParentId(cont.getParent().getId());
		}
		if (cont.getChild()!=null) {
			cont.setChildId(cont.getChild().getId());
		}
		return cont;
	}
	
	// TODO: FindById should include the transient objects based on a union select
}
