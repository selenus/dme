package de.unibamberg.minf.dme.importer.mapping.json;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.unibamberg.minf.dme.importer.mapping.BaseMappingImporter;
import de.unibamberg.minf.dme.importer.mapping.MappingImporter;

public abstract class BaseJsonMappingImporter extends BaseMappingImporter implements MappingImporter {
	@Autowired protected ObjectMapper objectMapper;
	
	@Override public boolean isKeepImportedIdsSupported() { return false; }
	@Override public String getMainImporterType() { return "JSON"; }
	
	public boolean getIsSupported() {
		try {
			final JsonParser parser = objectMapper.getFactory().createParser(new File(this.importFilePath));
			while (parser.nextToken() != null) {}
			return true;
		} catch (Exception e) {
			return false;			
		}
	}
	
	protected abstract void importJson();
}
