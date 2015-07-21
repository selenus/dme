package eu.dariah.de.minfba.schereg.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import eu.dariah.de.minfba.schereg.service.interfaces.TemporaryFileService;

@Component
public class TemporaryFileServiceImpl implements TemporaryFileService, InitializingBean {
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Value(value="${paths.tmpUploadDir:/tmp}")
	private String tmpUploadDirPath;
	
	public String getTmpUploadDirPath() { return tmpUploadDirPath; }

	@Override
	public void afterPropertiesSet() throws Exception {
		createDirectory("/");
	}

	@Override
	public Path writeString(String path, String content) throws IOException {
		//try {
			return Files.write(Paths.get(tmpUploadDirPath + File.separator + path), content.getBytes());
		/*} catch (IOException e) {
			logger.error("Failed to write temporary file", e);
			return null;
		}*/
	}

	@Override
	public void createDirectory(String path) throws IOException {
		if (!Files.exists(Paths.get(tmpUploadDirPath + File.separator + path))) {
			//try {
				Files.createDirectories(Paths.get(tmpUploadDirPath + File.separator + path));
			/*} catch (IOException e) {
				logger.error("Failed to create temporary directory", e);
			}*/
		}
	}
}
