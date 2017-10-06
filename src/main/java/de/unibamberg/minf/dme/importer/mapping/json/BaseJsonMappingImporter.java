package de.unibamberg.minf.dme.importer.mapping.json;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.unibamberg.minf.dme.exception.MappingImportException;
import de.unibamberg.minf.dme.importer.mapping.BaseMappingImporter;
import de.unibamberg.minf.dme.importer.mapping.MappingImporter;
import de.unibamberg.minf.dme.model.base.Element;
import de.unibamberg.minf.dme.model.grammar.GrammarContainer;
import de.unibamberg.minf.dme.model.mapping.base.MappedConcept;
import de.unibamberg.minf.dme.model.mapping.base.Mapping;
import de.unibamberg.minf.dme.service.interfaces.ElementService;
import de.unibamberg.minf.dme.service.interfaces.MappedConceptService;

public abstract class BaseJsonMappingImporter extends BaseMappingImporter implements MappingImporter {
	@Autowired protected ObjectMapper objectMapper;
	
	@Autowired private ElementService elementService;
	@Autowired private MappedConceptService conceptService;
	
	private List<Element> sourceElements;
	private List<Element> targetElements;
	
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
	
	@Override
	public void run() {
		try {
			this.setupDatamodels();
		} catch (MappingImportException e) {
			logger.error("Failed to setup mapping importer", e);
		}
		super.run();
	}
	
	private void setupDatamodels() throws MappingImportException {
		if (this.getMapping()==null) {
			throw new MappingImportException("No import target mapping specified");
		}
		sourceElements = this.elementService.findBySchemaId(this.getMapping().getSourceId());
		targetElements = this.elementService.findBySchemaId(this.getMapping().getTargetId());
	}
	
	protected void importMapping(Mapping m, Map<String, String> functions, Map<String, GrammarContainer> grammars) {
		if (m.getConcepts()!=null) {
			String function;
			for (MappedConcept mc : m.getConcepts()) {
				
				
				
				
				
				if (!keepImportedIds) {
					mc.setId(null);
				}
				
				function = functions.get(mc.getFunctionId());
				if (!keepImportedIds) {
					mc.setFunctionId(null);
				}
				
				
				
			}
		}
	}
	
	private Element findElementById(String id, List<Element> elements) {
		if (elements==null) {
			return null;
		}
		for (Element e : elements) {
			if (e.getId().equals(id)) {
				return e;
			}
		}
		return null;
	}
	
	protected abstract void importJson();
}
