package de.unibamberg.minf.dme.importer.model;

import java.util.ArrayList;
import java.util.Map;

import de.unibamberg.minf.dme.model.base.Function;
import de.unibamberg.minf.dme.model.base.Grammar;
import de.unibamberg.minf.dme.model.base.Label;
import de.unibamberg.minf.dme.model.base.Nonterminal;
import de.unibamberg.minf.dme.model.datamodel.DatamodelImpl;
import de.unibamberg.minf.dme.model.datamodel.LabelImpl;
import de.unibamberg.minf.dme.model.datamodel.NonterminalImpl;
import de.unibamberg.minf.dme.model.datamodel.base.Datamodel;
import de.unibamberg.minf.dme.model.datamodel.natures.XmlDatamodelNature;
import de.unibamberg.minf.dme.model.datamodel.natures.xml.XmlNamespace;
import de.unibamberg.minf.dme.model.datamodel.natures.xml.XmlTerminal;
import de.unibamberg.minf.dme.model.function.FunctionImpl;
import de.unibamberg.minf.dme.model.grammar.GrammarContainer;
import de.unibamberg.minf.dme.model.grammar.GrammarImpl;
import eu.dariah.de.minfba.core.metamodel.function.DescriptionGrammarImpl;
import eu.dariah.de.minfba.core.metamodel.function.TransformationFunctionImpl;
import eu.dariah.de.minfba.core.metamodel.interfaces.Schema;
import eu.dariah.de.minfba.core.metamodel.xml.XmlSchema;

public class LegacySchemaConverter {

	public static Nonterminal convertLegacyNonterminal (eu.dariah.de.minfba.core.metamodel.Nonterminal oldN, Map<String, String> nonterminalTerminalIdMap) {
		if (oldN==null) {
			return null;
		}
		Nonterminal n = new NonterminalImpl(oldN.getEntityId(), oldN.getId(), oldN.getName());
		n.setTransient(oldN.isTransient());
		n.setLocked(oldN.isLocked());
		
		if (nonterminalTerminalIdMap!=null) {
			nonterminalTerminalIdMap.put(oldN.getId(), oldN.getTerminalId());
		}
		
		if (oldN.getChildNonterminals()!=null) {
			n.setChildNonterminals(new ArrayList<Nonterminal>());
			for (eu.dariah.de.minfba.core.metamodel.Nonterminal oldChildN : oldN.getChildNonterminals()) {
				n.getChildNonterminals().add(convertLegacyNonterminal(oldChildN, nonterminalTerminalIdMap));
			}
		}
		
		if (oldN.getGrammars()!=null) {
			n.setGrammars(new ArrayList<Grammar>());
			for (DescriptionGrammarImpl oldG : oldN.getGrammars()) {
				n.getGrammars().add(convertLegacyGrammar(oldG));
			}
		}
		
		
		return n;
	}
	
	public static Grammar convertLegacyGrammar(DescriptionGrammarImpl oldG) {
		if (oldG==null) {
			return null;
		}
		Grammar g = new GrammarImpl(oldG.getEntityId(), oldG.getGrammarName());
		g.setId(oldG.getId());
		g.setBaseMethod(oldG.getBaseMethod());
		g.setError(oldG.isError());
		g.setLocked(oldG.isLocked());
		
		if (oldG.getGrammarContainer()!=null) {
			g.setGrammarContainer(convertLegacyGrammarContainer(oldG.getGrammarContainer()));
		}

		if (oldG.getTransformationFunctions()!=null) {
			g.setFunctions(new ArrayList<Function>());
			for (TransformationFunctionImpl oldF : oldG.getTransformationFunctions()) {
				g.getFunctions().add(convertLegacyFunction(oldF));
			}
		}
		return g;
	}
	
	public static GrammarContainer convertLegacyGrammarContainer(eu.dariah.de.minfba.core.metamodel.function.GrammarContainer oldG) {
		GrammarContainer g = new GrammarContainer();
		g.setId(oldG.getId());
		g.setLexerGrammar(oldG.getLexerGrammar());
		g.setParserGrammar(oldG.getParserGrammar());
		return g;
	}

	public static Function convertLegacyFunction(TransformationFunctionImpl oldF) {
		if (oldF==null) {
			return null;
		}
		Function f = new FunctionImpl(oldF.getEntityId(), oldF.getName());
		f.setId(oldF.getId());
		f.setError(oldF.isError());
		f.setFunction(oldF.getFunction());
		
		if (oldF.getOutputElements()!=null) {
			f.setOutputElements(new ArrayList<Label>());
			for (eu.dariah.de.minfba.core.metamodel.Label oldL : oldF.getOutputElements()) {
				f.getOutputElements().add(convertLegacyLabel(oldL));
			}
		}
		return f;
	}

	public static Label convertLegacyLabel(eu.dariah.de.minfba.core.metamodel.Label oldL) {
		if (oldL==null) {
			return null;
		}
		Label l = new LabelImpl(oldL.getEntityId(), oldL.getName());
		l.setId(oldL.getId());
		l.setLocked(oldL.isLocked());
		l.setTransient(oldL.isTransient());
		
		if (oldL.getGrammars()!=null) {
			l.setGrammars(new ArrayList<Grammar>());
			for (DescriptionGrammarImpl oldG : oldL.getGrammars()) {
				l.getGrammars().add(convertLegacyGrammar(oldG));
			}
		}
		if (oldL.getSubLabels()!=null) {
			l.setSubLabels(new ArrayList<Label>());
			for (eu.dariah.de.minfba.core.metamodel.Label oldChildL : oldL.getSubLabels()) {
				l.getSubLabels().add(convertLegacyLabel(oldChildL));
			}
		}
		return l;
	}

	public static Datamodel convertLegacySchema(Schema s) {
		if (s==null) {
			return null;
		}
		Datamodel m = new DatamodelImpl();
		m.setId(s.getId());
		m.setName(s.getLabel());
		m.setDescription(s.getDescription());

		// Legacy schemas were only implemented for XML...
		if (XmlSchema.class.isAssignableFrom(s.getClass()) && s.getTerminals()!=null) {
			XmlDatamodelNature xmlNature = new XmlDatamodelNature();
			m.addOrReplaceNature(xmlNature);
			
			XmlSchema oldXmlSchema = (XmlSchema)s;
			
			xmlNature.setRecordPath(oldXmlSchema.getRecordPath());
			xmlNature.setRootElementName(oldXmlSchema.getRootElementName());
			xmlNature.setRootElementNamespace(oldXmlSchema.getRootElementNamespace());
			
			if (oldXmlSchema.getNamespaces()!=null) {
				xmlNature.setNamespaces(new ArrayList<XmlNamespace>());
				for (eu.dariah.de.minfba.core.metamodel.xml.XmlNamespace oldXmlNs : oldXmlSchema.getNamespaces()) {
					xmlNature.getNamespaces().add(convertLegacyXmlNamespace(oldXmlNs));
				}
			}

			if (oldXmlSchema.getTerminals()!=null) {
				xmlNature.setTerminals(new ArrayList<XmlTerminal>());
				for (eu.dariah.de.minfba.core.metamodel.xml.XmlTerminal oldXmlTerminal : oldXmlSchema.getTerminals()) {
					xmlNature.getTerminals().add(convertLegacyXmlTerminal(oldXmlTerminal));
				}
			}
		}
		return m;
	}

	private static XmlTerminal convertLegacyXmlTerminal(eu.dariah.de.minfba.core.metamodel.xml.XmlTerminal oldXmlTerminal) {
		if (oldXmlTerminal==null) {
			return null;
		}
		XmlTerminal xmlTerminal = new XmlTerminal();
		xmlTerminal.setId(oldXmlTerminal.getId());
		xmlTerminal.setAttribute(oldXmlTerminal.isAttribute());
		xmlTerminal.setName(oldXmlTerminal.getName());
		xmlTerminal.setNamespace(oldXmlTerminal.getNamespace());
		return xmlTerminal;
	}

	private static XmlNamespace convertLegacyXmlNamespace(eu.dariah.de.minfba.core.metamodel.xml.XmlNamespace oldXmlNs) {
		if (oldXmlNs==null) {
			return null;
		}
		return new XmlNamespace(oldXmlNs.getPrefix(), oldXmlNs.getUrl());
	}
	
}
