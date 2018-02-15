package de.unibamberg.minf.dme.importer.mapping;

import java.util.List;
import java.util.Map;

import de.unibamberg.minf.dme.model.base.Grammar;
import de.unibamberg.minf.dme.model.mapping.base.MappedConcept;
import de.unibamberg.minf.dme.model.mapping.base.Mapping;
import eu.dariah.de.dariahsp.model.web.AuthPojo;

public interface MappingImportListener {
	public void registerImportFailed(Mapping mapping);
	public void registerImportFinished(Mapping mapping, List<MappedConcept> importedConcepts, Map<String, String> importedFunctions, Map<String, Grammar> importedGrammars, AuthPojo auth);
}
