package eu.dariah.de.minfba.schereg.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import eu.dariah.de.minfba.schereg.dao.base.BaseDaoImpl;
import eu.dariah.de.minfba.schereg.dao.base.Dao;
import eu.dariah.de.minfba.schereg.dao.interfaces.ReferenceDao;
import eu.dariah.de.minfba.schereg.serialization.Reference;

@Repository
public class ReferenceDaoImpl extends BaseDaoImpl<Reference> implements ReferenceDao, ApplicationContextAware {
	private ApplicationContext appContext;
	
	public ReferenceDaoImpl() {
		super(Reference.class);
	}
	
	@Override
	public void setApplicationContext(ApplicationContext appContext) throws BeansException {
		this.appContext = appContext;		
	}
	
	@Override
	public void deleteAll(Map<String, Reference[]> idMap) throws IllegalArgumentException, ClassNotFoundException {
		if (idMap==null) {
			return;
		}
		
		Dao matchingDao;
		Reference[] deleteReferences;
		List<String> deleteIds;
		Class<?> clazz;
		for (String type : idMap.keySet()) {
			deleteReferences = idMap.get(type);
			if (deleteReferences!=null && deleteReferences.length>0) {
				clazz = Class.forName(type);
				deleteIds = new ArrayList<String>(deleteReferences.length);
				for (int i=0; i<deleteReferences.length; i++) {
					Assert.isTrue(BaseDaoImpl.isValidObjectId(deleteReferences[i].getId()));
					deleteIds.add(deleteReferences[i].getId());
				}
				
				matchingDao = getMatchingDao(clazz);
				Assert.notNull(matchingDao);
				
				int result = matchingDao.delete(deleteIds);
				
				logger.info("Removed {} {} entities in consequence of a delete cascade", result, clazz.getSimpleName());
			}
		}
	}
	
	private Dao getMatchingDao(Class<?> entityType) {
		Map<String, Dao> daos = appContext.getBeansOfType(Dao.class);
		for (String key : daos.keySet()) {
			Dao dao = daos.get(key);
			if (dao.getClazz().isAssignableFrom(entityType)) {
				return dao;
			}
		}
		return null;
	}
}