package eu.dariah.de.minfba.schereg.dao;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.unibamberg.minf.dme.model.datamodel.DatamodelImpl;
import de.unibamberg.minf.dme.model.datamodel.base.Datamodel;
import de.unibamberg.minf.dme.model.datamodel.base.DatamodelNature;
import de.unibamberg.minf.dme.model.datamodel.natures.XmlDatamodelNature;
import de.unibamberg.minf.dme.model.datamodel.natures.xml.XmlNamespace;
import eu.dariah.de.minfba.schereg.dao.interfaces.SchemaDao;

//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations = {"classpath:orm-test-context.xml"})
public class SchemaDaoTest {
	
	@Autowired private SchemaDao schemaDao;
	
	//@Test
	public void testCreateSchema() {
		Datamodel s = new DatamodelImpl();
		
		XmlDatamodelNature xmlN = new XmlDatamodelNature();
		
		s.addOrReplaceNature(xmlN);
		
		xmlN.setExternalLabel("lblExt");
		s.setName("lbl");
		xmlN.setNamespaces(new ArrayList<XmlNamespace>());
		
		XmlNamespace xs = new XmlNamespace();
		xs.setPrefix("prefix_1");
		xs.setUrl("url_1");
		xmlN.getNamespaces().add(xs);
		
		xs = new XmlNamespace();
		xs.setPrefix("prefix_2");
		xs.setUrl("url_2");
		xmlN.getNamespaces().add(xs);
		
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
