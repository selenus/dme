package de.dariah.schereg;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:root-context.xml", "classpath:servlet-context.xml"})
public class SchemaTests {
	
	//private static final Logger logger = LoggerFactory.getLogger(SchemaTests.class);
	
    //@Autowired
    //private ApplicationContext applicationContext;
	
    @Before
    public void setUp() { }
    
    @Test
    @Transactional(propagation=Propagation.REQUIRED, readOnly=true)
    public void testLoadSchemaElements() { 
    	//SchemaService svc = applicationContext.getBean(SchemaService.class);
/*
    	ArrayList<SchemaElement> elements = svc.getRootElements(100558);
    	
    	for (SchemaElement element : elements) {
    		logger.info("Element found: " + element.getName());
    		assertTrue(element instanceof Containment);
    		Containment cont = (Containment)element;
    		assertTrue(cont.getParentId() == null);
    		
    		ArrayList<SchemaElement> subElements = svc.getChildElements(element.getId(), new HashSet<Integer>());
    		
    		for (SchemaElement subElement : subElements) {
    			logger.info("Subelement found: " + subElement.getName());
        	}
    	}    	*/
    }
}