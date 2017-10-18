package de.unibamberg.minf.dme.importer.datamodel.json;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.unibamberg.minf.core.util.Stopwatch;
import de.unibamberg.minf.dme.importer.datamodel.BaseDatamodelImporter;
import de.unibamberg.minf.dme.importer.datamodel.DatamodelImporter;
import de.unibamberg.minf.dme.model.base.Element;
import de.unibamberg.minf.dme.model.base.Function;
import de.unibamberg.minf.dme.model.base.Grammar;
import de.unibamberg.minf.dme.model.base.Label;
import de.unibamberg.minf.dme.model.base.ModelElement;
import de.unibamberg.minf.dme.model.base.NamedModelElement;
import de.unibamberg.minf.dme.model.base.Nonterminal;
import de.unibamberg.minf.dme.model.base.Terminal;
import de.unibamberg.minf.dme.model.datamodel.NonterminalImpl;
import de.unibamberg.minf.dme.model.datamodel.base.Datamodel;
import de.unibamberg.minf.dme.model.datamodel.base.DatamodelNature;
import de.unibamberg.minf.dme.model.exception.MetamodelConsistencyException;
import de.unibamberg.minf.dme.model.grammar.GrammarContainer;
import de.unibamberg.minf.dme.service.IdentifiableServiceImpl;

/**
 * Importer for JSON based schema definitions
 *  Currently only schemata in the native format (serializations of SerializableSchemaContainer) are supported
 * 
 * @author Tobias Gradl
 *
 */
public abstract class BaseJsonDatamodelImporter extends BaseDatamodelImporter implements DatamodelImporter {
	@Autowired protected ObjectMapper objectMapper;
	
	@Override public boolean isKeepImportedIdsSupported() { return true; } 	
	@Override public String getMainImporterType() { return "JSON"; }
		
	@Override
	public void run() {
		Stopwatch sw = new Stopwatch().start();
		logger.debug(String.format("Started importing datamodel %s", this.getDatamodel().getId()));
		try {
			this.importJson();
			if (this.getListener()!=null) {
				logger.info(String.format("Finished importing datamodel %s in %sms", this.getDatamodel().getId(), sw.getElapsedTime()));
				this.getListener().registerImportFinished(this.getDatamodel(), this.getElementId(), this.getRootElements(), this.getAdditionalRootElements(), this.auth);
			}
		} catch (Exception e) {
			logger.error("Error while importing JSON Datamodel", e);
			if (this.getListener()!=null) {
				this.getListener().registerImportFailed(this.getDatamodel());
			}
		}
	}

	@Override
	public boolean getIsSupported() {
		boolean validJson = false;
		try {
			final JsonParser parser = objectMapper.getFactory().createParser(new File(this.importFilePath));
			while (parser.nextToken() != null) {
			}
			validJson = true;
		} catch (Exception e) {
			validJson = false;			
		}
	   return validJson;
	}

	@Override
	public String[] getNamespaces() {
		return new String[]{""};
	}
		
	protected void importModel(Datamodel m, Nonterminal root, Map<String, GrammarContainer> grammars) throws MetamodelConsistencyException {
		Map<String, String> newToOldIdMap = new HashMap<String, String>();
		Map<String, String> oldToNewIdMap = new HashMap<String, String>();
		
		this.reworkElementHierarchy(this.getDatamodel(), root, oldToNewIdMap, newToOldIdMap, grammars);
		
		if (!isKeepImportedIds()) {
			if (m.getNatures()!=null) {
				for (DatamodelNature nature : m.getNatures()) {
					this.regenerateTerminalIds(nature, this.getDatamodel().getId(), oldToNewIdMap);
				}
			}
		}
		
		if (this.getRootElementType()==null || this.getRootElementType().isEmpty()) {
			this.setRootElementType(NonterminalImpl.class.getName());
		}
		
		List<ModelElement> possibleElements = IdentifiableServiceImpl.extractAllByType(root, this.getRootElementType());
		if (possibleElements!=null) {
			for (ModelElement i : possibleElements) {
				if (NamedModelElement.class.isAssignableFrom(i.getClass()) && ((NamedModelElement)i).getName().equals(this.getRootElementName())) {
					this.getRootElements().add(i);
				}
			}
		}
		this.setDatamodel(m);
	}
	
	protected abstract void importJson() throws JsonParseException, JsonMappingException, IOException, MetamodelConsistencyException;
	
	
	private void reworkElementHierarchy(Datamodel schema, Element element, Map<String, String> oldToNewIdMap, Map<String, String> newToOldIdMap, Map<String, GrammarContainer> grammarContainerMap) throws MetamodelConsistencyException {
		if (newToOldIdMap.containsKey(element.getId())) {
			return;
		}
		
		element.setEntityId(schema.getId());
		String newId = null;
		
		if (!this.isKeepImportedIds()) {
			newId = new ObjectId().toString();		
			oldToNewIdMap.put(element.getId(), newId);
			newToOldIdMap.put(newId, element.getId());
			element.setId(newId);
		} else {
			newToOldIdMap.put(element.getId(), element.getId());
		}
		
		List<? extends Element> children = null;
		if (Nonterminal.class.isAssignableFrom(element.getClass())) {
			children = ((Nonterminal)element).getChildNonterminals();
		} else if (Label.class.isAssignableFrom(element.getClass())) {
			children = ((Label)element).getSubLabels();
		}
		
		if (children!=null) {
			for (Element child : children) {
				this.reworkElementHierarchy(schema, child, oldToNewIdMap, newToOldIdMap, grammarContainerMap);
			}
		}
		if (element.getGrammars()!=null) {
			for (Grammar g : element.getGrammars()) {
				if (newToOldIdMap.containsKey(g.getId())) {
					continue;
				}
				g.setEntityId(schema.getId());
				
				if (grammarContainerMap!=null && grammarContainerMap.containsKey(g.getId())) {
					g.setGrammarContainer(grammarContainerMap.get(g.getId()));
				}
				
				if (!this.isKeepImportedIds()) {
					newId = new ObjectId().toString();
					newToOldIdMap.put(newId, g.getId());
					oldToNewIdMap.put(g.getId(), newId);
					g.setId(newId);
				} else {
					newToOldIdMap.put(g.getId(), g.getId());
				}
				
				if (g.getFunctions()!=null) {
					for (Function f : g.getFunctions()) {
						if (newToOldIdMap.containsKey(f.getId())) {
							continue;
						}
						f.setEntityId(schema.getId());
						if (!this.isKeepImportedIds()) {
							newId = new ObjectId().toString();
							newToOldIdMap.put(newId, f.getId());
							oldToNewIdMap.put(f.getId(), newId);
							f.setId(newId);
						} else {
							newToOldIdMap.put(f.getId(), f.getId());
						}
						if (f.getOutputElements()!=null) {
							for (Label fOut : f.getOutputElements()) {
								this.reworkElementHierarchy(schema, fOut, oldToNewIdMap, newToOldIdMap, grammarContainerMap);
							}
						}
					}
				}
			}
		}
	}
	
	protected void regenerateTerminalIds(DatamodelNature nature, String entityId, Map<String, String> idMap) throws MetamodelConsistencyException {
		Map<String, String> oldNonterminalTerminalIdMap = new HashMap<String, String>(nature.getNonterminalTerminalIdMap());
		nature.setNonterminalTerminalIdMap(new HashMap<String, String>());
		
		String newId;
		for (Terminal t : nature.getTerminals()) {
			newId = new ObjectId().toString();
			for (String oldNonterminalId : oldNonterminalTerminalIdMap.keySet()) {
				if (oldNonterminalTerminalIdMap.get(oldNonterminalId).equals(t.getId())) {
					nature.mapNonterminal(idMap.get(oldNonterminalId), newId);
				}
			}
			t.setId(newId);
		}
	}
}
