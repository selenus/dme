package de.unibamberg.minf.dme.importer.mapping.json;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.unibamberg.minf.dme.model.serialization.MappingContainer;


@Component
@Scope(value=ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class JsonMappingImporter extends BaseJsonMappingImporter {

	@Autowired protected ObjectMapper objectMapper;

	@Override public boolean isKeepImportedIdsSupported() { return true; }
	@Override public String getImporterSubtype() { return "Mapping"; }
	
	@Override
	public boolean getIsSupported() {
		if (super.getIsSupported()) {
			try {
				objectMapper.readValue(new File(this.importFilePath), MappingContainer.class);
				return true;
			} catch (Exception e) {}
		}
		return false;
	}
	
	@Override
	protected void importJson() {
		MappingContainer mc;
		try {
			mc = objectMapper.readValue(new File(this.importFilePath), MappingContainer.class);
			this.importMapping(mc.getMapping(), mc.getFunctions(), mc.getGrammars());
		} catch (Exception e) {
			logger.error("Failed to deserialize JSON mapping specification", e);
		}
	}
}
