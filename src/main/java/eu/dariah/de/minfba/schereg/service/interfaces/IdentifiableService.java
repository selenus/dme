package eu.dariah.de.minfba.schereg.service.interfaces;

import java.util.List;

import eu.dariah.de.minfba.core.metamodel.interfaces.Identifiable;

public interface IdentifiableService {
	public List<Identifiable> findByNameAndSchemaId(String query, String schemaId, Class<?>[] entityTypes);
}
