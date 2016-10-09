package eu.dariah.de.minfba.schereg.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.dariah.de.minfba.schereg.dao.interfaces.ReferenceDao;
import eu.dariah.de.minfba.schereg.serialization.Reference;
import eu.dariah.de.minfba.schereg.service.interfaces.ReferenceService;

@Service
public class ReferenceServiceImpl implements ReferenceService {
	@Autowired private ReferenceDao referenceDao;
		
	@Override
	public Reference findReferenceByChildId(String rootElementId, String childId) {
		return referenceDao.findParentByChildId(rootElementId, childId);
	}

	@Override
	public Reference findReferenceByChildId(Reference baseBeference, String childId) {
		return referenceDao.findParentByChildId(baseBeference, childId);
	}

	@Override
	public Reference findReferenceBySchemaAndChildId(String schemaId, String childId) {
		if (schemaId!=null) {
			return this.findReferenceByChildId(schemaId, childId);
		}
		return null;
	}

	@Override
	public Reference findReferenceByChildId(String rootElementId, String childId, List<String> parentClassNames) {
		return referenceDao.findParentByChildId(rootElementId, childId, parentClassNames);
	}

	@Override
	public Reference findReferenceByChildId(Reference reference, String childId, List<String> parentClassNames) {
		return referenceDao.findParentByChildId(reference, childId, parentClassNames);
	}
}