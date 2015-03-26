package eu.dariah.de.minfba.schereg.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.dariah.de.minfba.core.metamodel.interfaces.Element;
import eu.dariah.de.minfba.core.metamodel.interfaces.Schema;
import eu.dariah.de.minfba.schereg.dao.ElementDao;
import eu.dariah.de.minfba.schereg.dao.SchemaDao;

@Service
public class ElementServiceImpl implements ElementService {
	@Autowired private ElementDao elementDao;
	@Autowired private SchemaDao schemaDao;
	
	
	@Override
	public Element findRootBySchemaId(String schemaId) {
		Schema s = schemaDao.findById(schemaId);
		if (s!=null && s.getRootNonterminalId()!=null) {
			return elementDao.findById(s.getRootNonterminalId());
		}
		return null;
	}
	
	@Override
	public Element findRootByElementId(String rootElementId) {
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
	public void saveElement(Element root) {
		elementDao.save(root);
	}
}
