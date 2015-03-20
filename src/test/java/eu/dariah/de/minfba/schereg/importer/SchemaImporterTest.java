package eu.dariah.de.minfba.schereg.importer;


import org.apache.xerces.impl.xs.XSImplementationImpl;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSImplementation;
import org.apache.xerces.xs.XSLoader;
import org.apache.xerces.xs.XSModel;
import org.apache.xerces.xs.XSNamedMap;
import org.apache.xerces.xs.XSNamespaceItem;
import org.apache.xerces.xs.XSNamespaceItemList;
import org.apache.xerces.xs.XSObject;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.dariah.de.minfba.core.metamodel.xml.XmlSchema;

public class SchemaImporterTest {
	private static final Logger logger = LoggerFactory.getLogger(SchemaImporterTest.class);
	
	private static final String XML_SCHEMA_NAMESPACE = "http://www.w3.org/2001/XMLSchema";
	
	String oaiDcSchemaPath = this.getClass().getClassLoader().getResource("xsd/oai_dc.xsd").getPath();
	
	@Test
	public void testXmlSchemaImport() {		
		XSImplementation impl = new XSImplementationImpl();		
		XSLoader schemaLoader = impl.createXSLoader(null);
		XSModel model = schemaLoader.loadURI(oaiDcSchemaPath);
		
		XmlSchema s = new XmlSchema();
		s.setLabel("OAI-DC TestSchema");
				
		XSNamespaceItemList nil = model.getNamespaceItems();
		XSNamespaceItem ns;
		for (int i=0; i<nil.getLength(); i++) {
			ns = nil.item(i);			
			logger.info(ns.getSchemaNamespace());
		}

		this.processComponents(model.getComponents(XSConstants.TYPE_DEFINITION));
		this.processComponents(model.getComponents(XSConstants.ELEMENT_DECLARATION));
		
		Assert.assertNotNull(model);		
	}
	
	private void processComponents(XSNamedMap map) {
		for (int j=0; j<map.getLength(); j++) {
			XSObject o = map.item(j);
			if (o.getNamespace().equals(XML_SCHEMA_NAMESPACE)) {
				continue;
			}			
			System.out.println("{"+o.getNamespace()+"}"+o.getName() + " -> " + o.getType() + " => " + o.getClass().getSimpleName());
		}
	}
	

}
