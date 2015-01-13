package de.dariah.schereg.base.dao.impl;

import java.util.Collection;
import java.util.List;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import de.dariah.base.dao.base.PersistedSchemaElementDaoImpl;
import de.dariah.schereg.base.dao.DomainDao;
import de.dariah.schereg.base.model.Domain;

@Repository
@Transactional(readOnly = true)
// TODO: identification of global domains by their negative ids is weak (from Harmony)
public class DomainDaoImpl extends PersistedSchemaElementDaoImpl<Domain> implements DomainDao {

	public DomainDaoImpl() {
		super(Domain.class);
	}

	@Override
	public List<Domain> findGlobalDomains() {
		Criterion cr = Restrictions.lt("id", 0);
		return cast(getCurrentSession().createCriteria(Domain.class).add(cr).list());
	}	
	
	@Override
	public int delete(Collection<Domain> domains) {
		if (domains == null || domains.size() == 0) {
			return 0;
		}
		
		int i = 0;
		for(Domain domain : domains) {
			// Skip global domains
			if (domain.getId() < 0) {
				continue;
			}
			super.delete(domain);
			i++;
		}
		return i;
	}
	
	@Override
	public void delete(Domain domain) {
		if (domain.getId() < 0) {
			return;
		}
		super.delete(domain);
	}
}
