package de.unibamberg.minf.dme.importer.datamodel.json;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import de.unibamberg.minf.dme.importer.model.LegacySchemaConverter;
import de.unibamberg.minf.dme.model.base.Element;
import de.unibamberg.minf.dme.model.base.ModelElement;
import de.unibamberg.minf.dme.model.base.Nonterminal;
import de.unibamberg.minf.dme.model.datamodel.base.Datamodel;
import de.unibamberg.minf.dme.model.datamodel.natures.XmlDatamodelNature;
import de.unibamberg.minf.dme.model.exception.MetamodelConsistencyException;
import de.unibamberg.minf.dme.model.grammar.GrammarContainer;
import de.unibamberg.minf.dme.service.ElementServiceImpl;
import de.unibamberg.minf.dme.service.IdentifiableServiceImpl;
import eu.dariah.de.minfba.core.metamodel.serialization.SerializableSchemaContainer;

@Component
@Scope(value=ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class JsonLegacySchemaImporter extends BaseJsonDatamodelImporter {
	
	@Override public String getImporterSubtype() { return "Legacy datamodel"; }
	
	@Override
	public boolean getIsSupported() {
		if (super.getIsSupported()) {
			try {
				objectMapper.readValue(new File(this.importFilePath), SerializableSchemaContainer.class);
				return true;
			} catch (Exception e) {
				return false;
			}
		}
		return false;
	}
	
	@Override
	public List<? extends ModelElement> getElementsByTypes(List<Class<? extends ModelElement>> allowedSubtreeRoots) {
		try {
			SerializableSchemaContainer s = objectMapper.readValue(new File(this.importFilePath), SerializableSchemaContainer.class);
			Nonterminal rootN = LegacySchemaConverter.convertLegacyNonterminal((eu.dariah.de.minfba.core.metamodel.Nonterminal)s.getRoot(), null);
			
			return IdentifiableServiceImpl.extractAllByTypes(rootN, allowedSubtreeRoots);
		} catch (Exception e) {
			logger.error("Attempting legacy schema deserialization", e);
			return null;
		}
	}
	
	@Override
	public List<Element> getPossibleRootElements() {
		try {
			SerializableSchemaContainer s = objectMapper.readValue(new File(this.importFilePath), SerializableSchemaContainer.class);
			
			Nonterminal rootN = LegacySchemaConverter.convertLegacyNonterminal((eu.dariah.de.minfba.core.metamodel.Nonterminal)s.getRoot(), null);
			this.getRootElements().addAll(ElementServiceImpl.extractAllNonterminals(rootN));
			
			List<Element> result = new ArrayList<Element>(); 
			for (ModelElement me : this.getRootElements()) {
				result.add((Element)me);
			}
			return result;
		} catch (Exception e) {
			logger.error("Failed to retrieve possible root elements for schema", e);
			return null;
		}
	}
	
	@Override
	protected void importJson() throws JsonParseException, JsonMappingException, IOException, MetamodelConsistencyException {
		SerializableSchemaContainer s = objectMapper.readValue(new File(this.importFilePath), SerializableSchemaContainer.class);
		
		Datamodel m = LegacySchemaConverter.convertLegacySchema(s.getSchema());
		m.setId(this.getDatamodel().getId());
		
		Map<String, String> nonterminalTerminalIdMap = new HashMap<String, String>();
		Nonterminal root = LegacySchemaConverter.convertLegacyNonterminal((eu.dariah.de.minfba.core.metamodel.Nonterminal)s.getRoot(), nonterminalTerminalIdMap);
		
		// Processing root implemented after legacy schemas
		root.setProcessingRoot(true);
		
		m.getNature(XmlDatamodelNature.class).setNonterminalTerminalIdMap(nonterminalTerminalIdMap);
			
		Map<String, GrammarContainer> grammars = new HashMap<String, GrammarContainer>();
		if (s.getGrammars()!=null) {
			for (String key : s.getGrammars().keySet()) {
				grammars.put(key, LegacySchemaConverter.convertLegacyGrammarContainer(s.getGrammars().get(key)));
			}
		}
		this.importModel(m, root, grammars);
	}
}