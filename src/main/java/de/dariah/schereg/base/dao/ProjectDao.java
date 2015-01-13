package de.dariah.schereg.base.dao;

import de.dariah.base.dao.base.PersistedEntityDao;
import de.dariah.schereg.base.model.Project;

public interface ProjectDao extends PersistedEntityDao<Project> {
	public Project getMainProject();
	public Project getBackupProject();
}
