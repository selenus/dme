package eu.dariah.de.minfba.schereg.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.dariah.de.minfba.core.metamodel.function.DescriptionGrammarImpl;
import eu.dariah.de.minfba.core.metamodel.function.TransformationFunctionImpl;
import eu.dariah.de.minfba.core.metamodel.function.interfaces.DescriptionGrammar;
import eu.dariah.de.minfba.core.metamodel.function.interfaces.TransformationFunction;
import eu.dariah.de.minfba.core.metamodel.interfaces.Element;
import eu.dariah.de.minfba.schereg.dao.interfaces.GrammarDao;
import eu.dariah.de.minfba.schereg.dao.interfaces.SchemaDao;
import eu.dariah.de.minfba.schereg.serialization.Reference;
import eu.dariah.de.minfba.schereg.service.base.BaseReferenceServiceImpl;
import eu.dariah.de.minfba.schereg.service.interfaces.GrammarService;

@Service
public class GrammarServiceImpl extends BaseReferenceServiceImpl implements GrammarService {
	@Autowired private GrammarDao grammarDao;
	@Autowired private SchemaDao schemaDao;
	
	@Override
	public DescriptionGrammar createAndAppendGrammar(String schemaId, String parentElementId, String label) {
		String rootElementId = schemaDao.findById(schemaId).getRootNonterminalId();
		Reference rRoot = this.findRootReferenceById(rootElementId);
		Reference rParent = findSubreference(rRoot, parentElementId);
		
		DescriptionGrammarImpl grammar = new DescriptionGrammarImpl(schemaId, getNormalizedName(label));
		grammarDao.save(grammar);
		
		addChildReference(rParent, grammar);
		saveRootReference(rRoot);

		return grammar;
	}
	
	@Override
	public void deleteGrammarsBySchemaId(String schemaId) {}

	@Override
	public DescriptionGrammar deleteGrammarById(String schemaId, String id) {
		String rootElementId = schemaDao.findById(schemaId).getRootNonterminalId();
		
		DescriptionGrammar grammar = grammarDao.findById(id);
		if (grammar != null) {
			try {
				this.removeReference(rootElementId, id);
				grammarDao.delete(grammar);
				return grammar;
			} catch (Exception e) {
				logger.warn("An error occurred while deleting an element or its references. "
						+ "The owning schema {} might be in an inconsistent state", schemaId, e);
			}
		}
		return null;
	}

	@Override
	public DescriptionGrammar findById(String grammarId) {
		return grammarDao.findById(grammarId);
	}

	@Override
	public void saveGrammar(DescriptionGrammarImpl grammar) {
		List<TransformationFunctionImpl> transformationFunctions = grammar.getTransformationFunctions();
		grammar.setTransformationFunctions(null);
		grammarDao.save(grammar);
		grammar.setTransformationFunctions(transformationFunctions);
	}
}