package de.dariah.schereg.base.service;

import java.util.List;
import java.util.Set;

import de.dariah.schereg.base.model.MappingCell;

public interface MappingCellService {
	public MappingCell getMappingCell(int id);
	public List<MappingCell> getMappingCellsForOutput(int mappingId, int output);
	public List<MappingCell> getMappingCellsForInputOutput(int mappingId, int input, int output);
	public MappingCell createOrLoadCell(int id, int mappingId);
	public void mergeAndSave(MappingCell mc, Set<Integer> mciElementIds);
	public void remove(MappingCell mc);
	public List<MappingCell> getMappingCellsForInput(int mappingId, int inputCellId);
}
