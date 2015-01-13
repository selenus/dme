package de.dariah.schereg.base.dao.impl;

import org.springframework.stereotype.Repository;

import de.dariah.base.dao.base.BaseEntityDaoImpl;
import de.dariah.schereg.base.dao.FileTypeDao;
import de.dariah.schereg.base.model.FileType;

@Repository
public class FileTypeDaoImpl extends BaseEntityDaoImpl<FileType> implements FileTypeDao {
	public FileTypeDaoImpl() {
		super(FileType.class);
	}
}
