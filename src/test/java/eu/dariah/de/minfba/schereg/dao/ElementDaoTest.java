package eu.dariah.de.minfba.schereg.dao;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.UUID;

import org.apache.commons.lang3.mutable.MutableInt;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import eu.dariah.de.minfba.core.metamodel.Nonterminal;
import eu.dariah.de.minfba.core.metamodel.xml.XmlNamespace;
import eu.dariah.de.minfba.core.metamodel.xml.XmlSchema;
import eu.dariah.de.minfba.core.metamodel.xml.XmlTerminal;
import eu.dariah.de.minfba.schereg.dao.base.BasePersistenceTest;
import eu.dariah.de.minfba.schereg.service.ElementService;

public class ElementDaoTest extends BasePersistenceTest {	
	
	@Autowired private ElementDao elementDao;
	
	@Autowired private ElementService elementService;
	
	@Test
	public void testElementPersistence() throws IOException, URISyntaxException {
		long sId = this.saveElement();
		
		Nonterminal n = elementService.getElementHierarchy(sId);
		
		Assert.assertNotNull(n);
		
	}
	
	

	
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	private long saveElement() {
		XmlSchema s = this.createSchema();
		
		Nonterminal r = new Nonterminal();
		r.setName("ROOT");
		r.setSchema(s);
		r.setChildNonterminals(new ArrayList<Nonterminal>());
		r.getChildNonterminals().add(createHierarchy(r, new MutableInt(5)));
		
		elementDao.saveOrUpdate(r);
		
		return s.getId();
	}
	
	private Nonterminal createHierarchy(Nonterminal parent, MutableInt depth) {
		XmlTerminal t = new XmlTerminal();
		t.setName("name");
		t.setNamespacePrefix("ns");
		t.setSchema(parent.getSchema());
		
		Nonterminal n = new Nonterminal();
		n.setTerminal(t);
		n.setName("name");
		n.setSchema(parent.getSchema());
		n.setChildNonterminals(new ArrayList<Nonterminal>());
		n.setParentNonterminal(parent);
		
		if (depth.intValue() > 0) {
			depth.decrement();
			n.getChildNonterminals().add(createHierarchy(n, depth));
		}
		return n;
	}
	
	
	private XmlSchema createSchema() {
		XmlSchema s = new XmlSchema();
		s.setExternalLabel("ext_lbl");
		s.setLabel("lbl");
		s.setUuid(UUID.randomUUID().toString());
		s.setNamespaces(new ArrayList<XmlNamespace>());
			
		XmlNamespace xns = new XmlNamespace();
		xns.setPrefix("ns");
		xns.setUrl("http://.../");
		xns.setSchema(s);
		s.getNamespaces().add(xns);
			
		schemaDao.saveOrUpdate(s);
		return s;
	}
}
