package eu.dariah.de.minfba.schereg.controller.base;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import eu.dariah.de.minfba.core.metamodel.Label;
import eu.dariah.de.minfba.core.metamodel.Nonterminal;
import eu.dariah.de.minfba.core.metamodel.interfaces.Identifiable;
import eu.dariah.de.minfba.core.metamodel.interfaces.MappedConcept;
import eu.dariah.de.minfba.core.metamodel.interfaces.Mapping;
import eu.dariah.de.minfba.core.metamodel.interfaces.Schema;
import eu.dariah.de.minfba.core.metamodel.mapping.MappedConceptImpl;
import eu.dariah.de.minfba.schereg.serialization.Reference;
import eu.dariah.de.minfba.schereg.service.interfaces.MappedConceptService;
import eu.dariah.de.minfba.schereg.service.interfaces.MappingService;
import eu.dariah.de.minfba.schereg.service.interfaces.PersistedSessionService;
import eu.dariah.de.minfba.schereg.service.interfaces.ReferenceService;

public abstract class BaseFunctionController extends BaseScheregController {
	@Autowired private MappingService mappingService;
	@Autowired private MappedConceptService mappedConceptService;
	@Autowired protected PersistedSessionService sessionService;
	@Autowired private ReferenceService referenceService;
	
	public BaseFunctionController(String mainNavId) {
		super(mainNavId);
	}
	
	protected Identifiable getEntity(String entityId) {
		Identifiable entity = mappingService.findMappingById(entityId);
		if (entity==null) {
			entity = schemaService.findSchemaById(entityId);
		}
		return entity;
	}
	
	protected boolean getIsReadOnly(Identifiable entity, String userId) {
		if (Schema.class.isAssignableFrom(entity.getClass())) {
			return !schemaService.getHasWriteAccess(entity.getId(), userId);
		} else {
			return !mappingService.getHasWriteAccess(entity.getId(), userId);
		}
	}
	
	protected String getSampleInputValue(Identifiable entity, String executableId, String httpSessionId, String userId) {
		List<String> parentClasses = new ArrayList<String>();

		String inputElementId = null;
		if (Mapping.class.isAssignableFrom(entity.getClass())) {
			parentClasses.add(MappedConceptImpl.class.getName());
			parentClasses.add(MappedConcept.class.getName());
			
			Reference parentConceptReference = referenceService.findReferenceByChildId(entity.getId(), executableId, parentClasses);
			MappedConcept mc = mappedConceptService.findById(parentConceptReference.getId());
			
			for (String sourceElementId : mc.getElementGrammarIdsMap().keySet()) {
				if (executableId.equals(mc.getElementGrammarIdsMap().get(sourceElementId))) {
					inputElementId = sourceElementId;
					break;
				}
			}
		} else {
			parentClasses.add(Nonterminal.class.getName());
			parentClasses.add(Label.class.getName());
			
			inputElementId = referenceService.findReferenceByChildId(entity.getId(), executableId, parentClasses).getId();	
		}
		
		return sessionService.getSampleInputValue(inputElementId, entity.getId(), httpSessionId, userId);
	}
}