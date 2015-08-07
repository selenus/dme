package eu.dariah.de.minfba.schereg.service.interfaces;

import eu.dariah.de.minfba.schereg.serialization.Reference;

public interface ReferenceService {
	public Reference findReferenceBySchemaAndChildId(String schemaId, String childId);
	
	public Reference findReferenceByChildId(String rootElementId, String childId);
	public Reference findReferenceByChildId(Reference baseBeference, String childId);
}
