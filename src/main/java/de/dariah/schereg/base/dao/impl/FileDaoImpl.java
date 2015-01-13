package de.dariah.schereg.base.dao.impl;

import org.springframework.stereotype.Repository;

import de.dariah.base.dao.base.PersistedEntityDaoImpl;
import de.dariah.schereg.base.dao.FileDao;
import de.dariah.schereg.base.model.File;

@Repository
public class FileDaoImpl extends PersistedEntityDaoImpl<File> implements FileDao {
	public FileDaoImpl() {
		super(File.class);
	}
}
