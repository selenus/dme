package eu.dariah.de.minfba.schereg.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.dariah.samlsp.model.pojo.AuthPojo;
import eu.dariah.de.minfba.core.metamodel.function.DescriptionGrammarImpl;
import eu.dariah.de.minfba.core.metamodel.function.TransformationFunctionImpl;
import eu.dariah.de.minfba.core.metamodel.function.interfaces.TransformationFunction;
import eu.dariah.de.minfba.core.metamodel.interfaces.Element;
import eu.dariah.de.minfba.core.metamodel.interfaces.Identifiable;
import eu.dariah.de.minfba.core.metamodel.interfaces.MappedConcept;
import eu.dariah.de.minfba.core.metamodel.mapping.MappedConceptImpl;
import eu.dariah.de.minfba.schereg.dao.interfaces.ElementDao;
import eu.dariah.de.minfba.schereg.dao.interfaces.FunctionDao;
import eu.dariah.de.minfba.schereg.dao.interfaces.GrammarDao;
import eu.dariah.de.minfba.schereg.dao.interfaces.MappedConceptDao;
import eu.dariah.de.minfba.schereg.exception.GenericScheregException;
import eu.dariah.de.minfba.schereg.serialization.Reference;
import eu.dariah.de.minfba.schereg.service.base.BaseReferenceServiceImpl;
import eu.dariah.de.minfba.schereg.service.interfaces.MappedConceptService;

@Service
public class MappedConceptServiceImpl extends BaseReferenceServiceImpl implements MappedConceptService {
	@Autowired private MappedConceptDao mappedConceptDao;
	@Autowired private ElementDao elementDao;
	@Autowired private GrammarDao grammarDao;
	@Autowired private FunctionDao functionDao;
	
	@Override
	public void saveMappedConcept(MappedConcept mappedConcept, String mappingId, AuthPojo auth) {		
		boolean isNew = isNewId(mappedConcept.getId()); 
		
		mappedConcept.setEntityId(mappingId);
		mappedConceptDao.save(mappedConcept, auth.getUserId(), auth.getSessionId());
		
		Element source = elementDao.findById(mappedConcept.getSourceElementId());
		if (isNew) {
			TransformationFunction function = new TransformationFunctionImpl(mappingId, "g" + source.getName());
			functionDao.save(function, auth.getUserId(), auth.getSessionId());
			
			DescriptionGrammarImpl grammar = new DescriptionGrammarImpl(mappingId, "f" + source.getName());
			grammar.setPassthrough(true);
			grammarDao.save(grammar, auth.getUserId(), auth.getSessionId());
			
			Reference root = this.findReferenceById(mappingId);
			addChildReference(addChildReference(addChildReference(root, mappedConcept), grammar), function); 
			
			this.saveRootReference(root);
		}
	}
	
	@Override
	public List<MappedConcept> findAllByMappingId(String mappingId) {
		return mappedConceptDao.findByEntityId(mappingId);
	}
	
	@Override
	public List<MappedConcept> findAllByMappingId(String mappingId, boolean eagerLoadHierarchy) {
		Reference reference = this.findReferenceById(mappingId);
		if (reference.getChildReferences()==null || reference.getChildReferences().size()==0 || 
				!reference.getChildReferences().containsKey(MappedConceptImpl.class.getName()) || 
				reference.getChildReferences().get(MappedConceptImpl.class.getName()).length==0) {
			return new ArrayList<MappedConcept>();
		}
		List<MappedConcept> result = new ArrayList<MappedConcept>(reference.getChildReferences().get(MappedConceptImpl.class.getName()).length);
		List<Identifiable> elements = this.getAllElements(mappingId);		
		Map<String, Identifiable> elementMap = new HashMap<String, Identifiable>(elements.size()); 
		for (Identifiable e : elements) {
			elementMap.put(e.getId(), e);
		}
		for (Reference r : reference.getChildReferences().get(MappedConceptImpl.class.getName())) {
			result.add((MappedConcept)this.fillElement(r, elementMap));
		}
		return result;
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
		
		Reference root = this.findReferenceById(mappingId);
		removeSubreference(root, mappedConceptId);
		this.saveRootReference(root);
	}
	
	private List<Identifiable> getAllElements(String mappingId) {
		List<Identifiable> elements = new ArrayList<Identifiable>();
		elements.addAll(mappedConceptDao.findByEntityId(mappingId));
		elements.addAll(grammarDao.findByEntityId(mappingId));
		elements.addAll(functionDao.findByEntityId(mappingId));
		return elements;
	}
}