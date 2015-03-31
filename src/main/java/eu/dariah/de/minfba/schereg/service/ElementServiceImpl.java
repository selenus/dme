package eu.dariah.de.minfba.schereg.service;

import java.util.HashMap;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.dariah.de.minfba.core.metamodel.Label;
import eu.dariah.de.minfba.core.metamodel.Nonterminal;
import eu.dariah.de.minfba.core.metamodel.interfaces.Element;
import eu.dariah.de.minfba.core.metamodel.interfaces.Schema;
import eu.dariah.de.minfba.schereg.dao.ElementDao;
import eu.dariah.de.minfba.schereg.dao.ReferenceDao;
import eu.dariah.de.minfba.schereg.dao.SchemaDao;
import eu.dariah.de.minfba.schereg.serialization.Reference;

@Service
public class ElementServiceImpl implements ElementService {
	@Autowired private ElementDao elementDao;
	@Autowired private SchemaDao schemaDao;
	@Autowired private ReferenceDao referenceDao;
	
	@Override
	public Element findRootBySchemaId(String schemaId) {
		return this.findRootBySchemaId(schemaId, false);
	}
	
	@Override
	public Element findRootByElementId(String rootElementId) {
		return this.findRootByElementId(rootElementId, false);
	}
	
	@Override
	public Element findRootBySchemaId(String schemaId, boolean eagerLoadHierarchy) {
		Schema s = schemaDao.findById(schemaId);
		if (s!=null && s.getRootNonterminalId()!=null) {
			return this.findRootByElementId(s.getRootNonterminalId(), eagerLoadHierarchy);
		}
		return null;
	}
	
	@Override
	public Element findRootByElementId(String rootElementId, boolean eagerLoadHierarchy) {
		Element root = elementDao.findById(rootElementId);
		if (!eagerLoadHierarchy) {
			return root;
		}
		
		Reference r = referenceDao.findById(rootElementId);
		
		
		return elementDao.findById(rootElementId);
	}
	
	@Override
	public void deleteByRootElementId(String rootElementId) {
		elementDao.delete(rootElementId);
	}
	
	@Override
	public void deleteBySchemaId(String schemaId) {
		Schema s = schemaDao.findById(schemaId);
		if (s!=null && s.getRootNonterminalId()!=null) {
			elementDao.delete(s.getRootNonterminalId());
		}
	}

	@Override
	public Reference saveElementHierarchy(Element e) {
		Reference r = this.saveElementsInHierarchy(e);
		
		referenceDao.save(r);
		
		return r;
	}
	
	
	private Reference saveElementsInHierarchy(Element e) {
		Reference r = new Reference();
		
		List<? extends Element> subelements;
		Class<? extends Element> subelementClass;
		
		if (e instanceof Nonterminal) {
			Nonterminal n = ((Nonterminal)e);
			subelements = n.getChildNonterminals();
			n.setChildNonterminals(null);
			elementDao.save(e);
			
			n.setChildNonterminals((List<Nonterminal>)subelements);
			subelementClass = Nonterminal.class;
		} else {
			Label l = ((Label)e);
			subelements = l.getSubLabels();
			l.setSubLabels(null);
			elementDao.save(e);
			
			l.setSubLabels((List<Label>)subelements);			
			subelementClass = Label.class;
		}
				
		if (subelements!=null && subelements.size()>0) {
			r.setChildReferences(new HashMap<Class<?>, Reference[]>());
			
			Reference[] subreferences = new Reference[subelements.size()];
			for (int i=0; i<subreferences.length; i++) {
				subreferences[i] = saveElementsInHierarchy(subelements.get(i));
			}
			r.getChildReferences().put(subelementClass, subreferences);
		}
		
		// TODO Functions
		
		r.setId(e.getId());		
		return r;
	}
}
