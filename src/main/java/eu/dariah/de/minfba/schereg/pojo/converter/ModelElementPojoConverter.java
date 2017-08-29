package eu.dariah.de.minfba.schereg.pojo.converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.unibamberg.minf.dme.model.base.Element;
import de.unibamberg.minf.dme.model.base.Function;
import de.unibamberg.minf.dme.model.base.Grammar;
import de.unibamberg.minf.dme.model.base.Label;
import de.unibamberg.minf.dme.model.base.ModelElement;
import de.unibamberg.minf.dme.model.base.Nonterminal;
import eu.dariah.de.minfba.schereg.exception.GenericScheregException;
import eu.dariah.de.minfba.schereg.pojo.ModelElementPojo;
import eu.dariah.de.minfba.schereg.pojo.ModelElementPojo.ModelElementState;

public class ModelElementPojoConverter {

	public static List<ModelElementPojo> convertModelElements(List<Element> modelElements, boolean staticElementsOnly) throws GenericScheregException {
		return convertModelElements(modelElements, staticElementsOnly, new HashMap<Element, ModelElementPojo>());
	}
	
	public static List<ModelElementPojo> convertModelElements(List<Element> modelElements, boolean staticElementsOnly, Map<Element, ModelElementPojo> converted) throws GenericScheregException {
		List<ModelElementPojo> results = new ArrayList<ModelElementPojo>();
		ModelElementPojo mep;
		if (modelElements!=null) {
			for (Element e : modelElements) {
				mep = convertModelElement(e, staticElementsOnly, converted);
				if (mep!=null) {
					results.add(mep);
				}
			}
		}
		return results;
	}
	
	public static ModelElementPojo convertModelElement(ModelElement modelElement, boolean staticElementsOnly) throws GenericScheregException {
		return convertModelElement(modelElement, staticElementsOnly, new HashMap<Element, ModelElementPojo>());
	}
	
	public static ModelElementPojo convertModelElement(ModelElement modelElement, boolean staticElementsOnly, Map<Element, ModelElementPojo> converted) throws GenericScheregException {
		if (modelElement==null) {
			return null;
		} else if (Nonterminal.class.isAssignableFrom(modelElement.getClass())) {
			return convertNonterminal((Nonterminal)modelElement, staticElementsOnly, converted);
		} else if (Label.class.isAssignableFrom(modelElement.getClass())) {
			return convertLabel((Label)modelElement, staticElementsOnly);
		} else if (Grammar.class.isAssignableFrom(modelElement.getClass())) {
			if (staticElementsOnly) {
				return null;
			}
			return convertGrammar((Grammar)modelElement);
		} else if (Function.class.isAssignableFrom(modelElement.getClass())) {
			if (staticElementsOnly) {
				return null;
			}
			return convertFunction((Function)modelElement);
		}
		throw new GenericScheregException("Failed to convert model element; conversion not supported for " + modelElement.getClass().getName());
	}
	
	private static ModelElementPojo convertNonterminal(Nonterminal n, boolean staticElementsOnly, Map<Element, ModelElementPojo> converted) {
		if (converted.containsKey(n)) {
			//return converted.get(n);
			ModelElementPojo p = new ModelElementPojo();
			p.setState(ModelElementState.REUSE);
			p.setType("Nonterminal");
			p.setId(n.getId());
			p.setLabel(n.getName());
			return p;
		}
		
		ModelElementPojo p = new ModelElementPojo();
		p.setState(ModelElementState.OK);
		p.setType("Nonterminal");
		p.setProcessingRoot(n.isProcessingRoot());
		
		converted.put(n, p);
		
		if (n.getChildNonterminals()!=null && n.getChildNonterminals().size()>0) {
			p.setChildElements(new ArrayList<ModelElementPojo>());
			for (Nonterminal childN : n.getChildNonterminals()) {
				p.getChildElements().add(convertNonterminal(childN, staticElementsOnly, converted));
			}
		}
		
		return convertElement(p, n, staticElementsOnly); 
	}
	
	private static ModelElementPojo convertLabel(Label l, boolean staticElementsOnly) {
		ModelElementPojo p = new ModelElementPojo();
		p.setState(ModelElementState.OK);
		p.setType("Label");

		if (!staticElementsOnly && l.getSubLabels()!=null && l.getSubLabels().size()>0) {
			if (p.getChildElements()==null) {
				p.setChildElements(new ArrayList<ModelElementPojo>());
			}
			for (Label childL : l.getSubLabels()) {
				p.getChildElements().add(convertLabel(childL, staticElementsOnly));
			}
		}
		
		return convertElement(p, l, staticElementsOnly); 
	}
	
	private static ModelElementPojo convertElement(ModelElementPojo p, Element e, boolean staticElementsOnly) {
		p.setId(e.getId());
		p.setLabel(e.getName());
		p.setDisabled(e.isDisabled());
		
		List<Label> producedLabels = e.getProducedLabels();
		if (staticElementsOnly && producedLabels!=null && producedLabels.size()>0) {
			if (p.getChildElements()==null) {
				p.setChildElements(new ArrayList<ModelElementPojo>());
			}
			for (Label childL : producedLabels) {
				p.getChildElements().add(convertLabel(childL, staticElementsOnly));
			}
		}
		
		if (!staticElementsOnly && e.getGrammars()!=null && e.getGrammars().size()>0) {
			if (p.getChildElements()==null) {
				p.setChildElements(new ArrayList<ModelElementPojo>());
			}
			for (Grammar g : e.getGrammars()) {
				p.getChildElements().add(convertGrammar(g));
			}
		}
		return p;
	}
	 	
	private static ModelElementPojo convertGrammar(Grammar g) {
		ModelElementPojo p = new ModelElementPojo();
		p.setId(g.getId());
		p.setLabel(g.getName());
		p.setState(ModelElementState.OK);
		p.setType("Grammar");
		p.setDisabled(g.isDisabled());
		
		if (g.getFunctions()!=null && g.getFunctions().size()>0) {
			p.setChildElements(new ArrayList<ModelElementPojo>());
			for (Function f : g.getFunctions()) {
				p.getChildElements().add(convertFunction(f));
			}
		}
		
		return p;
	}
	
	private static ModelElementPojo convertFunction(Function f) {
		ModelElementPojo p = new ModelElementPojo();
		p.setId(f.getId());
		p.setLabel(f.getName());
		p.setState(ModelElementState.OK);
		p.setType("Function");
		p.setDisabled(f.isDisabled());
		
		if (f.getOutputElements()!=null && f.getOutputElements().size()>0) {
			p.setChildElements(new ArrayList<ModelElementPojo>());
			for (Label l : f.getOutputElements()) {
				p.getChildElements().add(convertLabel(l, false));
			}
		}
		
		return p;
	}

	
}
