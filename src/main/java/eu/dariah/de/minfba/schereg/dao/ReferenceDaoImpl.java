package eu.dariah.de.minfba.schereg.dao;

import org.springframework.stereotype.Repository;

import eu.dariah.de.minfba.schereg.dao.base.BaseDaoImpl;
import eu.dariah.de.minfba.schereg.serialization.Reference;

@Repository
public class ReferenceDaoImpl extends BaseDaoImpl<Reference> implements ReferenceDao {
	public ReferenceDaoImpl() {
		super(Reference.class);
	}
}
