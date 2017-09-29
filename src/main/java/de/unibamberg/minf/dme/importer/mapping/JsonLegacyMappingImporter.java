package de.unibamberg.minf.dme.importer.mapping;

import java.io.File;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import eu.dariah.de.minfba.core.metamodel.interfaces.Mapping;
import eu.dariah.de.minfba.core.metamodel.serialization.SerializableMappingContainer;

@Component
@Scope(value=ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class JsonLegacyMappingImporter extends JsonMappingImporter {

	private Mapping legacyMapping;
	
	@Override
	public boolean isKeepImportedIdsSupported() {
		return true;
	}
	
	@Override
	public boolean getIsSupported() {
		if (super.getIsSupported()) {
			try {
				objectMapper.readValue(new File(this.getMappingFilePath()), SerializableMappingContainer.class);
				return true;
			} catch (Exception e) {
				return false;
			}
		}
		return false;
	}
}
