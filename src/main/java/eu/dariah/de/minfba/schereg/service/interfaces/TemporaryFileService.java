package eu.dariah.de.minfba.schereg.service.interfaces;

import java.io.IOException;
import java.nio.file.Path;

public interface TemporaryFileService {
	public String getTmpUploadDirPath();
	public Path writeString(String path, String lexerGrammar) throws IOException;
	public void createDirectory(String path) throws IOException;
}
