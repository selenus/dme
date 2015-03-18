package eu.dariah.de.minfba.schereg.service;

import eu.dariah.de.minfba.core.metamodel.Nonterminal;
import eu.dariah.de.minfba.core.metamodel.interfaces.Schema;

public interface ElementService {
	public Schema getInitializedSchema(long id);
	public Nonterminal getElementHierarchy(long schemaId);
}
