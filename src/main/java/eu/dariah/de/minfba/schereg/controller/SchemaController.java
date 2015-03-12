package eu.dariah.de.minfba.schereg.controller;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping(value="/schema")
public class SchemaController {
	@Autowired private SessionFactory sessionFactory;
	
	@RequestMapping(value = {"", "/"}, method = RequestMethod.GET)
	public String getHome(Model model) {
		
        
        Session session = sessionFactory.openSession();
        session.beginTransaction();
 
         
       /* XmlSchema person = new Person("Steve", "Balmer");
        session.save(person);
 
        Employee employee = new Employee("James", "Gosling", "Marketing", new Date());
        session.save(employee);
 
        Owner owner = new Owner("Bill", "Gates", 300L, 20L);
        session.save(owner);
 */
         
        session.getTransaction().commit();
        session.close();
		
		return "schema/home";
	}
}
