package de.unibamberg.minf.dme.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import de.unibamberg.minf.dme.dao.base.DaoImpl;
import de.unibamberg.minf.dme.dao.interfaces.ElementDao;
import de.unibamberg.minf.dme.dao.interfaces.FunctionDao;
import de.unibamberg.minf.dme.dao.interfaces.GrammarDao;
import de.unibamberg.minf.dme.dao.interfaces.MappedConceptDao;
import de.unibamberg.minf.dme.exception.GenericScheregException;
import de.unibamberg.minf.dme.model.base.Element;
import de.unibamberg.minf.dme.model.base.Function;
import de.unibamberg.minf.dme.model.base.Identifiable;
import de.unibamberg.minf.dme.model.function.FunctionImpl;
import de.unibamberg.minf.dme.model.grammar.GrammarImpl;
import de.unibamberg.minf.dme.model.mapping.MappedConceptImpl;
import de.unibamberg.minf.dme.model.mapping.base.MappedConcept;
import de.unibamberg.minf.dme.serialization.Reference;
import de.unibamberg.minf.dme.service.base.BaseReferenceServiceImpl;
import de.unibamberg.minf.dme.service.interfaces.MappedConceptService;
import eu.dariah.de.dariahsp.model.web.AuthPojo;

@Service
public class MappedConceptServiceImpl extends BaseReferenceServiceImpl implements MappedConceptService {
	@Autowired private MappedConceptDao mappedConceptDao;
	@Autowired private ElementDao elementDao;
	@Autowired private GrammarDao grammarDao;
	@Autowired private FunctionDao functionDao;
	
	@Override
	public void saveMappedConcept(MappedConcept mappedConcept, String mappingId, AuthPojo auth) {		
		boolean isNew = DaoImpl.isNewId(mappedConcept.getId()); 
		
		mappedConcept.setEntityId(mappingId);		
		mappedConceptDao.save(mappedConcept, auth.getUserId(), auth.getSessionId());
		
		Reference root = this.findReferenceById(mappingId);
		Reference refConcept;
		boolean needReferenceSave = false; 
		
		if (isNew) {
			refConcept = this.addChildReference(root, mappedConcept);
			
			Function function = new FunctionImpl(mappingId, "fMapping");
			functionDao.save(function, auth.getUserId(), auth.getSessionId());
			
			mappedConcept.setFunctionId(function.getId());
			
			addChildReference(refConcept, function); 
			needReferenceSave = true;
		} else {
			refConcept = findSubreference(root, mappedConcept.getId());
		}

		for (String sourceElementId : mappedConcept.getElementGrammarIdsMap().keySet()) {
			if (mappedConcept.getElementGrammarIdsMap().get(sourceElementId)==null) {
				Element source = elementDao.findById(sourceElementId);
				
				GrammarImpl grammar = new GrammarImpl(mappingId, source.getName());
				grammar.setPassthrough(true);
				grammarDao.save(grammar, auth.getUserId(), auth.getSessionId());
				
				mappedConcept.getElementGrammarIdsMap().put(sourceElementId, grammar.getId());
				
				addChildReference(refConcept, grammar); 
				
				needReferenceSave = true;
			}
		}
		
		if (needReferenceSave) {
			this.saveRootReference(root);
			mappedConceptDao.save(mappedConcept, auth.getUserId(), auth.getSessionId());
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
	public MappedConcept findById(String mappingId, String mappedConceptId, boolean eagerLoadHierarchy) {
		Reference reference = this.findReferenceById(mappingId);
		if (reference.getChildReferences()==null || reference.getChildReferences().size()==0 || 
				!reference.getChildReferences().containsKey(MappedConceptImpl.class.getName()) || 
				reference.getChildReferences().get(MappedConceptImpl.class.getName()).length==0) {
			return null;
		}
		
		List<String> grammarIds = new ArrayList<String>();
		List<String> functionIds = new ArrayList<String>();
		Reference r = null;
		
		for (Reference rConcept : reference.getChildReferences().get(MappedConceptImpl.class.getName())) {
			if (rConcept.getId().equals(mappedConceptId)) {
				r = rConcept;
				if (rConcept.getChildReferences()!=null && rConcept.getChildReferences().containsKey(GrammarImpl.class.getName())) {
					for (Reference rGrammar : rConcept.getChildReferences().get(GrammarImpl.class.getName())) {
						grammarIds.add(rGrammar.getId());
						if (rGrammar.getChildReferences()!=null && rGrammar.getChildReferences().containsKey(FunctionImpl.class.getName())) {
							for (Reference rFunction : rGrammar.getChildReferences().get(FunctionImpl.class.getName())) {
								functionIds.add(rFunction.getId());
							}
						}
					}
				}
				break;
			}
		}
		if (r==null) {
			return null;
		}
		
		List<Identifiable> elements = new ArrayList<Identifiable>();
		elements.add(mappedConceptDao.findById(mappedConceptId));
		elements.addAll(grammarDao.find(Query.query(Criteria.where("_id").in(grammarIds))));
		elements.addAll(functionDao.find(Query.query(Criteria.where("_id").in(functionIds))));
		Map<String, Identifiable> elementMap = new HashMap<String, Identifiable>(elements.size()); 
		for (Identifiable e : elements) {
			elementMap.put(e.getId(), e);
		}
		
		return (MappedConcept)this.fillElement(r, elementMap);
	}

	@Override
	public void removeMappedConcept(String mappingId, String mappedConceptId, AuthPojo auth) throws GenericScheregException {
		MappedConcept c = mappedConceptDao.findById(mappedConceptId);
		if (!c.getEntityId().equals(mappingId)) {
			throw new GenericScheregException("Attempted to delete mapped concept via wrong mapping");
		}
		try {
			this.removeReference(mappingId, mappedConceptId, auth);
			mappedConceptDao.delete(c, auth.getUserId(), auth.getSessionId());
		} catch (IllegalArgumentException | ClassNotFoundException e) {
			logger.error("Failed to remove mapped concept", e);
		}
		
	}
	
	@Override
	public void removeSourceElementById(AuthPojo auth, String mappingId, MappedConcept mc, String sourceId) {
		if (!mc.getElementGrammarIdsMap().containsKey(sourceId)) {
			return;
		}
		mc.getElementGrammarIdsMap().remove(sourceId);
		
		if (mc.getElementGrammarIdsMap().size()==0) {
			try {
				this.removeMappedConcept(mappingId, mc.getId(), auth);
			} catch (GenericScheregException e) {
				logger.error("Failed to remove empty mapped concept", e);
			}
		} else {
			this.saveMappedConcept(mc, mappingId, auth);
			
			Reference rMapping = this.findReferenceById(mappingId);
			removeSubreference(rMapping, sourceId);
			this.saveRootReference(rMapping);
		}
	}

	@Override
	public void removeSourceElementById(AuthPojo auth, String mappingId, String mappedConceptId, String sourceId) {
		this.removeSourceElementById(auth, mappingId, this.findById(mappedConceptId), sourceId);
	}
	
	private List<Identifiable> getAllElements(String mappingId) {
		List<Identifiable> elements = new ArrayList<Identifiable>();
		elements.addAll(mappedConceptDao.findByEntityId(mappingId));
		elements.addAll(grammarDao.findByEntityId(mappingId));
		elements.addAll(functionDao.findByEntityId(mappingId));
		return elements;
	}
}