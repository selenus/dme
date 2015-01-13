package de.dariah.schereg.base.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.Validator;

import de.dariah.base.dao.base.PersistedEntityDao;
import de.dariah.schereg.base.dao.MappingCellDao;
import de.dariah.schereg.base.dao.MappingDao;
import de.dariah.schereg.base.dao.ProjectDao;
import de.dariah.schereg.base.dao.SchemaDao;
import de.dariah.schereg.base.model.Mapping;
import de.dariah.schereg.base.model.MappingCell;
import de.dariah.schereg.base.model.Project;
import de.dariah.schereg.base.model.Schema;

@Service
@Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
public class MappingServiceImpl implements MappingService {
	
	@Autowired
	private MappingDao mappingDao;
	
	@Autowired
	private MappingCellDao mappingCellDao;
	
	@Autowired
	private ProjectDao projectDao;

	@Autowired
	private SchemaDao schemaDao;
	
	@Override
	public List<Mapping> listMappings() {
		return mappingDao.findAll();
	}

	@Override
	public Mapping getMapping(int id) {
		return mappingDao.findById(id);
	}	

	@Override
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public void removeMapping(int id) {		
		mappingDao.delete(id);		
	}
	
	@Override
	@Transactional(propagation=Propagation.REQUIRES_NEW, readOnly=false)
	public void saveMapping(Mapping mapping) {
		
		boolean containsSource = false;
		boolean containsTarget = false;
		
		Project project = projectDao.findById(mapping.getProjectId());
		for (Schema schema : project.getSchemas()) {
			if (schema.getId() == mapping.getSourceId()) {
				containsSource = true;
			}
			if (schema.getId() == mapping.getTargetId()) {
				containsTarget = true;
			}
		}
		
		if (!containsTarget) {
			project.getSchemas().add(schemaDao.findById(mapping.getTargetId()));
		}
		if (!containsSource) {
			project.getSchemas().add(schemaDao.findById(mapping.getSourceId()));
		}
		
		
		projectDao.saveOrUpdate(project);
		mappingDao.saveOrUpdate(mapping);		
	}
	
	
	public Mapping createNewMapping() {
		Mapping mapping = new Mapping();
		mapping.setProject(projectDao.getMainProject());
		mapping.setPerformMatching(true);
		
		return mapping;
	}

	@Override
	public Collection<Mapping> getMappingsCreatedAfter(DateTime created) {
		Criterion cr = Restrictions.gt("created", created);
		return mappingDao.findByCriterion(cr);
	}

	@Override
	public Collection<Mapping> getMappingsBySchema(int schemaId) {
		Criterion cr = Restrictions.or(Restrictions.eq("source.id", schemaId), Restrictions.eq("target.id", schemaId));
		
		return mappingDao.findByCriterion(cr);
	}
	
	@Override
	public List<Mapping> getMappingsBySchema(int sourceId, int targetId) throws Exception {
		Criterion cr = Restrictions.and(Restrictions.eq("source.id", sourceId), Restrictions.eq("target.id", targetId));	
		return mappingDao.findByCriterion(cr);
	}

	@Override
	public long getMappingCount() {
		return mappingDao.count(null);
	}

	@Override
	public DateTime getLastModified() {
		return mappingDao.getLastModified(null);
	}

	@Override
	public Collection<MappingCell> getMappingCells(int mappingId) {
		List<MappingCell> cells = mappingCellDao.findByCriterion(Restrictions.eq("mapping.id", mappingId));
		
		return cells;
		
	}

	@Override
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public void saveMappingCells(ArrayList<MappingCell> cells) {
		mappingCellDao.saveOrUpdate(cells);
	}
	
	@Override
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public void saveMappingCell(MappingCell cell) {
		mappingCellDao.saveOrUpdate(cell);
	}	
}
