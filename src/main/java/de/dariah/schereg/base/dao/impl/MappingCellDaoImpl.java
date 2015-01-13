package de.dariah.schereg.base.dao.impl;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import de.dariah.base.dao.base.PersistedEntityDaoImpl;
import de.dariah.schereg.base.dao.MappingCellDao;
import de.dariah.schereg.base.model.MappingCell;

@Repository
@Transactional
public class MappingCellDaoImpl extends PersistedEntityDaoImpl<MappingCell> implements MappingCellDao {

	public MappingCellDaoImpl() {
		super(MappingCell.class);
	}

	@Override
	public List<MappingCell> findAllByInputOutput(int mappingId, int input, int output) {
		
		Criteria cr = getCurrentSession().createCriteria(clazz);		
		cr.createAlias("mappingCellInputs", "mci");
		cr.add(Restrictions.eq("mapping.id", mappingId));
		cr.add(Restrictions.eq("output", output));
		cr.add(Restrictions.eq("mci.elementID", input));
		
		return cast(cr.list());
	}

	@Override
	public List<MappingCell> findByMappingAndInputCell(int mappingId, int input) {
		
		Criteria cr = getCurrentSession().createCriteria(clazz);		
		cr.createAlias("mappingCellInputs", "mci");
		cr.add(Restrictions.eq("mapping.id", mappingId));
		cr.add(Restrictions.eq("mci.elementID", input));
		
		return cast(cr.list());
	}
	
}
