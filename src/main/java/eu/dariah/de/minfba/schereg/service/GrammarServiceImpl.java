package eu.dariah.de.minfba.schereg.service;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import eu.dariah.de.minfba.core.metamodel.function.DescriptionGrammarImpl;
import eu.dariah.de.minfba.core.metamodel.function.interfaces.DescriptionGrammar;
import eu.dariah.de.minfba.core.metamodel.function.interfaces.TransformationFunction;
import eu.dariah.de.minfba.schereg.dao.GrammarDao;
import eu.dariah.de.minfba.schereg.dao.SchemaDao;
import eu.dariah.de.minfba.schereg.serialization.Reference;

@Service
public class GrammarServiceImpl extends BaseReferenceServiceImpl implements GrammarService {
	@Autowired private GrammarDao grammarDao;
	@Autowired private SchemaDao schemaDao;
	
	@Override
	public void deleteGrammarsBySchemaId(String schemaId) {}

	@Override
	public DescriptionGrammar deleteGrammarById(String schemaId, String id) {
		grammarDao.delete(id);
		
		return null;
	}

	@Override
	public void deleteFunctionsBySchemaId(String schemaId) {}

	@Override
	public TransformationFunction deleteFunctionById(String schemaId, String id) {
		return null;
	}
	
	@Override
	public DescriptionGrammar createAndAppendGrammar(String schemaId, String parentElementId, String label) {
		String rootElementId = schemaDao.findById(schemaId).getRootNonterminalId();
		Reference rRoot = this.findRootReferenceById(rootElementId);
		Reference rParent = findSubreference(rRoot, parentElementId);
		
		DescriptionGrammar grammar = new DescriptionGrammarImpl(schemaId, getGrammarName(label));
		grammarDao.save(grammar);
		
		addChildReference(rParent, grammar);
		saveRootReference(rRoot);

		return grammar;
	}
	
	private String getGrammarName(String label) {
		if (label==null || label.trim().isEmpty()) {
			label = "g" + new ObjectId().toString().toUpperCase();
		}
		label = label.replaceAll("\\W", "");
		label = label.substring(0,1).toLowerCase() + label.substring(1);
		return label;
	}
}