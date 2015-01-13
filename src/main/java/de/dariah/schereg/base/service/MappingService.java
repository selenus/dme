package de.dariah.schereg.base.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.joda.time.DateTime;

import de.dariah.schereg.base.model.Mapping;
import de.dariah.schereg.base.model.MappingCell;

public interface MappingService {
	public List<Mapping> listMappings();
	public Mapping getMapping(int id);
	public void removeMapping(int id);
	public void saveMapping(Mapping mapping);
	public Mapping createNewMapping();
	public Collection<Mapping> getMappingsBySchema(int schemaId);
	public Collection<Mapping> getMappingsCreatedAfter(DateTime created);
	public long getMappingCount();
	public DateTime getLastModified();
	public Collection<MappingCell> getMappingCells(int mappingId);
	public void saveMappingCells(ArrayList<MappingCell> cells);
	public void saveMappingCell(MappingCell cell);
	public List<Mapping> getMappingsBySchema(int sourceId, int targetId) throws Exception;
}
