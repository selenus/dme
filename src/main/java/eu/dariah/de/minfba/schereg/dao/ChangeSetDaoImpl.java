package eu.dariah.de.minfba.schereg.dao;

import org.springframework.stereotype.Repository;

import eu.dariah.de.minfba.core.metamodel.tracking.ChangeSet;
import eu.dariah.de.minfba.schereg.dao.base.BaseDaoImpl;
import eu.dariah.de.minfba.schereg.dao.interfaces.ChangeSetDao;

@Repository
public class ChangeSetDaoImpl extends BaseDaoImpl<ChangeSet> implements ChangeSetDao {
	public ChangeSetDaoImpl() {
		super(ChangeSet.class);
	}
}
