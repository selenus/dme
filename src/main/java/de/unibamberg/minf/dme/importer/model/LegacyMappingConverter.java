package de.unibamberg.minf.dme.importer.model;

import java.util.ArrayList;
import java.util.HashMap;

import de.unibamberg.minf.dme.model.mapping.MappedConceptImpl;
import de.unibamberg.minf.dme.model.mapping.MappingImpl;
import de.unibamberg.minf.dme.model.mapping.base.MappedConcept;
import de.unibamberg.minf.dme.model.mapping.base.Mapping;

public class LegacyMappingConverter {

	public static Mapping convertMapping(eu.dariah.de.minfba.core.metamodel.interfaces.Mapping oldM) {
		if (oldM==null) {
			return null;
		}
		Mapping m = new MappingImpl();
		m.setId(oldM.getId());
		m.setSourceId(oldM.getSourceId());
		m.setTargetId(oldM.getTargetId());
		m.setVersionId(oldM.getVersionId());
		m.setDescription(oldM.getDescription());
		
		if (oldM.getConcepts()!=null) {
			m.setConcepts(new ArrayList<MappedConcept>());
			for (eu.dariah.de.minfba.core.metamodel.interfaces.MappedConcept oldMc : oldM.getConcepts()) {
				m.getConcepts().add(LegacyMappingConverter.convertMappedConcept(oldMc));
			}
		}
		return m;
	}

	public static MappedConcept convertMappedConcept(eu.dariah.de.minfba.core.metamodel.interfaces.MappedConcept oldMc) {
		if (oldMc==null) {
			return null;
		}
		MappedConcept mc = new MappedConceptImpl();
		mc.setId(oldMc.getId());
		mc.setEntityId(oldMc.getEntityId());
		mc.setFunctionId(oldMc.getFunctionId());
		mc.setElementGrammarIdsMap(new HashMap<String, String>(oldMc.getElementGrammarIdsMap()));
		
		if (oldMc.getTargetElementGroups()!=null) {
			mc.setTargetElementIds(new ArrayList<String>());
			for (eu.dariah.de.minfba.core.metamodel.mapping.TargetElementGroup oldTeg : oldMc.getTargetElementGroups()) {
				if (!mc.getTargetElementIds().contains(oldTeg.getTargetContainerElementId())) {
					mc.getTargetElementIds().add(oldTeg.getTargetContainerElementId());
				}
			}
		}
		return mc;
	}

}
