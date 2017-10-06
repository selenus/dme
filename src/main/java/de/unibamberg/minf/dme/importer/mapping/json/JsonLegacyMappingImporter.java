package de.unibamberg.minf.dme.importer.mapping.json;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import de.unibamberg.minf.dme.importer.model.LegacyMappingConverter;
import de.unibamberg.minf.dme.importer.model.LegacySchemaConverter;
import de.unibamberg.minf.dme.model.grammar.GrammarContainer;
import de.unibamberg.minf.dme.model.mapping.base.Mapping;
import eu.dariah.de.minfba.core.metamodel.serialization.SerializableMappingContainer;

@Component
@Scope(value=ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class JsonLegacyMappingImporter extends BaseJsonMappingImporter {

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
		SerializableMappingContainer mc;
		try {
			mc = objectMapper.readValue(new File(this.importFilePath), SerializableMappingContainer.class);
		
			Mapping m = LegacyMappingConverter.convertMapping(mc.getMapping());
			
			Map<String, GrammarContainer> grammars = new HashMap<String, GrammarContainer>();
			if (mc.getGrammars()!=null) {
				for (String key : mc.getGrammars().keySet()) {
					grammars.put(key, LegacySchemaConverter.convertLegacyGrammarContainer(mc.getGrammars().get(key)));
				}
			}
			this.importMapping(m, mc.getFunctions(), grammars);
			
		} catch (Exception e) {
			logger.error("Failed to deserialize JSON mapping specification", e);
		}
	}
	
}
