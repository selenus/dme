package eu.dariah.de.minfba.schereg.dao;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.Criteria;
import org.hibernate.criterion.Projections;
import org.junit.Assert;
import org.junit.Test;
import eu.dariah.de.minfba.core.metamodel.interfaces.Schema;
import eu.dariah.de.minfba.core.metamodel.xml.XmlNamespace;
import eu.dariah.de.minfba.core.metamodel.xml.XmlSchema;
import eu.dariah.de.minfba.schereg.dao.base.BasePersistenceTest;

public class SchemaDaoTest extends BasePersistenceTest {	
	
	@Test
	public void testSchemaPersistence() throws IOException, URISyntaxException {
		this.createSchemata(5);
		
		List<Schema> schemata = schemaDao.findAll();
		Assert.assertTrue(schemata.size()==5);
		
		XmlSchema s = (XmlSchema)schemata.get(2);
		for (XmlNamespace xns1 : s.getNamespaces()) {
			xns1.setPrefix("new prefix");
		}
		
		// Cascading save on namespaces
		schemaDao.saveOrUpdate(s);
		
		schemata = schemaDao.findAll();
		Assert.assertTrue(schemata.size()==5);
		
		s = (XmlSchema)schemata.get(2);
		for (XmlNamespace xns1 : s.getNamespaces()) {
			Assert.assertEquals(xns1.getPrefix(), "new prefix");
		}	    
		
		for (Schema sDel : schemata) {
			// Cascading delete on namespaces
			schemaDao.delete(sDel);
		}
		
		Assert.assertEquals(0, this.countNamespaces());    
	}
		
	private void createSchemata(int count) {
		XmlSchema s;
		for (int i=0; i<count; i++) {
			s = new XmlSchema();
			s.setExternalLabel("ext label of schema " + i);
			s.setLabel("label of schema " + i);
			s.setRecordPath("recordPath_" + i);
			s.setUuid(UUID.randomUUID().toString());
			s.setNamespaces(new ArrayList<XmlNamespace>());
			
			XmlNamespace xns;			
			for (int j=0; j<count; j++) {
				xns = new XmlNamespace();
				xns.setPrefix("ns_" + j);
				xns.setUrl("http://.../" + j);
				xns.setSchema(s);
				s.getNamespaces().add(xns);
			}
			
			schemaDao.saveOrUpdate(s);
		}		
	}
	
	private long countNamespaces() {
		Criteria crit = sessionFactory.getCurrentSession().createCriteria(XmlNamespace.class);
        crit.setProjection(Projections.rowCount());
		
        return ((Long)crit.list().get(0)).longValue();     
	}	
}
