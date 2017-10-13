package de.unibamberg.minf.dme.pojo.converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.unibamberg.minf.dme.exception.GenericScheregException;
import de.unibamberg.minf.dme.model.base.Element;
import de.unibamberg.minf.dme.model.base.Function;
import de.unibamberg.minf.dme.model.base.Grammar;
import de.unibamberg.minf.dme.model.base.Label;
import de.unibamberg.minf.dme.model.base.ModelElement;
import de.unibamberg.minf.dme.model.base.Nonterminal;
import de.unibamberg.minf.dme.model.base.Terminal;
import de.unibamberg.minf.dme.model.datamodel.base.DatamodelNature;
import de.unibamberg.minf.dme.model.datamodel.natures.XmlDatamodelNature;
import de.unibamberg.minf.dme.model.datamodel.natures.xml.XmlNamespace;
import de.unibamberg.minf.dme.model.datamodel.natures.xml.XmlTerminal;
import de.unibamberg.minf.dme.pojo.ModelElementPojo;
import de.unibamberg.minf.dme.pojo.ModelElementPojo.ModelElementState;

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
	
	public static ModelElementPojo convertModelElementTerminal(Element modelElement, DatamodelNature nature) throws GenericScheregException {
		return convertModelElementTerminal(modelElement, nature, new HashMap<Nonterminal, ModelElementPojo>());
	}
	
	
	public static ModelElementPojo convertModelElementTerminal(ModelElement modelElement, DatamodelNature nature, Map<Nonterminal, ModelElementPojo> converted) throws GenericScheregException {
		if (modelElement!=null && Nonterminal.class.isAssignableFrom(modelElement.getClass())) {
			Nonterminal n = (Nonterminal)modelElement;
			return convertTerminal(n, nature, converted);
		}
		return null;
	}
	
	private static ModelElementPojo convertTerminal(Nonterminal n, DatamodelNature nature, Map<Nonterminal, ModelElementPojo> converted) {
		if (converted.containsKey(n)) {
			ModelElementPojo pExist = converted.get(n);
			pExist.setState(ModelElementState.REUSED);
			
			ModelElementPojo p = new ModelElementPojo();
			p.setState(ModelElementState.REUSING);
			p.setType(pExist.getType());
			p.setId(pExist.getId());
			p.setLabel(pExist.getLabel());
			return p;
		}
		
		Map<String, String> namespacePrefixMap = null;
		if (XmlDatamodelNature.class.isAssignableFrom(nature.getClass()) && ((XmlDatamodelNature)nature).getNamespaces()!=null) {
			namespacePrefixMap = new HashMap<String, String>();
			for (XmlNamespace xmlNs : ((XmlDatamodelNature)nature).getNamespaces()) {
				namespacePrefixMap.put(xmlNs.getUrl(), xmlNs.getPrefix());
			}
		}
		
		Terminal t = nature.getTerminalByNonterminalId(n.getId());
		ModelElementPojo p = new ModelElementPojo();
		if (t!=null) {
			p.setState(ModelElementState.OK);
			p.setId(t.getId());
			p.setInfo(new String[] { nature.getClass().getSimpleName(), nature.getClass().getName() });
			if (namespacePrefixMap!=null) {
				XmlTerminal xmlT = (XmlTerminal)t;
				String prefix = null;
				if (xmlT.getNamespace()!=null && namespacePrefixMap.containsKey(xmlT.getNamespace())) {
					prefix = namespacePrefixMap.get(xmlT.getNamespace());
				}
				if (prefix==null || prefix.isEmpty()) {
					p.setLabel(t.getName());
				} else {
					p.setLabel(prefix + ":" + t.getName());
				}
				
			} else {
				p.setLabel(t.getName());
			}
			p.setType("Terminal");
		} else {
			p.setState(ModelElementState.ERROR);
			p.setType("Terminal/Missing");
			p.setLabel(n.getName());
			p.setId(n.getId());
		}
		p.setProcessingRoot(n.isProcessingRoot());
		p.setDisabled(n.isDisabled());
		converted.put(n, p);
		
		if (n.getChildNonterminals()!=null && n.getChildNonterminals().size()>0) {
			p.setChildElements(new ArrayList<ModelElementPojo>());
			for (Nonterminal childN : n.getChildNonterminals()) {
				p.getChildElements().add(convertTerminal(childN, nature, converted));
			}
		}
		
		return p;
	}
	
	private static ModelElementPojo convertNonterminal(Nonterminal n, boolean staticElementsOnly, Map<Element, ModelElementPojo> converted) {
		if (converted.containsKey(n)) {
			converted.get(n).setState(ModelElementState.REUSED);
			
			ModelElementPojo p = new ModelElementPojo();
			p.setState(ModelElementState.REUSING);
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
