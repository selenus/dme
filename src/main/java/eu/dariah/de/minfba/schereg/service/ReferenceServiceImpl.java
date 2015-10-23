package eu.dariah.de.minfba.schereg.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.dariah.de.minfba.schereg.dao.interfaces.ReferenceDao;
import eu.dariah.de.minfba.schereg.dao.interfaces.SchemaDao;
import eu.dariah.de.minfba.schereg.serialization.Reference;
import eu.dariah.de.minfba.schereg.service.interfaces.ReferenceService;

@Service
public class ReferenceServiceImpl implements ReferenceService {
	@Autowired private ReferenceDao referenceDao;
	@Autowired private SchemaDao schemaDao;
	
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
		String rootId = schemaDao.findEnclosedById(schemaId).getRootNonterminalId();
		if (rootId!=null) {
			return this.findReferenceByChildId(rootId, childId);
		}
		return null;
	}

}
