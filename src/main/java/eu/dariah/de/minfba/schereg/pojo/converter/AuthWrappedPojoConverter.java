package eu.dariah.de.minfba.schereg.pojo.converter;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import eu.dariah.de.minfba.core.metamodel.tracking.TrackedEntity;
import eu.dariah.de.minfba.schereg.model.RightsContainer;
import eu.dariah.de.minfba.schereg.pojo.AuthWrappedPojo;

@Component
public class AuthWrappedPojoConverter {
	
	public <T extends TrackedEntity> AuthWrappedPojo<T> convert(RightsContainer<T> element, String userId) {
		if (element==null || element.getElement()==null) {
			return null;
		}
		AuthWrappedPojo<T> result = new AuthWrappedPojo<T>();
		result.setPojo(element.getElement());
		result.setDraft(element.isDraft());
		result.setReadOnly(element.isReadOnly());
		if (userId!=null) {
			result.setOwn(element.getOwnerId().equals(userId));
			result.setWrite(element.getWriteIds()!=null && element.getWriteIds().contains(userId));
			result.setShare(element.getShareIds()!=null && element.getShareIds().contains(userId));
		}
		return result;
	}
	
	public <T extends TrackedEntity> List<AuthWrappedPojo<T>> convert(List<RightsContainer<T>> elements, String userId) {
		if (elements==null) {
			return null;
		}
		List<AuthWrappedPojo<T>> result = new ArrayList<AuthWrappedPojo<T>>(elements.size());
		for (RightsContainer<T> element : elements) {
			AuthWrappedPojo<T> pojo = convert(element, userId);
			if (pojo!=null) {
				result.add(pojo);
			}
		}
		return result;
	}
}