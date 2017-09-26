package eu.dariah.de.minfba.schereg.importer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.unibamberg.minf.dme.model.base.Identifiable;
import de.unibamberg.minf.dme.model.base.Nonterminal;
import eu.dariah.de.minfba.core.metamodel.interfaces.Element;
import eu.dariah.de.minfba.core.metamodel.serialization.SerializableSchemaContainer;
import eu.dariah.de.minfba.core.util.Stopwatch;
import eu.dariah.de.minfba.schereg.service.ElementServiceImpl;
import eu.dariah.de.minfba.schereg.service.IdentifiableServiceImpl;

@Component
@Scope(value=ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class JsonLegacySchemaImporter extends BaseSchemaImporter implements SchemaImporter {

	@Autowired private ObjectMapper objectMapper;

	@Override
	public void run() {
		Stopwatch sw = new Stopwatch().start();
		logger.debug(String.format("Started importing legacy schema %s", this.getSchema().getId()));
		try {
			//this.importSerializedJsonSchema();
			if (this.getListener()!=null) {
				logger.info(String.format("Finished importing legacy schema %s in %sms", this.getSchema().getId(), sw.getElapsedTime()));
				
				// TODO: Convert
				
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
	public boolean getIsSupported() {
		boolean validJson = false;
		try {
			final JsonParser parser = objectMapper.getFactory().createParser(new File(this.getSchemaFilePath()));
			while (parser.nextToken() != null) {
			}
			objectMapper.readValue(new File(this.getSchemaFilePath()), SerializableSchemaContainer.class);
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

	@Override
	public List<? extends Identifiable> getPossibleRootElements() {
		try {
			SerializableSchemaContainer s = objectMapper.readValue(new File(this.getSchemaFilePath()), SerializableSchemaContainer.class);
			
			List<Nonterminal> nonterminals = this.extractAllNonterminals(s.getRoot());
			
			/*this.getRootElements().addAll(ElementServiceImpl.extractAllNonterminals((Nonterminal)s.getRoot()));
			
			List<Element> result = new ArrayList<Element>(); 
			for (ModelElement me : this.getRootElements()) {
				result.add((Element)me);
			}
			return result;*/
			return null;
		} catch (Exception e) {
			logger.error("Failed to retrieve possible root elements for schema", e);
			return null;
		}
	}
	
	private List<Nonterminal> extractAllNonterminals(Element element) {
		return null;
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
	
	
	public static List<eu.dariah.de.minfba.core.metamodel.interfaces.Identifiable> extractAllByTypes(ModelElement i, List<Class<? extends ModelElement>> allowedSubtreeRoots) {
		List<ModelElement> result = new ArrayList<ModelElement>();
		if (i!=null) {
			if (allowedSubtreeRoots.contains(i.getClass())) {				
				result.add(i);
			}
			if (Nonterminal.class.isAssignableFrom(i.getClass())) {
				Nonterminal n = (Nonterminal)i;
				if (n.getChildNonterminals()!=null) {
					for (Nonterminal nChild : n.getChildNonterminals()) {
						result.addAll(extractAllByTypes(nChild, allowedSubtreeRoots));
					}
				}
				if (n.getGrammars()!=null) {
					for (Grammar g : n.getGrammars()) {
						result.addAll(extractAllByTypes(g, allowedSubtreeRoots));
					}
				}
			} else if (Label.class.isAssignableFrom(i.getClass())) {
				Label l = (Label)i;
				if (l.getSubLabels()!=null) {
					for (Label lChild : l.getSubLabels()) {
						result.addAll(extractAllByTypes(lChild, allowedSubtreeRoots));
					}
				}
				if (l.getGrammars()!=null) {
					for (Grammar g : l.getGrammars()) {
						result.addAll(extractAllByTypes(g, allowedSubtreeRoots));
					}
				}
			} else if (Grammar.class.isAssignableFrom(i.getClass())) {
				Grammar g = (Grammar)i;
				if (g.getFunctions()!=null) {
					for (Function t : g.getFunctions()) {
						result.addAll(extractAllByTypes(t, allowedSubtreeRoots));
					}
				}
			} else if (Function.class.isAssignableFrom(i.getClass())) {
				Function t = (Function)i;
				if (t.getOutputElements()!=null) {
					for (Label l : t.getOutputElements()) {
						result.addAll(extractAllByTypes(l, allowedSubtreeRoots));
					}
				}
			}
		}
		return result;
	}

	public static List<ModelElement> extractAllByType(ModelElement i, String rootElementType) {
		if (rootElementType.equals(Nonterminal.class.getName()) || rootElementType.equals(NonterminalImpl.class.getName())) {
			return extractAllByTypes(i, getNonterminalClasses());
		} else if (rootElementType.equals(Label.class.getName()) || rootElementType.equals(LabelImpl.class.getName())) {
			return extractAllByTypes(i, getLabelClasses());
		} else if (rootElementType.equals(Grammar.class.getName()) || rootElementType.equals(GrammarImpl.class.getName())) {
			return extractAllByTypes(i, getGrammarClasses());
		} else if (rootElementType.equals(Nonterminal.class.getName()) || rootElementType.equals(NonterminalImpl.class.getName())) {
			return extractAllByTypes(i, getFunctionClasses());
		}
		return null;
	}
}
