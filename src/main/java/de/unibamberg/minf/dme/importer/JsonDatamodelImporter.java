package de.unibamberg.minf.dme.importer;

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

import de.unibamberg.minf.core.util.Stopwatch;
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
import de.unibamberg.minf.dme.model.serialization.DatamodelContainer;
import de.unibamberg.minf.dme.service.ElementServiceImpl;
import de.unibamberg.minf.dme.service.IdentifiableServiceImpl;

/**
 * Importer for JSON based schema definitions
 *  Currently only schemata in the native format (serializations of SerializableSchemaContainer) are supported
 * 
 * @author Tobias Gradl
 *
 */
@Component
@Scope(value=ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class JsonDatamodelImporter extends BaseSchemaImporter implements SchemaImporter {

	@Autowired private ObjectMapper objectMapper;
	
	
	@Override
	public void run() {
		Stopwatch sw = new Stopwatch().start();
		logger.debug(String.format("Started importing schema %s", this.getSchema().getId()));
		try {
			this.importSerializedJsonSchema();
			if (this.getListener()!=null) {
				logger.info(String.format("Finished importing schema %s in %sms", this.getSchema().getId(), sw.getElapsedTime()));
				this.getListener().registerImportFinished(this.getSchema(), this.getElementId(), this.getRootElements(), this.getAdditionalRootElements(), this.getAuth());
			}
		} catch (Exception e) {
			logger.error("Error while importing JSON Schema", e);
			if (this.getListener()!=null) {
				this.getListener().registerImportFailed(this.getSchema());
			}
		}
	}

	@Override
	public List<? extends ModelElement> getElementsByTypes(List<Class<? extends ModelElement>> allowedSubtreeRoots) {
		try {
			DatamodelContainer s = objectMapper.readValue(new File(this.getSchemaFilePath()), DatamodelContainer.class);			
			return IdentifiableServiceImpl.extractAllByTypes(s.getRoot(), allowedSubtreeRoots);
		} catch (Exception e) {
			logger.error("Attempting legacy schema deserialization", e);
			return null;
		}
	}
	
	private void importSerializedJsonSchema() throws JsonParseException, JsonMappingException, IOException, MetamodelConsistencyException {
		DatamodelContainer s = objectMapper.readValue(new File(this.getSchemaFilePath()), DatamodelContainer.class);
		s.getModel().setId(this.getSchema().getId());

		Map<String, String> nonterminalIdMap = new HashMap<String, String>();
		this.regenerateElementIds(this.getSchema(), s.getRoot(), nonterminalIdMap, s.getGrammars());
		
		if (s.getModel().getNatures()!=null) {
			for (DatamodelNature nature : s.getModel().getNatures()) {
				this.regenerateTerminalIds(nature, this.getSchema().getId(), nonterminalIdMap);
			}
		}
		
		if (this.getRootElementType()==null || this.getRootElementType().isEmpty()) {
			this.setRootElementType(NonterminalImpl.class.getName());
		}
		
		List<ModelElement> possibleElements = IdentifiableServiceImpl.extractAllByType(s.getRoot(), this.getRootElementType());
		if (possibleElements!=null) {
			for (ModelElement i : possibleElements) {
				if (NamedModelElement.class.isAssignableFrom(i.getClass()) && ((NamedModelElement)i).getName().equals(this.getRootElementName())) {
					this.getRootElements().add(i);
				}
			}
		}
		this.setSchema(s.getModel());
	}

	@Override
	public boolean getIsSupported() {
		boolean validJson = false;
		try {
			final JsonParser parser = objectMapper.getFactory().createParser(new File(this.getSchemaFilePath()));
			while (parser.nextToken() != null) {
			}
			objectMapper.readValue(new File(this.getSchemaFilePath()), DatamodelContainer.class);
			validJson = true;
		} catch (Exception e) {
			validJson = false;			
		}
	   return validJson;
	}

	@Override
	public List<Element> getPossibleRootElements() {
		try {
			DatamodelContainer s = objectMapper.readValue(new File(this.getSchemaFilePath()), DatamodelContainer.class);
			
			this.getRootElements().addAll(ElementServiceImpl.extractAllNonterminals((Nonterminal)s.getRoot()));
			
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
	public String[] getNamespaces() {
		return new String[]{""};
	}
	
	private void regenerateElementIds(Datamodel schema, Element element, Map<String, String> nonterminalIdMap, Map<String, GrammarContainer> grammarContainerMap) throws MetamodelConsistencyException {
		element.setEntityId(schema.getId());
		
		String newId = new ObjectId().toString();		
		nonterminalIdMap.put(element.getId(), newId);
		element.setId(newId);
		
		List<? extends Element> children = null;
		if (Nonterminal.class.isAssignableFrom(element.getClass())) {
			children = ((Nonterminal)element).getChildNonterminals();
		} else if (Label.class.isAssignableFrom(element.getClass())) {
			children = ((Label)element).getSubLabels();
		}
		
		if (children!=null) {
			for (Element child : children) {
				this.regenerateElementIds(schema, child, nonterminalIdMap, grammarContainerMap);
			}
		}
		if (element.getGrammars()!=null) {
			for (Grammar g : element.getGrammars()) {
				g.setEntityId(schema.getId());
				
				if (grammarContainerMap!=null && grammarContainerMap.containsKey(g.getId())) {
					g.setGrammarContainer(grammarContainerMap.get(g.getId()));
				}
				
				newId = new ObjectId().toString();
				nonterminalIdMap.put(g.getId(), newId);
				g.setId(newId);
				
				if (g.getFunctions()!=null) {
					for (Function f : g.getFunctions()) {
						f.setEntityId(schema.getId());
						newId = new ObjectId().toString();
						nonterminalIdMap.put(f.getId(), newId);
						f.setId(newId);
						
						if (f.getOutputElements()!=null) {
							for (Label fOut : f.getOutputElements()) {
								this.regenerateElementIds(schema, fOut, nonterminalIdMap, grammarContainerMap);
							}
						}
					}
				}
			}
		}
	}
	
	private void regenerateTerminalIds(DatamodelNature nature, String entityId, Map<String, String> nonterminalIdMap) throws MetamodelConsistencyException {
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
