package eu.dariah.de.minfba.schereg.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import eu.dariah.de.minfba.core.metamodel.Nonterminal;
import eu.dariah.de.minfba.core.metamodel.interfaces.Schema;
import eu.dariah.de.minfba.core.metamodel.xml.XmlSchema;
import eu.dariah.de.minfba.schereg.dao.ElementDao;
import eu.dariah.de.minfba.schereg.dao.SchemaDao;

@Service
public class ElementServiceImpl implements ElementService {
	@Autowired private ElementDao elementDao;
	@Autowired private SchemaDao schemaDao;
	
	@Transactional(propagation=Propagation.REQUIRED, readOnly=true)
	public Schema getInitializedSchema(long id) {
		XmlSchema s = (XmlSchema) schemaDao.findById(id);

		s.getNonterminals().size();
		s.getTerminals().size();
		
		//Hibernate.initialize(s.getTerminals());
		//Hibernate.initialize(s.getNonterminals());
		
		return s;
	}
	
	@Transactional(propagation=Propagation.REQUIRED, readOnly=true)
	public Nonterminal getElementHierarchy(long schemaId) {
		Schema s = this.getInitializedSchema(schemaId);
	
		for (Nonterminal n : s.getNonterminals()) {
			if (n.getParentNonterminal()==null) {
				
				
				for (Nonterminal n1 : n.getChildNonterminals()) {
					System.out.println(n1.getName());
				}
				
				return n;
			}
		}
		return null;
	}
}
