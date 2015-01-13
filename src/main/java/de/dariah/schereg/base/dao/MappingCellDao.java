package de.dariah.schereg.base.dao;

import java.util.List;

import de.dariah.base.dao.base.PersistedEntityDao;
import de.dariah.schereg.base.model.MappingCell;

public interface MappingCellDao extends PersistedEntityDao<MappingCell> {
	public List<MappingCell> findAllByInputOutput(int mappingId, int input, int output);
	public List<MappingCell> findByMappingAndInputCell(int mappingId, int inputCellId);
}