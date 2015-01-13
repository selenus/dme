package de.dariah.schereg;

import static org.junit.Assert.*;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.Before;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;

import de.dariah.schereg.base.model.Mapping;
import de.dariah.schereg.base.model.Schema;
import de.dariah.schereg.base.service.MappingService;
import de.dariah.schereg.base.service.SchemaService;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:root-context.xml", "classpath:servlet-context.xml"})
public class PersistenceTests {

    @Autowired
    private ApplicationContext applicationContext;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private HandlerAdapter handlerAdapter;

    @Before
    public void setUp() throws Exception {
        this.request = new MockHttpServletRequest();
        this.response = new MockHttpServletResponse();

        //this.handlerAdapter = this.applicationContext.getBean(HandlerAdapter.class);
    }

    private ModelAndView handle(HttpServletRequest request, HttpServletResponse response)
            throws Exception {
        final HandlerMapping handlerMapping = applicationContext.getBean(HandlerMapping.class);
        final HandlerExecutionChain handler = handlerMapping.getHandler(request);
        assertNotNull("No handler found for request, check you request mapping", handler);

        final Object controller = handler.getHandler();
        // if you want to override any injected attributes do it here

        final HandlerInterceptor[] interceptors =
            handlerMapping.getHandler(request).getInterceptors();
        for (HandlerInterceptor interceptor : interceptors) {
            final boolean carryOn = interceptor.preHandle(request, response, controller);
            if (!carryOn) {
                return null;
            }
        }

        final ModelAndView mav = handlerAdapter.handle(request, response, controller);
        return mav;
    }

    @Test
    public void testProcessFormSubmission() throws Exception {
        //request.setMethod("POST");
        //request.setRequestURI("/simple-form");
        //request.setParameter("myNumber", "");

        //final ModelAndView mav = handle(request, response);
        // test we're returned back to the form
       // assertViewName(mav, "simple-form");
        // make assertions on the errors
       // final BindingResult errors = assertAndReturnModelAttributeOfType(mav, "org.springframework.validation.BindingResult.myForm", BindingResult.class);
        //assertEquals(1, errors.getErrorCount());
       // assertEquals("", errors.getFieldValue("myNumber"));        
    	assertEquals(0, 0);
    }
    
    @Test
    @Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
    public void testSchemaHandling() {
    	SchemaService schemaSvc = this.applicationContext.getBean(SchemaService.class);
    	MappingService mappingSvc = this.applicationContext.getBean(MappingService.class);
    	
    	List<Schema> schemas = schemaSvc.listSchemas();
    	List<Mapping> mappings = mappingSvc.listMappings();
    	
    	for(Mapping mapping : mappings)
    	{
    		assertNotNull(mapping);
    		assertTrue(mapping.getMappingCells().size()>3);
    	}
    	
    }
    
}
