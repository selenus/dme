package de.unibamberg.minf.dme.importer.mapping;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import de.unibamberg.minf.core.util.Stopwatch;
import de.unibamberg.minf.dme.exception.MappingImportException;
import de.unibamberg.minf.dme.importer.BaseImporter;
import de.unibamberg.minf.dme.model.base.Element;
import de.unibamberg.minf.dme.model.base.Grammar;
import de.unibamberg.minf.dme.model.grammar.GrammarContainer;
import de.unibamberg.minf.dme.model.mapping.MappedConceptImpl;
import de.unibamberg.minf.dme.model.mapping.base.MappedConcept;
import de.unibamberg.minf.dme.model.mapping.base.Mapping;
import de.unibamberg.minf.dme.service.interfaces.ElementService;

public abstract class BaseMappingImporter extends BaseImporter implements MappingImporter {
	
	@Autowired private ElementService elementService;
	
	private Mapping mapping;
	private MappingImportListener importListener;	
	private List<Element> sourceElements;
	private List<Element> targetElements;
	
	private List<MappedConcept> importedConcepts;
	private Map<String, String> importedFunctions;
	private Map<String, Grammar> importedGrammars;
	
	public Mapping getMapping() { return mapping; }
	@Override public void setMapping(Mapping mapping) { this.mapping = mapping; }

	public MappingImportListener getImportListener() { return importListener; }
	@Override public void setImportListener(MappingImportListener importListener) { this.importListener = importListener; }
	
	
	@Override
	public void run() {
		Stopwatch sw = new Stopwatch().start();
		
		logger.debug(String.format("Started importing mapping %s", this.getMapping().getId()));
		try {
			this.setupDatamodels();
			this.importJson();
			if (this.getImportListener()!=null) {
				logger.info(String.format("Finished importing mapping %s in %sms", this.getMapping().getId(), sw.getElapsedTime()));
				this.getImportListener().registerImportFinished(this.getMapping(), this.importedConcepts, this.importedFunctions, this.importedGrammars, this.auth);
			}
		} catch (Exception e) {
			logger.error("Error while importing JSON Mapping", e);
			if (this.getImportListener()!=null) {
				this.getImportListener().registerImportFailed(this.getMapping());
			}
		}
	}
	
	protected void importMapping(Mapping m, Map<String, String> functions, Map<String,Grammar> grammars) {
		importedConcepts = new ArrayList<MappedConcept>();
		importedFunctions = new HashMap<String, String>();
		importedGrammars = new HashMap<String, Grammar>();
		
		MappedConcept importedMc;
		List<String> targetElementIds, sourceElementIds;
		String grammarId;
		String setId;
		
		if (m.getConcepts()!=null) {
			for (MappedConcept mc : m.getConcepts()) {
				if (mc.getTargetElementIds()==null) {
					// No target elements at all
					continue;
				}
				
				targetElementIds = new ArrayList<String>(mc.getTargetElementIds());
				this.matchElementIds(targetElementIds, targetElements);
				
				sourceElementIds = new ArrayList<String>(mc.getElementGrammarIdsMap().keySet());
				this.matchElementIds(sourceElementIds, sourceElements);
				
				// TODO: Match ids first, when unsuccessful match element label paths				
				if (targetElementIds.size()==0 || sourceElementIds.size()==0) {
					// No matching source and/or target elements
					continue;
				}
				
				importedMc = new MappedConceptImpl();
				importedMc.setId(this.getOrCreateId(mc.getId()));
				importedMc.setEntityId(this.mapping.getId());
				importedMc.setTargetElementIds(targetElementIds);
				
				// Collect imported real grammars, setting a null ID for missing containers (passthrough grammars)
				importedMc.setElementGrammarIdsMap(new HashMap<String, String>());
				for (String sourceElementId : sourceElementIds) {
					grammarId = mc.getElementGrammarIdsMap().get(sourceElementId);
					if (grammarId!=null && grammars!=null && grammars.containsKey(grammarId)) {
						setId = this.getOrCreateId(grammarId); 
						importedMc.getElementGrammarIdsMap().put(sourceElementId, setId);
						importedGrammars.put(setId, grammars.get(grammarId));
						break;
					} else {
						importedMc.getElementGrammarIdsMap().put(sourceElementId, null);
					}
				}
				
				// If a function is provided, use it, otherwise leave null (assuming value-assignment function)
				if (mc.getFunctionId()!=null && functions.containsKey(mc.getFunctionId())) {
					setId = this.getOrCreateId(mc.getFunctionId());
					importedMc.setFunctionId(setId);
					if (functions.get(mc.getFunctionId())!=null) {
						importedFunctions.put(setId, functions.get(mc.getFunctionId()));
					}
				}
				importedConcepts.add(importedMc);
			}
		}
	}	
	
	private void matchElementIds(List<String> elementIds, List<Element> matchElements) {
		List<String> retainTargetIds = new ArrayList<String>();
		for (String elementId : elementIds) {
			if (this.findElementById(elementId, matchElements)!=null && !retainTargetIds.contains(elementId)) {
				retainTargetIds.add(elementId);
			}
		}
		elementIds.retainAll(retainTargetIds);
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
	
	private void setupDatamodels() throws MappingImportException {
		if (this.getMapping()==null) {
			throw new MappingImportException("No import target mapping specified");
		}
		sourceElements = this.elementService.findBySchemaId(this.getMapping().getSourceId());
		targetElements = this.elementService.findBySchemaId(this.getMapping().getTargetId());
	}
	
	
	protected abstract void importJson();
}
