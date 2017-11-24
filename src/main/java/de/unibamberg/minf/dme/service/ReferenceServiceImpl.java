package de.unibamberg.minf.dme.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.unibamberg.minf.dme.dao.interfaces.ReferenceDao;
import de.unibamberg.minf.dme.model.reference.Reference;
import de.unibamberg.minf.dme.service.interfaces.ReferenceService;

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

	@Override
	public Reference findReferenceBySchemaId(String schemaId) {
		return referenceDao.findById(schemaId);
	}

	@Override
	public void saveRoot(Reference root) {
		referenceDao.save(root);
	}

	@Override
	public Reference findReferenceById(Reference root, String referenceId) {
		return referenceDao.findById(root, referenceId);
	}
}