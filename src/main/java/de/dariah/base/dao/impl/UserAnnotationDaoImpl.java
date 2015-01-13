package de.dariah.base.dao.impl;

import java.util.Collection;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import de.dariah.base.dao.base.BaseEntityDaoImpl;
import de.dariah.base.dao.iface.UserAnnotationDao;
import de.dariah.base.model.impl.UserAnnotation;

@Repository
public class UserAnnotationDaoImpl extends BaseEntityDaoImpl<UserAnnotation> implements UserAnnotationDao {

	public UserAnnotationDaoImpl() {
		super(UserAnnotation.class);
	}
}
