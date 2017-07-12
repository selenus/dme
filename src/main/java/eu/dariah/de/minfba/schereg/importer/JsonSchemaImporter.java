package eu.dariah.de.minfba.schereg.importer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.dariah.de.minfba.core.metamodel.exception.MetamodelConsistencyException;
import eu.dariah.de.minfba.core.metamodel.function.DescriptionGrammarImpl;
import eu.dariah.de.minfba.core.metamodel.function.GrammarContainer;
import eu.dariah.de.minfba.core.metamodel.function.TransformationFunctionImpl;
import eu.dariah.de.minfba.core.metamodel.interfaces.Element;
import eu.dariah.de.minfba.core.metamodel.interfaces.Identifiable;
import eu.dariah.de.minfba.core.metamodel.interfaces.Nonterminal;
import eu.dariah.de.minfba.core.metamodel.interfaces.Schema;
import eu.dariah.de.minfba.core.metamodel.interfaces.SchemaNature;
import eu.dariah.de.minfba.core.metamodel.interfaces.Terminal;
import eu.dariah.de.minfba.core.metamodel.serialization.SerializableSchemaContainer;
import eu.dariah.de.minfba.core.util.Stopwatch;
import eu.dariah.de.minfba.schereg.service.ElementServiceImpl;
import eu.dariah.de.minfba.schereg.service.IdentifiableServiceImpl;

/**
 * Importer for JSON based schema definitions
 *  Currently only schemata in the native format (serializations of SerializableSchemaContainer) are supported
 * 
 * @author Tobias Gradl
 *
 */
@Component
@Scope(value=ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class JsonSchemaImporter extends BaseSchemaImporter implements SchemaImporter {

	@Autowired private ObjectMapper objectMapper;
	
	
	@Override
	public void run() {
		Stopwatch sw = new Stopwatch().start();
		logger.debug(String.format("Started importing schema %s", this.getSchema().getEntityId()));
		try {
			this.importSerializedJsonSchema();
			if (this.getListener()!=null) {
				logger.info(String.format("Finished importing schema %s in %sms", this.getSchema().getId(), sw.getElapsedTime()));
				this.getListener().registerImportFinished(this.getSchema(), this.getElementId(), this.getRootNonterminal(), this.getAdditionalRootElements(), this.getAuth());
			}
		} catch (Exception e) {
			logger.error("Error while importing JSON Schema", e);
			if (this.getListener()!=null) {
				this.getListener().registerImportFailed(this.getSchema());
			}
		}
	}

	@Override
	public List<? extends Identifiable> getElementsByTypes(List<Class<? extends Identifiable>> allowedSubtreeRoots) {
		try {
			SerializableSchemaContainer s = objectMapper.readValue(new File(this.getSchemaFilePath()), SerializableSchemaContainer.class);

			return IdentifiableServiceImpl.extractAllByTypes(s.getRoot(), allowedSubtreeRoots);
		} catch (Exception e) {
			logger.error("Failed to deserialize JSON schema", e);
			return null;
		}
	}
	
	private void importSerializedJsonSchema() throws JsonParseException, JsonMappingException, IOException, MetamodelConsistencyException {
		SerializableSchemaContainer s = objectMapper.readValue(new File(this.getSchemaFilePath()), SerializableSchemaContainer.class);
		s.getSchema().setId(this.getSchema().getId());

		Map<String, String> nonterminalIdMap = new HashMap<String, String>();
		this.regenerateElementIds(this.getSchema(), s.getRoot(), nonterminalIdMap, s.getGrammars());
		
		for (SchemaNature nature : s.getSchema().getNatures()) {
			this.regenerateTerminalIds(nature, this.getSchema().getId(), nonterminalIdMap);
		}
		
		this.setRootNonterminal((Nonterminal)s.getRoot());
		this.setSchema(s.getSchema());
	}

	@Override
	public boolean getIsSupported() {
		boolean validJson = false;
		try {
			final JsonParser parser = objectMapper.getFactory().createParser(new File(this.getSchemaFilePath()));
			while (parser.nextToken() != null) {
			}
			validJson = true;
		} catch (Exception e) {
			validJson = false;			
		}
	   return validJson;
	}

	@Override
	public List<Element> getPossibleRootElements() {
		try {
			SerializableSchemaContainer s = objectMapper.readValue(new File(this.getSchemaFilePath()), SerializableSchemaContainer.class);
			
			List<Element> rootElements = new ArrayList<Element>();
			rootElements.addAll(ElementServiceImpl.extractAllNonterminals((Nonterminal)s.getRoot()));
			
			return rootElements;
		} catch (Exception e) {
			logger.error("Failed to retrieve possible root elements for schema", e);
			return null;
		}
	}

	@Override
	public String[] getNamespaces() {
		return new String[]{""};
	}
	
	private void regenerateElementIds(Schema schema, Element element, Map<String, String> nonterminalIdMap, Map<String, GrammarContainer> grammarContainerMap) throws MetamodelConsistencyException {
		element.setEntityId(schema.getId());
		
		String newId = new ObjectId().toString();		
		nonterminalIdMap.put(element.getId(), newId);
		element.setId(newId);
		
		List<Element> children = element.getAllChildElements();
		if (children!=null) {
			for (Element child : children) {
				this.regenerateElementIds(schema, child, nonterminalIdMap, grammarContainerMap);
			}
		}
		if (element.getGrammars()!=null) {
			for (DescriptionGrammarImpl g : element.getGrammars()) {
				g.setEntityId(schema.getId());
				
				if (grammarContainerMap!=null && grammarContainerMap.containsKey(g.getId())) {
					g.setGrammarContainer(grammarContainerMap.get(g.getId()));
				}
				
				newId = new ObjectId().toString();
				nonterminalIdMap.put(g.getId(), newId);
				g.setId(newId);
				
				if (g.getTransformationFunctions()!=null) {
					for (TransformationFunctionImpl f : g.getTransformationFunctions()) {
						f.setEntityId(schema.getId());
						newId = new ObjectId().toString();
						nonterminalIdMap.put(f.getId(), newId);
						f.setId(newId);
					}
				}
			}
		}
	}
	
	private void regenerateTerminalIds(SchemaNature nature, String entityId, Map<String, String> nonterminalIdMap) throws MetamodelConsistencyException {
		Map<String, String> oldNonterminalTerminalIdMap = new HashMap<String, String>(nature.getNonterminalTerminalIdMap());
		nature.setNonterminalTerminalIdMap(new HashMap<String, String>());
		
		String newId;
		for (Terminal t : nature.getTerminals()) {
			newId = new ObjectId().toString();
			for (String oldNonterminalId : oldNonterminalTerminalIdMap.keySet()) {
				if (oldNonterminalTerminalIdMap.get(oldNonterminalId).equals(t.getId())) {
					nature.mapNonterminal(nonterminalIdMap.get(oldNonterminalId), newId);
				}
			}
			t.setId(newId);
		}
	}
}
