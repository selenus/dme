package de.dariah.schereg.base.dao.impl;

import java.util.Collection;

import org.springframework.stereotype.Repository;

import de.dariah.base.dao.base.PersistedSchemaElementDaoImpl;
import de.dariah.schereg.base.dao.SubtypeDao;
import de.dariah.schereg.base.model.Subtype;

@Repository
public class SubtypeDaoImpl extends PersistedSchemaElementDaoImpl<Subtype> implements SubtypeDao {

	public SubtypeDaoImpl() {
		super(Subtype.class);
	}
	
	@Override
	public int[] saveOrUpdate(Collection<Subtype> entities) {
		for (Subtype entity : entities) {
			setForeignIds(entity);
		}
		return super.saveOrUpdate(entities);
	}

	@Override
	public Subtype saveOrUpdate(Subtype entity) {
		return super.saveOrUpdate(setForeignIds(entity));
	}
	
	/**
	 * Ids need to be set manually, since the objects are transient.
	 * This is done since the individual ids could point to various types
	 */
	private Subtype setForeignIds(Subtype subtype) {
		if (subtype.getParent()!=null) {
			subtype.setParentId(subtype.getParent().getId());
		}
		if (subtype.getChild()!=null) {
			subtype.setChildId(subtype.getChild().getId());
		}		
		return subtype;
	}
	
	// TODO: FindById should include the transient objects based on a union select
}
