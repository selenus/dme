package de.dariah.schereg.base.dao.impl;

import java.util.ArrayList;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import de.dariah.base.dao.base.PersistedEntityDaoImpl;
import de.dariah.schereg.base.dao.ProjectDao;
import de.dariah.schereg.base.model.Project;
import de.dariah.schereg.util.ScheRegConstants;

@Repository
public class ProjectDaoImpl extends PersistedEntityDaoImpl<Project> implements ProjectDao {

	public ProjectDaoImpl() {
		super(Project.class);
	}

	public Project getMainProject() {	
		ArrayList<Criterion> al = new ArrayList<Criterion>();
		al.add(Restrictions.eq("name", ScheRegConstants.PROJECT_NAME_MAIN));

		try {
			return super.findByCriteriaDistinct(new ArrayList<Criterion>(al));
		} catch (Exception e) {
			logger.error("Get Main Project failed: " + e.getMessage(), e);
			return null;
		}
	}

	@Override
	public Project getBackupProject() {
		ArrayList<Criterion> al = new ArrayList<Criterion>();
		al.add(Restrictions.eq("name", ScheRegConstants.PROJECT_NAME_BACKUP));

		try {
			return super.findByCriteriaDistinct(new ArrayList<Criterion>(al));
		} catch (Exception e) {
			logger.error("Get Main Project failed: " + e.getMessage(), e);
			return null;
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public Project findById(int id) {
		return super.findById(id);
	}

	@Override
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public void delete(int id) {
		super.delete(id);
	}
}
