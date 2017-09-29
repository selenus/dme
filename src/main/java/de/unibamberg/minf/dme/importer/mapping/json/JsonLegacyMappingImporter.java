package de.unibamberg.minf.dme.importer.mapping.json;

import java.io.File;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import eu.dariah.de.minfba.core.metamodel.interfaces.Mapping;
import eu.dariah.de.minfba.core.metamodel.serialization.SerializableMappingContainer;

@Component
@Scope(value=ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class JsonLegacyMappingImporter extends BaseJsonMappingImporter {

	private Mapping legacyMapping;
	
	@Override public boolean isKeepImportedIdsSupported() { return true; }
	@Override public String getImporterSubtype() { return "Legacy mapping"; }
	
	@Override
	public boolean getIsSupported() {
		if (super.getIsSupported()) {
			try {
				objectMapper.readValue(new File(this.importFilePath), SerializableMappingContainer.class);
				return true;
			} catch (Exception e) {}
		}
		return false;
	}

	@Override
	protected void importJson() {
		// TODO Auto-generated method stub
		
	}
}
