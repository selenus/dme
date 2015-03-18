package eu.dariah.de.minfba.schereg.dao.base;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.criterion.Projections;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import eu.dariah.de.minfba.core.metamodel.BaseElement;
import eu.dariah.de.minfba.core.metamodel.BaseSchema;
import eu.dariah.de.minfba.core.metamodel.BaseTerminal;
import eu.dariah.de.minfba.core.metamodel.Label;
import eu.dariah.de.minfba.core.metamodel.Nonterminal;
import eu.dariah.de.minfba.core.metamodel.function.DescriptionGrammarImpl;
import eu.dariah.de.minfba.core.metamodel.function.GrammarContainer;
import eu.dariah.de.minfba.core.metamodel.function.TransformationFunctionImpl;
import eu.dariah.de.minfba.core.metamodel.xml.XmlNamespace;
import eu.dariah.de.minfba.core.metamodel.xml.XmlSchema;
import eu.dariah.de.minfba.core.metamodel.xml.XmlTerminal;
import eu.dariah.de.minfba.schereg.dao.SchemaDao;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:orm-test-context.xml"})
public class BasePersistenceTest {
	
	@Autowired protected SessionFactory sessionFactory;
	@Autowired protected SchemaDao schemaDao;
	
	@Before
	//@After
	public void cleanDatabase() {
		Session session = sessionFactory.openSession();
		Transaction t = session.beginTransaction();
		for (Object s : session.createCriteria(BaseSchema.class).list()) {
			session.delete(s);
		}		
		t.commit();
		
		this.assertNoObjects(session, new Class[] {
				BaseSchema.class, XmlSchema.class,
				BaseElement.class, Nonterminal.class, Label.class,
				BaseTerminal.class, XmlTerminal.class,
				XmlNamespace.class, 
				DescriptionGrammarImpl.class, TransformationFunctionImpl.class, GrammarContainer.class
			});
	}
	
	private void assertNoObjects(Session session, Class<?>[] clazz) {
		for (int i=0; i<clazz.length; i++) {			
			Criteria crit = session.createCriteria(clazz[i]);
			crit.setProjection(Projections.rowCount());
			Assert.assertEquals(0, ((Long)crit.list().get(0)).longValue());
        }
	}
}
