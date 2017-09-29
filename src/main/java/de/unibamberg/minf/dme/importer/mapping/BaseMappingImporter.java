package de.unibamberg.minf.dme.importer.mapping;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.unibamberg.minf.core.util.Stopwatch;
import de.unibamberg.minf.dme.importer.MappingImporter;
import de.unibamberg.minf.dme.model.mapping.base.Mapping;
import eu.dariah.de.dariahsp.model.web.AuthPojo;

public abstract class BaseMappingImporter implements MappingImporter {
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired protected ObjectMapper objectMapper;
	
	private Mapping mapping;
	private String mappingFilePath;
	private MappingImportListener importListener;
	private AuthPojo auth;
	
	
	public Mapping getMapping() { return mapping; }
	@Override public void setMapping(Mapping mapping) { this.mapping = mapping; }
	
	public String getMappingFilePath() { return mappingFilePath; }
	@Override public void setMappingFilePath(String mappingFilePath) { this.mappingFilePath = mappingFilePath; }

	public MappingImportListener getImportListener() { return importListener; }
	@Override public void setImportListener(MappingImportListener importListener) { this.importListener = importListener; }
	
	public AuthPojo getAuth() { return auth; }
	@Override public void setAuth(AuthPojo auth) { this.auth = auth; }
	
	
	@Override
	public boolean getIsSupported() {
		try {
			final JsonParser parser = objectMapper.getFactory().createParser(new File(this.getMappingFilePath()));
			while (parser.nextToken() != null) {}
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	@Override
	public void run() {
		Stopwatch sw = new Stopwatch().start();
		logger.debug(String.format("Started importing mapping %s", this.getMapping().getId()));
		try {
			this.importJson();
			if (this.getImportListener()!=null) {
				logger.info(String.format("Finished importing mapping %s in %sms", this.getMapping().getId(), sw.getElapsedTime()));
				this.getImportListener().registerImportFinished(this.getMapping(), this.getAuth());
			}
		} catch (Exception e) {
			logger.error("Error while importing JSON Mapping", e);
			if (this.getImportListener()!=null) {
				this.getImportListener().registerImportFailed(this.getMapping());
			}
		}
	}
	
	protected abstract void importJson();
}
