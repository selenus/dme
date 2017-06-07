package eu.dariah.de.minfba.schereg.dao;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import eu.dariah.de.minfba.core.metamodel.interfaces.SchemaNature;
import eu.dariah.de.minfba.core.metamodel.xml.XmlNamespace;
import eu.dariah.de.minfba.core.metamodel.xml.XmlSchemaNature;
import eu.dariah.de.minfba.schereg.dao.interfaces.SchemaDao;

//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations = {"classpath:orm-test-context.xml"})
public class SchemaDaoTest {
	
	@Autowired private SchemaDao schemaDao;
	
	//@Test
	public void testCreateSchema() {
		XmlSchemaNature s = new XmlSchemaNature();
		s.setExternalLabel("lblExt");
		s.setLabel("lbl");
		s.setNamespaces(new ArrayList<XmlNamespace>());
		
		XmlNamespace xs = new XmlNamespace();
		xs.setPrefix("prefix_1");
		xs.setUrl("url_1");
		s.getNamespaces().add(xs);
		
		xs = new XmlNamespace();
		xs.setPrefix("prefix_2");
		xs.setUrl("url_2");
		s.getNamespaces().add(xs);
		
		/*s = schemaDao.save(s);
		
		List<Schema> schemas = schemaDao.findAll();
		Assert.assertTrue(schemas.size()>0);
		
		XmlNamespace ns = schemaDao.findNamespaceByPrefix("prefix_1");
		Assert.assertTrue(ns!=null);
		
		schemaDao.updateNamespaceByPrefix(s, "prefix_1", "url_3");
		
		
		Schema schema = schemaDao.findById(s.getId());
		Assert.assertTrue(schemas.size()>0);*/
	}
}
