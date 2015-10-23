package eu.dariah.de.minfba.schereg.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.dariah.samlsp.model.pojo.AuthPojo;
import eu.dariah.de.minfba.core.metamodel.BaseElement;
import eu.dariah.de.minfba.core.metamodel.Label;
import eu.dariah.de.minfba.core.metamodel.function.TransformationFunctionImpl;
import eu.dariah.de.minfba.core.metamodel.function.interfaces.DescriptionGrammar;
import eu.dariah.de.minfba.core.metamodel.function.interfaces.TransformationFunction;
import eu.dariah.de.minfba.core.metamodel.interfaces.Element;
import eu.dariah.de.minfba.schereg.dao.interfaces.FunctionDao;
import eu.dariah.de.minfba.schereg.dao.interfaces.SchemaDao;
import eu.dariah.de.minfba.schereg.serialization.Reference;
import eu.dariah.de.minfba.schereg.service.base.BaseReferenceServiceImpl;
import eu.dariah.de.minfba.schereg.service.interfaces.FunctionService;

@Service
public class FunctionServiceImpl extends BaseReferenceServiceImpl implements FunctionService {
	@Autowired private SchemaDao schemaDao;
	@Autowired private FunctionDao functionDao;
	
	@Override
	public TransformationFunction createAndAppendFunction(String schemaId, String grammarId, String label, AuthPojo auth) {
		String rootElementId = schemaDao.findEnclosedById(schemaId).getRootNonterminalId();
		Reference rRoot = this.findRootReferenceById(rootElementId);
		Reference rParent = findSubreference(rRoot, grammarId);
		
		TransformationFunction grammar = new TransformationFunctionImpl(schemaId, getNormalizedName(label));
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
	public TransformationFunction deleteFunctionById(String schemaId, String id, AuthPojo auth) {
		String rootElementId = schemaDao.findEnclosedById(schemaId).getRootNonterminalId();
		
		TransformationFunction function = functionDao.findById(id);
		if (function != null) {
			try {
				this.removeReference(rootElementId, id, auth);
				functionDao.delete(function, auth.getUserId(), auth.getSessionId());
				return function;
			} catch (Exception e) {
				logger.warn("An error occurred while deleting an element or its references. "
						+ "The owning schema {} might be in an inconsistent state", schemaId, e);
			}
		}
		return null;
	}

	@Override
	public TransformationFunction findById(String functionId) {
		return functionDao.findById(functionId);
	}

	@Override
	public void saveFunction(TransformationFunctionImpl function, AuthPojo auth) {
		List<BaseElement> extElements = function.getExternalInputElements();
		function.setExternalInputElements(null);
		
		List<Label> outputElements = function.getOutputElements();
		function.setOutputElements(null);
		
		function.setName(getNormalizedName(function.getName()));
		
		functionDao.save(function, auth.getUserId(), auth.getSessionId());
		function.setExternalInputElements(extElements);
		function.setOutputElements(outputElements);
	}
}
