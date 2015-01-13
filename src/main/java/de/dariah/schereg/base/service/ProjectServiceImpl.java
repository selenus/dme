package de.dariah.schereg.base.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import de.dariah.schereg.base.dao.ProjectDao;
import de.dariah.schereg.base.model.Project;

@Service
@Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
public class ProjectServiceImpl implements ProjectService {

	@Autowired
	private ProjectDao projectDao;
	
	@Override
	public List<Project> listProjects() {
		return projectDao.findAll();
	}

	@Override
	public Project getProject(int id) {
		return projectDao.findById(id);
	}

	@Override
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public void removeProject(int id) {
		projectDao.delete(id);
	}

	@Override
	@Transactional(propagation=Propagation.REQUIRED, readOnly=false)
	public void saveProject(Project project) {
		projectDao.saveOrUpdate(project);
	}

}
