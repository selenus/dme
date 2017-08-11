package eu.dariah.de.minfba.schereg.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.unibamberg.minf.dme.model.base.BaseElement;
import de.unibamberg.minf.dme.model.base.Element;
import de.unibamberg.minf.dme.model.base.Function;
import de.unibamberg.minf.dme.model.base.Label;
import de.unibamberg.minf.dme.model.function.FunctionImpl;
import eu.dariah.de.dariahsp.model.web.AuthPojo;
import eu.dariah.de.minfba.schereg.dao.interfaces.FunctionDao;
import eu.dariah.de.minfba.schereg.serialization.Reference;
import eu.dariah.de.minfba.schereg.service.base.BaseReferenceServiceImpl;
import eu.dariah.de.minfba.schereg.service.interfaces.FunctionService;

@Service
public class FunctionServiceImpl extends BaseReferenceServiceImpl implements FunctionService {
	@Autowired private FunctionDao functionDao;
	
	@Override
	public Function createAndAppendFunction(String schemaId, String grammarId, String label, AuthPojo auth) {
		Reference rRoot = this.findReferenceById(schemaId);
		Reference rParent = findSubreference(rRoot, grammarId);
		
		Function grammar = new FunctionImpl(schemaId, getNormalizedName(label));
		functionDao.save(grammar, auth.getUserId(), auth.getSessionId());
		
		addChildReference(rParent, grammar);
		saveRootReference(rRoot);

		return grammar;
	}

	@Override
	public void deleteFunctionsBySchemaId(String schemaId) {
		// TODO Auto-generated method stub
	}

	@Override
	public Function deleteFunctionById(String schemaId, String id, AuthPojo auth) {
		Function function = functionDao.findById(id);
		if (function != null) {
			try {
				this.removeReference(schemaId, id, auth);
				//functionDao.delete(function, auth.getUserId(), auth.getSessionId());
				return function;
			} catch (Exception e) {
				logger.warn("An error occurred while deleting an element or its references. "
						+ "The owning schema {} might be in an inconsistent state", schemaId, e);
			}
		}
		return null;
	}

	@Override
	public Function findById(String functionId) {
		return functionDao.findById(functionId);
	}

	@Override
	public void saveFunction(FunctionImpl function, AuthPojo auth) {
		List<Element> extElements = function.getExternalInputElements();
		function.setExternalInputElements(null);
		
		List<Label> outputElements = function.getOutputElements();
		function.setOutputElements(null);
		
		function.setName(getNormalizedName(function.getName()));
		
		functionDao.save(function, auth.getUserId(), auth.getSessionId());
		function.setExternalInputElements(extElements);
		function.setOutputElements(outputElements);
	}

	@Override
	public List<Function> findByEntityId(String entityId) {
		return functionDao.findByEntityId(entityId);
	}
}
