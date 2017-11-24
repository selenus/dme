package de.unibamberg.minf.dme.importer.datamodel.json;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

import de.unibamberg.minf.dme.model.base.Element;
import de.unibamberg.minf.dme.model.base.Identifiable;
import de.unibamberg.minf.dme.model.base.ModelElement;
import de.unibamberg.minf.dme.model.datamodel.NonterminalImpl;
import de.unibamberg.minf.dme.model.datamodel.base.Datamodel;
import de.unibamberg.minf.dme.model.datamodel.base.DatamodelNature;
import de.unibamberg.minf.dme.model.exception.MetamodelConsistencyException;
import de.unibamberg.minf.dme.model.reference.Reference;
import de.unibamberg.minf.dme.model.reference.ReferenceHelper;
import de.unibamberg.minf.dme.model.serialization.DatamodelReferenceContainer;
import de.unibamberg.minf.dme.service.base.BaseReferenceServiceImpl;
import de.unibamberg.minf.dme.service.interfaces.ReferenceService;

@Component
@Scope(value=ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class JsonDatamodelReferenceImporter extends BaseJsonDatamodelImporter {
	
	@Autowired private ReferenceService referenceService;
	
	@Override public String getImporterSubtype() { return "Datamodel v1.1"; }
	
	@Override
	public boolean getIsSupported() {
		if (super.getIsSupported()) {
			try {
				objectMapper.readValue(new File(this.importFilePath), DatamodelReferenceContainer.class);
				return true;
			} catch (Exception e) {
				logger.warn("Import error", e);
				return false;
			}
		}
		return false;
	}

	@Override
	public List<? extends Identifiable> getPossibleRootElements() {
		try {
			List<Class<? extends ModelElement>> allowedRootTypes = new ArrayList<Class<? extends ModelElement>>();
			allowedRootTypes.add(NonterminalImpl.class);
			
			return this.getElementsByTypes(allowedRootTypes);
		} catch (Exception e) {
			logger.error("Failed to retrieve possible root elements for schema", e);
			return null;
		}
	}

	@Override
	public List<? extends ModelElement> getElementsByTypes(List<Class<? extends ModelElement>> allowedSubtreeRoots) {
		try {
			DatamodelReferenceContainer s = objectMapper.readValue(new File(this.importFilePath), DatamodelReferenceContainer.class);
			List<ModelElement> result = new ArrayList<ModelElement>();
			if (s.getElements()!=null) {
				for (ModelElement me : s.getElements().values()) {
					if (allowedSubtreeRoots.contains(me.getClass())) {
						result.add(me);
					}
				}
			}
			return result;
		} catch (Exception e) {
			logger.error("Failed to extract model elements from serialized datamodel", e);
			return null;
		}
	}

	@Override
	protected void importJson() throws JsonParseException, JsonMappingException, IOException, MetamodelConsistencyException {
		DatamodelReferenceContainer s = objectMapper.readValue(new File(this.importFilePath), DatamodelReferenceContainer.class);
		Datamodel m = s.getModel();
		m.setId(this.getDatamodel().getId());
		
		Reference importRoot = referenceService.findReferenceById(s.getRoot(), this.getRootElementName());
		
		Element e = (Element)ReferenceHelper.fillElement(importRoot, s.getElements());
		
		Map<String, String> oldToNewIdMap = new HashMap<String, String>();
		this.reworkElementHierarchy(m.getId(), e, oldToNewIdMap, new HashMap<String, String>(), null);
		
		if (!isKeepImportedIds()) {
			if (m.getNatures()!=null) {
				for (DatamodelNature nature : m.getNatures()) {
					this.regenerateTerminalIds(nature, this.getDatamodel().getId(), oldToNewIdMap);
				}
			}
		}
		
		if (this.getRootElementType()==null || this.getRootElementType().isEmpty()) {
			this.setRootElementType(e.getClass().getName());
		}
		
		this.setRootElements(new ArrayList<ModelElement>());
		this.getRootElements().add(e);
	
		this.setDatamodel(m);
	}
}
