package de.unibamberg.minf.dme.dao.base;

import de.unibamberg.minf.dme.model.base.ModelElement;


public interface ModelElementDao<T extends ModelElement> extends TrackedEntityDao<T> {
	public void updateEntityId(String currentEntityId, String newEntityId);
}
