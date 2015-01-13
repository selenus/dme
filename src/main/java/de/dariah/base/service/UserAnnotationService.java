package de.dariah.base.service;

import java.util.List;

import de.dariah.base.model.base.Identifiable;
import de.dariah.base.model.impl.UserAnnotation;

public interface UserAnnotationService {
	public UserAnnotation getAnnotation(int id);
	public List<UserAnnotation> getAllAnnotations(int limit);
	public List<UserAnnotation> getAnnotationsByAggregator(String type, int id) throws ClassNotFoundException;
	public List<UserAnnotation> getAnnotationsByObject(String type, int id) throws ClassNotFoundException;
	
	public void createOrUpdateAnnotation(Identifiable obj, String comment);
	public void updateComment(int id, String comment);
	public UserAnnotation createAnnotation(String annotatedObjectType, int annotatedObjectId) throws ClassNotFoundException, Exception;
	public UserAnnotation saveNewAnnotation(UserAnnotation a);
}
