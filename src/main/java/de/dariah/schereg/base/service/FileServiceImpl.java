package de.dariah.schereg.base.service;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import de.dariah.schereg.base.dao.FileDao;
import de.dariah.schereg.base.dao.FileTypeDao;
import de.dariah.schereg.base.model.File;
import de.dariah.schereg.base.model.FileType;

@Service
@Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
public class FileServiceImpl implements FileService {

	@Autowired
	private FileDao fileDao;
	
	@Autowired
	private FileTypeDao fileTypeDao;

	@Override
	public List<File> listFiles() {
		return fileDao.findAll();
	}

	@Override
	public File getFile(int id) {
		return fileDao.findById(id);
	}

	@Override
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public void removeFile(int id) {
		fileDao.delete(id);
	}

	@Override
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public void saveFile(File file) {
		fileDao.saveOrUpdate(file);
	}

	@Override
	public List<FileType> listFileTypes() {
		return fileTypeDao.findAll();
	}

	@Override
	public FileType getFileType(int id) {
		return fileTypeDao.findById(id);
	}

	@Override
	public FileType getFileType(String name) throws Exception {
		Criterion cr = Restrictions.eq("name", name);
		ArrayList<Criterion> crList = new ArrayList<Criterion>();
		crList.add(cr);
		return fileTypeDao.findByCriteriaDistinct(crList);
	}
}
