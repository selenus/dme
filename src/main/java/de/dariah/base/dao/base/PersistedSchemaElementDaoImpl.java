package de.dariah.base.dao.base;

import java.util.Collection;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;

import de.dariah.base.model.base.SchemaElement;

public class PersistedSchemaElementDaoImpl<T extends SchemaElement> extends PersistedEntityDaoImpl<T> implements PersistedSchemaElementDao<T> {

	public PersistedSchemaElementDaoImpl(Class<T> clazz) {
		super(clazz);
	}

	@Override
	public Collection<T> findBySchemaId(int schemaId) {
	
		Criterion cr = Restrictions.eq("schema.id", schemaId);
		return super.findByCriterion(cr);
	}

}
