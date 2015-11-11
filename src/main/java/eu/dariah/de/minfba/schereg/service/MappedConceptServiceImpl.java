package eu.dariah.de.minfba.schereg.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.dariah.samlsp.model.pojo.AuthPojo;
import eu.dariah.de.minfba.core.metamodel.interfaces.MappedConcept;
import eu.dariah.de.minfba.schereg.dao.interfaces.MappedConceptDao;
import eu.dariah.de.minfba.schereg.exception.GenericScheregException;
import eu.dariah.de.minfba.schereg.service.interfaces.MappedConceptService;

@Service
public class MappedConceptServiceImpl implements MappedConceptService {
	@Autowired private MappedConceptDao mappedConceptDao;
	/*@Autowired private GrammarDao grammarDao;
	@Autowired private FunctionDao functionDao;*/
	
	@Override
	public void saveMappedConcept(MappedConcept mappedConcept, AuthPojo auth) {
		mappedConceptDao.save(mappedConcept, auth.getUserId(), auth.getSessionId());
	}
	
	@Override
	public List<MappedConcept> findAll(String mappingId) {
		return mappedConceptDao.findByEntityId(mappingId);
	}

	@Override
	public MappedConcept findById(String id) {
		return mappedConceptDao.findById(id);
	}

	@Override
	public void removeMappedConcept(String mappingId, String mappedConceptId, AuthPojo auth) throws GenericScheregException {
		MappedConcept c = mappedConceptDao.findById(mappedConceptId);
		if (!c.getEntityId().equals(mappingId)) {
			throw new GenericScheregException("Attempted to delete mapped concept via wrong mapping");
		}
		/*List<String> deleteFunctionIds = new ArrayList<String>();
		if (c.getGrammars()!=null) {
		}*/
		mappedConceptDao.delete(c, auth.getUserId(), auth.getSessionId());
	}
}