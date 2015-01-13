package de.dariah.base.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import de.dariah.aai.javasp.base.User;
import de.dariah.base.dao.base.BaseEntityDao;
import de.dariah.base.dao.base.Dao;
import de.dariah.base.dao.iface.UserAnnotationDao;
import de.dariah.base.model.base.Identifiable;
import de.dariah.base.model.base.UserAnnotateable;
import de.dariah.base.model.impl.UserAnnotation;
import de.dariah.base.model.impl.UserAnnotation.ACTION_TYPES;
import de.dariah.samlsp.orm.service.UserService;
import de.dariah.schereg.base.dao.ReadOnlySchemaElementDao;
import de.dariah.schereg.base.model.ReadOnlySchemaElement;
import de.dariah.schereg.util.ContextService;

@Service
@Transactional(propagation=Propagation.SUPPORTS, readOnly=true)
public class UserAnnotationServiceImpl implements UserAnnotationService {

	@Autowired UserAnnotationDao annotationDao;
	@Autowired ReadOnlySchemaElementDao readOnlySchemaElementDao;
	@Autowired UserService userService;

	@Override
	public UserAnnotation getAnnotation(int id) {
		return annotationDao.findById(id);
	}

	@Override
	public List<UserAnnotation> getAllAnnotations(int limit) {
		List<Criterion> crs = new ArrayList<Criterion>();
		Order o = Order.desc("created");		
		return annotationDao.findByCriteria(crs, o, limit);
	}
	
	@Override
	@Transactional(propagation=Propagation.SUPPORTS, readOnly=false)
	public void createOrUpdateAnnotation(Identifiable obj, String comment) {
		if (comment==null || comment.isEmpty()) {
			return;
		}
		
		UserAnnotation annotation = null;
		if (obj instanceof UserAnnotateable) {			
			annotation = ((UserAnnotateable)obj).getUserAnnotationsInSession().getLast();
		} else {
			annotation = createAnnotation(comment, obj, ACTION_TYPES.COMMENT_ONLY, false);
		}
		annotation.setComment(comment);
		annotationDao.saveOrUpdate(annotation);
	}
	
	@Override
	@Transactional(propagation=Propagation.SUPPORTS, readOnly=false)
	public void updateComment(int id, String comment) {
		if (comment==null || comment.isEmpty()) {
			return;
		}
		UserAnnotation a = annotationDao.findById(id);
		a.setComment(comment);
		annotationDao.saveOrUpdate(a);
	}

	private UserAnnotation createAnnotation(String comment, Identifiable obj, ACTION_TYPES action, boolean saveSnapshot) {
		UserAnnotation a = new UserAnnotation();
		a.setAnnotatedObjectId(obj.getId());
		a.setAnnotatedObjectType(obj.getClass().getName());
		a.setUser(userService.findById(ContextService.getInstance().getCurrentUserDetails().getId()));
		a.setComment(comment);
		a.setActionType(action);
		
		if (saveSnapshot) {
			a.setObjectSnapshot(obj);
		}
		a.setCreated(DateTime.now());
		
		return a;
	}

	@Override
	public List<UserAnnotation> getAnnotationsByAggregator(String type, int id) throws ClassNotFoundException {
		List<Criterion> crs = new ArrayList<Criterion>();
		crs.add(Restrictions.eq("aggregatorObjectId", id));
		
		if (type.equals(ReadOnlySchemaElement.class.getName())) {
			ReadOnlySchemaElement tmpSe = readOnlySchemaElementDao.findById(id);
			crs.add(Restrictions.eq("aggregatorObjectType", tmpSe.getType()));
		} else {
			crs.add(Restrictions.eq("aggregatorObjectType", type));
		}
		
		Order o = Order.desc("created");		
		return annotationDao.findByCriteria(crs, o, 100);
	}
	
	@Override
	public List<UserAnnotation> getAnnotationsByObject(String type, int id) throws ClassNotFoundException {
		List<Criterion> crs = new ArrayList<Criterion>();
		crs.add(Restrictions.eq("annotatedObjectId", id));
		
		if (type.equals(ReadOnlySchemaElement.class.getName())) {
			ReadOnlySchemaElement tmpSe = readOnlySchemaElementDao.findById(id);
			crs.add(Restrictions.eq("annotatedObjectType", tmpSe.getType()));
		} else {
			crs.add(Restrictions.eq("annotatedObjectType", type));
		}
		
		Order o = Order.desc("created");		
		return annotationDao.findByCriteria(crs, o, 100);	
	}

	@Override
	public UserAnnotation createAnnotation(String annotatedObjectType, int annotatedObjectId) throws Exception {
		Class<?> clazz = Class.forName(annotatedObjectType);
		
		if (annotatedObjectType.equals(ReadOnlySchemaElement.class.getName())) {
			ReadOnlySchemaElement tmpSe = readOnlySchemaElementDao.findById(annotatedObjectId);
			annotatedObjectType = tmpSe.getType();
			
			clazz = Class.forName(annotatedObjectType);
		}
		
		if (!UserAnnotateable.class.isAssignableFrom(clazz)) {
			throw new Exception("Annotations can only be created on Annotateable objects");
		}
		
		if (clazz!=null) {
			Map<String, Dao> allDaos = ContextService.getInstance().getBeansOfType(Dao.class);
			for (Dao dao : allDaos.values()) {
				if (dao.isSupported(clazz)) {
					UserAnnotation a = new UserAnnotation();
					UserAnnotateable anObject = (UserAnnotateable)dao.findById(annotatedObjectId);
					
					a.setAnnotatedObjectId(annotatedObjectId);
					a.setAnnotatedObjectType(annotatedObjectType);
					a.setAggregatorObjectId(anObject.getAggregatorObject().getId());
					a.setAggregatorObjectType(anObject.getAggregatorObject().getClass().getName());
					a.setUser(userService.findById(ContextService.getInstance().getCurrentUserDetails().getId()));
					a.setActionType(ACTION_TYPES.COMMENT_ONLY);
					a.setCreated(DateTime.now());
					
					return a;
				}
			}
		}
		
		return null;
	}

	@Override
	public UserAnnotation saveNewAnnotation(UserAnnotation a) {		
		return annotationDao.saveOrUpdate(a);
	}
}
