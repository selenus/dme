package eu.dariah.de.minfba.schereg.service.interfaces;

import java.util.List;

import eu.dariah.de.minfba.core.metamodel.interfaces.Identifiable;

public interface IdentifiableService {
	public List<Identifiable> findByNameAndSchemaId(String query, String schemaId, Class<?>[] entityTypes);

	public List<Class<? extends Identifiable>> getAllowedSubelementTypes(String elementId);

	public Identifiable findById(String id);
}
