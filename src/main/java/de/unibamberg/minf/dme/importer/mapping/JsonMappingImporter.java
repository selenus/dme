package de.unibamberg.minf.dme.importer.mapping;

import java.io.File;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import de.unibamberg.minf.dme.model.serialization.MappingContainer;


@Component
@Scope(value=ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class JsonMappingImporter extends BaseMappingImporter {

	@Override public boolean isKeepImportedIdsSupported() { return false; }
	@Override public String getMainImporterType() { return "JSON"; }

	
	@Override
	public boolean getIsSupported() {
		if (super.getIsSupported()) {
			try {
				objectMapper.readValue(new File(this.getMappingFilePath()), MappingContainer.class);
				return true;
			} catch (Exception e) {
				return false;
			}
		}
		return false;
	}
	
	@Override
	protected void importJson() {
		// TODO Auto-generated method stub
		
	}

}
