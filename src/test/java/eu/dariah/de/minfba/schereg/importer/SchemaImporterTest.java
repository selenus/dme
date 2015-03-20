package eu.dariah.de.minfba.schereg.importer;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;

import org.apache.xerces.impl.xs.XSElementDecl;
import org.apache.xerces.impl.xs.XSImplementationImpl;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSImplementation;
import org.apache.xerces.xs.XSLoader;
import org.apache.xerces.xs.XSModel;
import org.apache.xerces.xs.XSNamedMap;
import org.apache.xerces.xs.XSNamespaceItem;
import org.apache.xerces.xs.XSNamespaceItemList;
import org.apache.xerces.xs.XSObject;

import eu.dariah.de.minfba.core.metamodel.Nonterminal;
import eu.dariah.de.minfba.core.metamodel.xml.XmlSchema;
import eu.dariah.de.minfba.core.metamodel.xml.XmlTerminal;

public class SchemaImporterTest {
	private static final Logger logger = LoggerFactory.getLogger(SchemaImporterTest.class);
	private static final String XML_SCHEMA_NAMESPACE = "http://www.w3.org/2001/XMLSchema";
		
	private String oaiDcSchemaPath = this.getClass().getClassLoader().getResource("xsd/oai_dc.xsd").getPath();
	private String rootElementNs = "http://www.openarchives.org/OAI/2.0/oai_dc/";
	private String rootElementName = "dc";
	
	private Map<String, XmlTerminal> existingTerminalQNs = new HashMap<String, XmlTerminal>();
	private XSModel model;
	private XmlSchema s;
	
	@Test
	public void testXmlSchemaImport() throws ClassNotFoundException, InstantiationException, IllegalAccessException, ClassCastException {
		/* Why would this be suggested? http://stackoverflow.com/questions/3996857/java-api-to-parse-xsd-schema-file*/
		/*System.setProperty(DOMImplementationRegistry.PROPERTY, "org.apache.xerces.dom.DOMXSImplementationSourceImpl");
		DOMImplementationRegistry registry = DOMImplementationRegistry.newInstance(); 
		XSImplementation impl = (XSImplementation) registry.getDOMImplementation("XS-Loader");*/

		XSImplementation impl = new XSImplementationImpl();
		XSLoader schemaLoader = impl.createXSLoader(null);
		model = schemaLoader.loadURI(oaiDcSchemaPath);
		
		s = new XmlSchema();
		s.setLabel("OAI-DC TestSchema");

		XSNamespaceItemList nil = model.getNamespaceItems();
		XSNamespaceItem ns;
		for (int i=0; i<nil.getLength(); i++) {
			ns = nil.item(i);			
			logger.info(ns.getSchemaNamespace());
		}
		
		/** Collect it all */
		this.processTypes();
				
		/** What is our root though? */
		XSElementDecl rootElement = this.getRootElementDeclaration(rootElementNs, rootElementName);
		Nonterminal root = createNonterminal(rootElement.getNamespace(), rootElement.getName(), false);
		
		Assert.assertNotNull(root);		
	}
	
	
	
	private Nonterminal createNonterminal(String terminalNamespace, String terminalName, boolean isAttribute) {
		String terminalQN = String.format("{%s}:%s%s", terminalNamespace, (isAttribute ? "#" : ""), terminalName);
		
		String terminalId = null;
		if (existingTerminalQNs.containsKey(terminalQN)) {
			terminalId = existingTerminalQNs.get(terminalQN).getId();
		} else {
			XmlTerminal t = new XmlTerminal();
			t.setNamespacePrefix(terminalNamespace);
			t.setId(new ObjectId().toString());
			t.setName(terminalName);
			t.setAttribute(false);
			
			terminalId = t.getId();
			existingTerminalQNs.put(terminalQN, t);
		}
		
		Nonterminal n = new Nonterminal();
		n.setId(new ObjectId().toString());
		n.setName(this.createNonterminalName(terminalName));
		n.setTerminalId(terminalId);
		return n;
	}
	
	private String createNonterminalName(String terminalName) {
		return terminalName.substring(0, 1).toUpperCase() + terminalName.substring(1); 
	}
	
	private XSElementDecl getRootElementDeclaration(String ns, String name) {
		XSNamedMap map = this.model.getComponents(XSConstants.ELEMENT_DECLARATION);
		for (int j=0; j<map.getLength(); j++) {
			XSObject o = map.item(j);
			if (o.getNamespace().equals(ns) && o.getName().equals(name)) {
				logger.info("Identified root element declaration [{}]{}", ns, name);
				return (XSElementDecl)o;
			}
		}
		logger.warn("Unknown element declaration [{}]{}", ns, name);
		return null;
	}

	private void processTypes() {
		XSNamedMap map = this.model.getComponents(XSConstants.TYPE_DEFINITION);
		for (int j=0; j<map.getLength(); j++) {
			XSObject o = map.item(j);
			if (o.getNamespace().equals(XML_SCHEMA_NAMESPACE)) {
				continue;
			}			
			System.out.println("{"+o.getNamespace()+"}"+o.getName() + " -> " + o.getType() + " => " + o.getClass().getSimpleName());
		}
	}

	

}
