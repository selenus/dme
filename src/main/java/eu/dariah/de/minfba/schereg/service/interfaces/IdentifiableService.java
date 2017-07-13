package eu.dariah.de.minfba.schereg.service.interfaces;

import java.util.List;

import eu.dariah.de.dariahsp.model.web.AuthPojo;
import eu.dariah.de.minfba.core.metamodel.ModelElement;
import eu.dariah.de.minfba.core.metamodel.interfaces.Identifiable;
import eu.dariah.de.minfba.schereg.serialization.Reference;

public interface IdentifiableService {
	public List<Identifiable> findByNameAndSchemaId(String query, String schemaId, Class<?>[] entityTypes);
	public List<Class<? extends ModelElement>> getAllowedSubelementTypes(String elementId);
	public Identifiable findById(String id);
	public Reference saveHierarchy(ModelElement me, AuthPojo auth);
}
