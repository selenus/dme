package de.unibamberg.minf.dme.importer.json;

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

import de.unibamberg.minf.dme.model.base.Element;
import de.unibamberg.minf.dme.model.base.ModelElement;
import de.unibamberg.minf.dme.model.base.NamedModelElement;
import de.unibamberg.minf.dme.model.base.Nonterminal;
import de.unibamberg.minf.dme.model.datamodel.NonterminalImpl;
import de.unibamberg.minf.dme.model.datamodel.base.DatamodelNature;
import de.unibamberg.minf.dme.model.exception.MetamodelConsistencyException;
import de.unibamberg.minf.dme.model.serialization.DatamodelContainer;
import de.unibamberg.minf.dme.service.ElementServiceImpl;
import de.unibamberg.minf.dme.service.IdentifiableServiceImpl;

@Component
@Scope(value=ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class JsonDatamodelImporter extends BaseJsonImporter {

	@Override public String getImporterSubtype() { return "Datamodel"; }
		
	@Override
	public boolean getIsSupported() {
		if (super.getIsSupported()) {
			try {
				objectMapper.readValue(new File(this.getSchemaFilePath()), DatamodelContainer.class);
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
			DatamodelContainer s = objectMapper.readValue(new File(this.getSchemaFilePath()), DatamodelContainer.class);			
			return IdentifiableServiceImpl.extractAllByTypes(s.getRoot(), allowedSubtreeRoots);
		} catch (Exception e) {
			logger.error("Attempting legacy schema deserialization", e);
			return null;
		}
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
	protected void importJson() throws JsonParseException, JsonMappingException, IOException, MetamodelConsistencyException {
		DatamodelContainer s = objectMapper.readValue(new File(this.getSchemaFilePath()), DatamodelContainer.class);
		s.getModel().setId(this.getSchema().getId());

		this.importModel(s.getModel(), (Nonterminal)s.getRoot(), s.getGrammars());
	}
	
}
