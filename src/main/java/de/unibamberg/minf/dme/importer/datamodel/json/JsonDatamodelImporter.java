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

import de.unibamberg.minf.dme.model.base.Element;
import de.unibamberg.minf.dme.model.base.Function;
import de.unibamberg.minf.dme.model.base.Grammar;
import de.unibamberg.minf.dme.model.base.Label;
import de.unibamberg.minf.dme.model.base.ModelElement;
import de.unibamberg.minf.dme.model.base.Nonterminal;
import de.unibamberg.minf.dme.model.exception.MetamodelConsistencyException;
import de.unibamberg.minf.dme.model.serialization.DatamodelContainer;
import de.unibamberg.minf.dme.service.ElementServiceImpl;
import de.unibamberg.minf.dme.service.IdentifiableServiceImpl;

@Component
@Scope(value=ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class JsonDatamodelImporter extends BaseJsonDatamodelImporter {

	@Override public String getImporterSubtype() { return "Datamodel"; }
		
	@Override
	public boolean getIsSupported() {
		if (super.getIsSupported()) {
			try {
				objectMapper.readValue(new File(this.importFilePath), DatamodelContainer.class);
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
			DatamodelContainer s = objectMapper.readValue(new File(this.importFilePath), DatamodelContainer.class);			
			return IdentifiableServiceImpl.extractAllByTypes(s.getRoot(), allowedSubtreeRoots);
		} catch (Exception e) {
			logger.error("Attempting legacy schema deserialization", e);
			return null;
		}
	}
	
	@Override
	public List<Element> getPossibleRootElements() {
		try {
			DatamodelContainer s = objectMapper.readValue(new File(this.importFilePath), DatamodelContainer.class);
			
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
		DatamodelContainer s = objectMapper.readValue(new File(this.importFilePath), DatamodelContainer.class);
		s.getModel().setId(this.getDatamodel().getId());

		Map<String, ModelElement> elementReferences = new HashMap<String, ModelElement>(); 
		
		this.collectElementReferences(s.getRoot(), elementReferences);
		this.resolveReoccurringElements(s.getRoot(), elementReferences, new ArrayList<String>());;
		
		this.importModel(s.getModel(), (Nonterminal)s.getRoot(), s.getGrammars());
	}

	private void resolveReoccurringElements(ModelElement e, Map<String, ModelElement> elements, List<String> processedIds) {
		
		if (!processedIds.contains(e.getId())) {
			processedIds.add(e.getId());
		} 
		// Child elements are exported only with one main occurrence
		if (Nonterminal.class.isAssignableFrom(e.getClass())) {
			Nonterminal n = (Nonterminal)e;
			if ( (n.getChildNonterminals()!=null && !n.getChildNonterminals().isEmpty()) || 
					(n.getGrammars()!=null && !n.getGrammars().isEmpty() )) {
				elements.put(n.getId(), n);
				if (n.getChildNonterminals()!=null && !n.getChildNonterminals().isEmpty()) {
					for (int i=0; i<n.getChildNonterminals().size(); i++) {
						n.getChildNonterminals().set(i, (Nonterminal)elements.get(n.getChildNonterminals().get(i).getId()));
						if (!processedIds.contains(n.getChildNonterminals().get(i).getId())) {
							this.resolveReoccurringElements(n.getChildNonterminals().get(i), elements, processedIds);
						}					
					}
				}
				if (n.getGrammars()!=null && !n.getGrammars().isEmpty()) {
					for (int i=0; i<n.getGrammars().size(); i++) {
						n.getGrammars().set(i, (Grammar)elements.get(n.getGrammars().get(i).getId()));
						if (!processedIds.contains(n.getGrammars().get(i).getId())) {
							this.resolveReoccurringElements(n.getGrammars().get(i), elements, processedIds);
						}					
					}
				}
			}
		} else if (Label.class.isAssignableFrom(e.getClass())) {
			Label l = (Label)e;
			if ( (l.getSubLabels()!=null && !l.getSubLabels().isEmpty()) ||
					(l.getGrammars()!=null && !l.getGrammars().isEmpty())) {
				elements.put(l.getId(), l);				
				if (l.getSubLabels()!=null && !l.getSubLabels().isEmpty()) {
					for (int i=0; i<l.getSubLabels().size(); i++) {
						l.getSubLabels().set(i, (Label)elements.get(l.getSubLabels().get(i).getId()));
						if (!processedIds.contains(l.getSubLabels().get(i).getId())) {
							this.resolveReoccurringElements(l.getSubLabels().get(i), elements, processedIds);
						}					
					}
				}
				if (l.getGrammars()!=null && !l.getGrammars().isEmpty()) {
					for (int i=0; i<l.getGrammars().size(); i++) {
						l.getGrammars().set(i, (Grammar)elements.get(l.getGrammars().get(i).getId()));
						if (!processedIds.contains(l.getGrammars().get(i).getId())) {
							this.resolveReoccurringElements(l.getGrammars().get(i), elements, processedIds);
						}					
					}
				}
			}				
		} else if (Grammar.class.isAssignableFrom(e.getClass())) {
			Grammar g = (Grammar)e;
			if (g.getFunctions()!=null && !g.getFunctions().isEmpty()) {
				elements.put(g.getId(), g);
				for (int i=0; i<g.getFunctions().size(); i++) {
					g.getFunctions().set(i, (Function)elements.get(g.getFunctions().get(i).getId()));
					if (!processedIds.contains(g.getFunctions().get(i).getId())) {
						this.resolveReoccurringElements(g.getFunctions().get(i), elements, processedIds);
					}					
				}
			}
		} else if (Function.class.isAssignableFrom(e.getClass())) {
			Function f = (Function)e;
			if (f.getOutputElements()!=null && !f.getOutputElements().isEmpty()) {
				elements.put(f.getId(), f);
				for (int i=0; i<f.getOutputElements().size(); i++) {
					f.getOutputElements().set(i, (Label)elements.get(f.getOutputElements().get(i).getId()));
					if (!processedIds.contains(f.getOutputElements().get(i).getId())) {
						this.resolveReoccurringElements(f.getOutputElements().get(i), elements, processedIds);
					}					
				}
			}
		}
		
		
	}
	
	private void collectElementReferences(ModelElement e, Map<String, ModelElement> elements) {
		
		if (!elements.containsKey(e.getId())) {
			elements.put(e.getId(), e);
		} 
		// Child elements are exported only with one main occurrence
		if (Nonterminal.class.isAssignableFrom(e.getClass())) {
			Nonterminal n = (Nonterminal)e;
			if ( (n.getChildNonterminals()!=null && !n.getChildNonterminals().isEmpty()) || 
					(n.getGrammars()!=null && !n.getGrammars().isEmpty() )) {
				elements.put(n.getId(), n);
				if (n.getChildNonterminals()!=null && !n.getChildNonterminals().isEmpty()) {
					for (Nonterminal nChild : n.getChildNonterminals()) {
						this.collectElementReferences(nChild, elements);
					}
				}
				if (n.getGrammars()!=null && !n.getGrammars().isEmpty()) {
					for (Grammar gChild : n.getGrammars()) {
						this.collectElementReferences(gChild, elements);
					}
				}
				elements.put(n.getId(), n);
			}
		} else if (Label.class.isAssignableFrom(e.getClass())) {
			Label l = (Label)e;
			if ( (l.getSubLabels()!=null && !l.getSubLabels().isEmpty()) ||
					(l.getGrammars()!=null && !l.getGrammars().isEmpty())) {
				elements.put(l.getId(), l);
				if (l.getSubLabels()!=null && !l.getSubLabels().isEmpty()) {
					for (Label lChild : l.getSubLabels()) {
						this.collectElementReferences(lChild, elements);
					}
				}
				if (l.getGrammars()!=null && !l.getGrammars().isEmpty()) {
					for (Grammar gChild : l.getGrammars()) {
						this.collectElementReferences(gChild, elements);
					}
				}
				elements.put(l.getId(), l);
			}				
		} else if (Grammar.class.isAssignableFrom(e.getClass())) {
			Grammar g = (Grammar)e;
			if (g.getFunctions()!=null && !g.getFunctions().isEmpty()) {
				elements.put(g.getId(), g);
				for (Function fChild : g.getFunctions()) {
					this.collectElementReferences(fChild, elements);
				}
				elements.put(g.getId(), g);
			}
		} else if (Function.class.isAssignableFrom(e.getClass())) {
			Function f = (Function)e;
			if (f.getOutputElements()!=null && !f.getOutputElements().isEmpty()) {
				elements.put(f.getId(), f);
				for (Label lChild : f.getOutputElements()) {
					this.collectElementReferences(lChild, elements);
				}
				elements.put(f.getId(), f);
			}
		}
		
	}
	
}
