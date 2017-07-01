package eu.dariah.de.minfba.schereg.pojo.converter;

import java.util.ArrayList;
import java.util.List;

import eu.dariah.de.minfba.core.metamodel.BaseModelElement;
import eu.dariah.de.minfba.core.metamodel.function.interfaces.DescriptionGrammar;
import eu.dariah.de.minfba.core.metamodel.function.interfaces.TransformationFunction;
import eu.dariah.de.minfba.core.metamodel.interfaces.Element;
import eu.dariah.de.minfba.core.metamodel.interfaces.Label;
import eu.dariah.de.minfba.core.metamodel.interfaces.Nonterminal;
import eu.dariah.de.minfba.schereg.exception.GenericScheregException;
import eu.dariah.de.minfba.schereg.pojo.ModelElementPojo;
import eu.dariah.de.minfba.schereg.pojo.ModelElementPojo.ModelElementState;

public class ModelElementPojoConverter {

	public static List<ModelElementPojo> convertModelElements(List<Element> modelElements, boolean staticElementsOnly) throws GenericScheregException {
		List<ModelElementPojo> results = new ArrayList<ModelElementPojo>();
		if (modelElements!=null) {
			for (Element e : modelElements) {
				results.add(convertModelElement(e, staticElementsOnly));
			}
		}
		return results;
	}
	
	public static ModelElementPojo convertModelElement(BaseModelElement modelElement, boolean staticElementsOnly) throws GenericScheregException {
		if (modelElement==null) {
			return null;
		} else if (Nonterminal.class.isAssignableFrom(modelElement.getClass())) {
			return convertNonterminal((Nonterminal)modelElement, staticElementsOnly);
		} else if (Label.class.isAssignableFrom(modelElement.getClass())) {
			return convertLabel((Label)modelElement, staticElementsOnly);
		} else if (DescriptionGrammar.class.isAssignableFrom(modelElement.getClass())) {
			if (staticElementsOnly) {
				return null;
			}
			return convertGrammar((DescriptionGrammar)modelElement);
		} else if (TransformationFunction.class.isAssignableFrom(modelElement.getClass())) {
			if (staticElementsOnly) {
				return null;
			}
			return convertFunction((TransformationFunction)modelElement);
		}
		throw new GenericScheregException("Failed to convert model element; conversion not supported for " + modelElement.getClass().getName());
	}
	
	private static ModelElementPojo convertNonterminal(Nonterminal n, boolean staticElementsOnly) {
		ModelElementPojo p = new ModelElementPojo();
		p.setState(ModelElementState.OK);
		p.setType("Nonterminal");
		p.setProcessingRoot(n.isProcessingRoot());
		
		if (n.getChildNonterminals()!=null && n.getChildNonterminals().size()>0) {
			p.setChildElements(new ArrayList<ModelElementPojo>());
			for (Nonterminal childN : n.getChildNonterminals()) {
				p.getChildElements().add(convertNonterminal(childN, staticElementsOnly));
			}
		}
		
		List<Label> producedLabels = n.getProducedLabels();
		if (staticElementsOnly && producedLabels!=null && producedLabels.size()>0) {
			if (p.getChildElements()==null) {
				p.setChildElements(new ArrayList<ModelElementPojo>());
			}
			for (Label childL : producedLabels) {
				p.getChildElements().add(convertLabel(childL, staticElementsOnly));
			}
		}
		
		return convertElement(p, n, staticElementsOnly); 
	}
	
	private static ModelElementPojo convertLabel(Label l, boolean staticElementsOnly) {
		ModelElementPojo p = new ModelElementPojo();
		p.setState(ModelElementState.OK);
		p.setType("Label");
		
		if (l.getSubLabels()!=null && l.getSubLabels().size()>0) {
			p.setChildElements(new ArrayList<ModelElementPojo>());
			for (Label childL : l.getSubLabels()) {
				p.getChildElements().add(convertLabel(childL, staticElementsOnly));
			}
		}
		return convertElement(p, l, staticElementsOnly); 
	}
	
	private static ModelElementPojo convertElement(ModelElementPojo p, Element e, boolean staticElementsOnly) {
		p.setId(e.getId());
		p.setLabel(e.getName());
				
		if (!staticElementsOnly && e.getGrammars()!=null && e.getGrammars().size()>0) {
			if (p.getChildElements()==null) {
				p.setChildElements(new ArrayList<ModelElementPojo>());
			}
			for (DescriptionGrammar g : e.getGrammars()) {
				p.getChildElements().add(convertGrammar(g));
			}
		}
		return p;
	}
	
	private static ModelElementPojo convertGrammar(DescriptionGrammar g) {
		ModelElementPojo p = new ModelElementPojo();
		p.setId(g.getId());
		p.setLabel(g.getGrammarName());
		p.setState(ModelElementState.OK);
		p.setType("Grammar");
		
		if (g.getTransformationFunctions()!=null && g.getTransformationFunctions().size()>0) {
			p.setChildElements(new ArrayList<ModelElementPojo>());
			for (TransformationFunction f : g.getTransformationFunctions()) {
				p.getChildElements().add(convertFunction(f));
			}
		}
		
		return p;
	}
	
	private static ModelElementPojo convertFunction(TransformationFunction f) {
		ModelElementPojo p = new ModelElementPojo();
		p.setId(f.getId());
		p.setLabel(f.getName());
		p.setState(ModelElementState.OK);
		p.setType("Function");
		
		if (f.getOutputElements()!=null && f.getOutputElements().size()>0) {
			p.setChildElements(new ArrayList<ModelElementPojo>());
			for (Label l : f.getOutputElements()) {
				p.getChildElements().add(convertLabel(l, false));
			}
		}
		
		return p;
	}

	
}
