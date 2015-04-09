package eu.dariah.de.minfba.schereg.service;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import eu.dariah.de.minfba.core.metamodel.function.DescriptionGrammarImpl;
import eu.dariah.de.minfba.core.metamodel.function.interfaces.DescriptionGrammar;
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
		
		DescriptionGrammarImpl grammar = new DescriptionGrammarImpl(schemaId, getGrammarName(label));
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
	
	private String getGrammarName(String label) {
		if (label==null || label.trim().isEmpty()) {
			label = "g" + new ObjectId().toString().toUpperCase();
		}
		label = label.replaceAll("\\W", "");
		label = label.substring(0,1).toLowerCase() + label.substring(1);
		return label;
	}
}