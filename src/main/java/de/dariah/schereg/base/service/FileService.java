package de.dariah.schereg.base.service;

import java.util.List;

import de.dariah.schereg.base.model.File;
import de.dariah.schereg.base.model.FileType;

public interface FileService {
	public List<File> listFiles();
	public File getFile(int id);
	public void removeFile(int id);
	public void saveFile(File file);
	
	public List<FileType> listFileTypes();
	public FileType getFileType(int id);
	public FileType getFileType(String name) throws Exception;
}
