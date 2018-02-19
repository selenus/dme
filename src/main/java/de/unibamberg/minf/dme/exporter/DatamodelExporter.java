package de.unibamberg.minf.dme.exporter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.unibamberg.minf.dme.model.RightsContainer;
import de.unibamberg.minf.dme.model.base.Element;
import de.unibamberg.minf.dme.model.base.Function;
import de.unibamberg.minf.dme.model.base.Grammar;
import de.unibamberg.minf.dme.model.base.Identifiable;
import de.unibamberg.minf.dme.model.base.Label;
import de.unibamberg.minf.dme.model.base.ModelElement;
import de.unibamberg.minf.dme.model.base.Nonterminal;
import de.unibamberg.minf.dme.model.datamodel.DatamodelImpl;
import de.unibamberg.minf.dme.model.datamodel.NonterminalImpl;
import de.unibamberg.minf.dme.model.datamodel.base.Datamodel;
import de.unibamberg.minf.dme.model.datamodel.base.DatamodelNature;
import de.unibamberg.minf.dme.model.function.FunctionImpl;
import de.unibamberg.minf.dme.model.grammar.GrammarImpl;
import de.unibamberg.minf.dme.model.reference.Reference;
import de.unibamberg.minf.dme.model.serialization.DatamodelContainer;
import de.unibamberg.minf.dme.model.serialization.DatamodelReferenceContainer;
import de.unibamberg.minf.dme.model.tracking.ChangeSet;
import de.unibamberg.minf.dme.service.IdentifiableServiceImpl;
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
			result.add(new DatamodelReferenceContainer((DatamodelImpl)exportModel));
		}
		return result;
	}
	
	public DatamodelReferenceContainer exportDatamodelSubtree(String entityId, String exportElementId, AuthPojo auth) {
		RightsContainer<Datamodel> s = datamodelService.findByIdAndAuth(entityId, auth);
		if (s==null || s.getElement()==null) {
			return null;
		}
		
		DatamodelReferenceContainer sp = new DatamodelReferenceContainer();
			
		ChangeSet ch = datamodelService.getLatestChangeSetForEntity(s.getId());
		if (ch!=null) {
			s.getElement().setVersionId(ch.getId());
		}
		s.flush();
		

		Identifiable rootE = elementService.getElementSubtree(entityId, exportElementId);
		Element expE;
		if (Element.class.isAssignableFrom(rootE.getClass())) {
			expE = (Element)rootE;
		} else {
			expE = new NonterminalImpl(s.getId(), "EXPORT_CONTAINER");
			expE.setGrammars(new ArrayList<Grammar>());
			
			GrammarImpl expG;
			if (GrammarImpl.class.isAssignableFrom(rootE.getClass())) {
				expG = (GrammarImpl)rootE;
			} else {
				expG = new GrammarImpl(entityId, "EXPORT_CONTAINER");
				expG.setFunctions(new ArrayList<Function>());
				
				FunctionImpl expF;
				if (FunctionImpl.class.isAssignableFrom(rootE.getClass())) {
					expF = (FunctionImpl)rootE;
				} else {
					return null;
				}
				expG.getFunctions().add(expF);
			}
			expE.getGrammars().add(expG);
		}

		// Clone relevant parts of DatamodelNatures
		sp.setModel(datamodelService.cloneSchemaForSubtree(s.getElement(), expE));
			
		Reference rootR = new Reference(entityId);
		Reference parentReference = referenceService.findReferenceBySchemaAndChildId(entityId, exportElementId);
		for (String className : parentReference.getChildReferences().keySet()) {
			boolean found = false;
			for (Reference rChild : parentReference.getChildReferences().get(className)) {
				if (rChild.getId().equals(exportElementId)) {
					rootR.setChildReferences(new HashMap<String, Reference[]>());
					rootR.getChildReferences().put(className, new Reference[] {rChild});
					found = true;
					break;
				}
			}
			if (found) {
				break;
			}
		}		
		sp.setRoot(rootR);
		
		Map<String, ModelElement> elements = new HashMap<String, ModelElement>();

		this.flattenElements(elements, expE);
		sp.setElements(elements);
		
		return sp;		
	}
	
	private void flattenElements(Map<String, ModelElement> flattenedMap, ModelElement e) {
		flattenedMap.put(e.getId(), e);
		
		if (Nonterminal.class.isAssignableFrom(e.getClass())) {
			this.flattenElements(flattenedMap, ((Nonterminal)e).getChildNonterminals());
			this.flattenElements(flattenedMap, ((Nonterminal)e).getGrammars());
		} else if (Label.class.isAssignableFrom(e.getClass())) {
			this.flattenElements(flattenedMap, ((Label)e).getSubLabels());
			this.flattenElements(flattenedMap, ((Label)e).getGrammars());
		} else if (Grammar.class.isAssignableFrom(e.getClass())) {
			this.flattenElements(flattenedMap, ((Grammar)e).getFunctions());
		} else if (Function.class.isAssignableFrom(e.getClass())) {
			this.flattenElements(flattenedMap, ((Function)e).getOutputElements());
		}
	}
	
	private void flattenElements(Map<String, ModelElement> flattenedMap, List<? extends ModelElement> elements) {
		if (elements==null || elements.size()==0) {
			return;
		}
		for (ModelElement e : elements) {
			this.flattenElements(flattenedMap, e);
		}
	}
	
	
	public DatamodelReferenceContainer exportDatamodel(String entityId, AuthPojo auth) {
		RightsContainer<Datamodel> s = datamodelService.findByIdAndAuth(entityId, auth);
		if (s==null || s.getElement()==null) {
			return null;
		}
		
		DatamodelReferenceContainer sp = new DatamodelReferenceContainer();
		sp.setModel((DatamodelImpl)s.getElement());
		
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
