package eu.dariah.de.minfba.schereg.importer;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.dariah.de.minfba.core.metamodel.Nonterminal;

public class SchemaImporterTest {
	private static final Logger logger = LoggerFactory.getLogger(SchemaImporterTest.class);

	private String oaiDcSchemaPath = this.getClass().getClassLoader().getResource("xsd/oai_dc.xsd").getPath();
	private String rootElementNs = "http://www.openarchives.org/OAI/2.0/oai_dc/";
	private String rootElementName = "dc";
	
	
	@Test
	public void testXmlSchemaImport() throws ClassNotFoundException, InstantiationException, IllegalAccessException, ClassCastException {
		SchemaImporter si = new XmlSchemaImporter();
		si.setSchemaFilePath(oaiDcSchemaPath);
		si.setRootElementNs(rootElementNs);
		si.setRootElementName(rootElementName);
		si.run();
		
		Nonterminal root = si.getRootNonterminal();
		
		Assert.assertNotNull(root);		
	}
}