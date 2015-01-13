package de.dariah.schereg.base.dao.impl;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import de.dariah.base.dao.base.BaseEntityDaoImpl;
import de.dariah.schereg.base.dao.MappingCellInputDao;
import de.dariah.schereg.base.model.MappingCellInput;

@Repository
@Transactional
public class MappingCellInputDaoImpl extends BaseEntityDaoImpl<MappingCellInput> implements MappingCellInputDao {

	public MappingCellInputDaoImpl() {
		super(MappingCellInput.class);
	}
}
