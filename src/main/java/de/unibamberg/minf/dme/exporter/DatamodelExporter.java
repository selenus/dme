package de.unibamberg.minf.dme.exporter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.unibamberg.minf.dme.model.RightsContainer;
import de.unibamberg.minf.dme.model.base.Element;
import de.unibamberg.minf.dme.model.base.Function;
import de.unibamberg.minf.dme.model.base.Grammar;
import de.unibamberg.minf.dme.model.base.ModelElement;
import de.unibamberg.minf.dme.model.datamodel.DatamodelImpl;
import de.unibamberg.minf.dme.model.datamodel.base.Datamodel;
import de.unibamberg.minf.dme.model.datamodel.base.DatamodelNature;
import de.unibamberg.minf.dme.model.serialization.DatamodelReferenceContainer;
import de.unibamberg.minf.dme.model.tracking.ChangeSet;
import de.unibamberg.minf.dme.service.interfaces.ElementService;
import de.unibamberg.minf.dme.service.interfaces.FunctionService;
import de.unibamberg.minf.dme.service.interfaces.GrammarService;
import de.unibamberg.minf.dme.service.interfaces.ReferenceService;
import de.unibamberg.minf.dme.service.interfaces.SchemaService;
import eu.dariah.de.dariahsp.model.web.AuthPojo;

@Component
public class DatamodelExporter {

	@Autowired private SchemaService datamodelService;
	@Autowired private ReferenceService referenceService;
	
	@Autowired private ElementService elementService;
	@Autowired protected GrammarService grammarService;
	@Autowired private FunctionService functionService;
	
	public List<DatamodelReferenceContainer> exportDatamodels(AuthPojo auth) throws InstantiationException, IllegalAccessException {
		return this.exportDatamodels(auth, false);
	}
	
	public List<DatamodelReferenceContainer> exportDatamodels(AuthPojo auth, boolean includeDrafts) throws InstantiationException, IllegalAccessException {
		List<RightsContainer<Datamodel>> models = datamodelService.findAllByAuth(auth);
		if (models==null || models.isEmpty()) {
			return null;
		}
		
		List<DatamodelReferenceContainer> result = new ArrayList<DatamodelReferenceContainer>();
		
		ChangeSet ch;
		Datamodel exportModel;
		for (RightsContainer<Datamodel> m : models) {
			exportModel = new DatamodelImpl();
			exportModel.setId(m.getElement().getId());
			exportModel.setName(m.getElement().getName());
			
			ch = datamodelService.getLatestChangeSetForEntity(m.getElement().getId());
			if (ch!=null) {
				exportModel.setVersionId(ch.getId());
			}
			
			if (m.getElement().getNatures()!=null) {
				exportModel.setNatures(new ArrayList<DatamodelNature>());
				for (DatamodelNature n : m.getElement().getNatures()) {
					exportModel.getNatures().add(n.getClass().newInstance());
				}
			}
			result.add(new DatamodelReferenceContainer(exportModel));
		}
		return result;
	}
	
	public DatamodelReferenceContainer exportDatamodel(String entityId, AuthPojo auth) {
		RightsContainer<Datamodel> s = datamodelService.findByIdAndAuth(entityId, auth);
		if (s==null || s.getElement()==null) {
			return null;
		}
		
		DatamodelReferenceContainer sp = new DatamodelReferenceContainer();
		sp.setModel(s.getElement());
		
		ChangeSet ch = datamodelService.getLatestChangeSetForEntity(s.getId());
		if (ch!=null) {
			s.getElement().setVersionId(ch.getId());
		}
		s.flush();
		
		sp.setRoot(referenceService.findReferenceBySchemaId(entityId));
		sp.setElements(new HashMap<String, ModelElement>());
		
		for (Element e : elementService.findBySchemaId(entityId)) {
			e.flush();
			sp.getElements().put(e.getId(), e);
		}
		for (Grammar g : grammarService.findByEntityId(entityId, true)) {
			g.flush();
			sp.getElements().put(g.getId(), g);
		}
		for (Function f : functionService.findByEntityId(entityId)) {
			f.flush();
			sp.getElements().put(f.getId(), f);
		}
		return sp;
	}
	
}
