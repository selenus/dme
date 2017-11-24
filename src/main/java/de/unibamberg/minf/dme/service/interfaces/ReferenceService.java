package de.unibamberg.minf.dme.service.interfaces;

import java.util.List;

import de.unibamberg.minf.dme.model.reference.Reference;

public interface ReferenceService {
	public Reference findReferenceBySchemaAndChildId(String schemaId, String childId);
	
	public Reference findReferenceByChildId(String rootElementId, String childId);
	public Reference findReferenceByChildId(Reference baseBeference, String childId);
	
	public Reference findReferenceByChildId(String rootElementId, String childId, List<String> parentClassNames);
	public Reference findReferenceByChildId(Reference reference, String childId, List<String> parentClassNames);

	public Reference findReferenceBySchemaId(String schemaId);

	public void saveRoot(Reference root);

	public Reference findReferenceById(Reference root, String referenceId);
}
