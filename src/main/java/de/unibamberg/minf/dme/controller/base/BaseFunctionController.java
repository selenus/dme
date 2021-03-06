package de.unibamberg.minf.dme.controller.base;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;

import de.unibamberg.minf.dme.model.PersistedSession;
import de.unibamberg.minf.dme.model.base.Identifiable;
import de.unibamberg.minf.dme.model.base.Label;
import de.unibamberg.minf.dme.model.base.Nonterminal;
import de.unibamberg.minf.dme.model.datamodel.LabelImpl;
import de.unibamberg.minf.dme.model.datamodel.NonterminalImpl;
import de.unibamberg.minf.dme.model.mapping.MappedConceptImpl;
import de.unibamberg.minf.dme.model.mapping.base.MappedConcept;
import de.unibamberg.minf.dme.model.mapping.base.Mapping;
import de.unibamberg.minf.dme.model.reference.Reference;
import de.unibamberg.minf.dme.service.interfaces.MappedConceptService;
import de.unibamberg.minf.dme.service.interfaces.MappingService;
import de.unibamberg.minf.dme.service.interfaces.PersistedSessionService;
import de.unibamberg.minf.dme.service.interfaces.ReferenceService;

public abstract class BaseFunctionController extends BaseScheregController {
	@Autowired private MappedConceptService mappedConceptService;
	@Autowired protected PersistedSessionService sessionService;
	@Autowired private ReferenceService referenceService;
	
	public BaseFunctionController(String mainNavId) {
		super(mainNavId);
	}
	
	protected boolean getIsReadOnly(Identifiable entity, String userId) {
		// Checks both entity types
		return !mappingService.getUserCanWriteEntity(entity.getId(), userId);
	}
	
	protected String getSampleInputValue(Identifiable entity, String executableId, String httpSessionId, String userId, HttpServletResponse response) {
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
			parentClasses.add(NonterminalImpl.class.getName());
			parentClasses.add(Label.class.getName());
			parentClasses.add(LabelImpl.class.getName());
			
			Reference r = referenceService.findReferenceByChildId(entity.getId(), executableId, parentClasses);
			
			if (r!=null) {
				inputElementId = r.getId();
			}
		}
		PersistedSession session = sessionService.access(entity.getId(), httpSessionId, userId);
		if (session==null) {
			response.setStatus(HttpServletResponse.SC_RESET_CONTENT);
			return null;
		}
		
		String result = sessionService.getSampleInputValue(session, inputElementId);
		return result;
	}
}
