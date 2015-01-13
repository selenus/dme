package de.dariah.schereg.base.service;

import java.util.List;

import de.dariah.schereg.base.model.Project;

public interface ProjectService {
	public List<Project> listProjects();
	public Project getProject(int id);
	public void removeProject(int id);
	public void saveProject(Project project);
}
