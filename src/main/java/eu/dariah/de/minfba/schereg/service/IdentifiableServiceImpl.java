package eu.dariah.de.minfba.schereg.service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import edu.stanford.nlp.ling.Label;
import eu.dariah.de.minfba.core.metamodel.Nonterminal;
import eu.dariah.de.minfba.core.metamodel.function.DescriptionGrammarImpl;
import eu.dariah.de.minfba.core.metamodel.function.TransformationFunctionImpl;
import eu.dariah.de.minfba.core.metamodel.interfaces.Identifiable;
import eu.dariah.de.minfba.schereg.dao.interfaces.ElementDao;
import eu.dariah.de.minfba.schereg.dao.interfaces.FunctionDao;
import eu.dariah.de.minfba.schereg.dao.interfaces.GrammarDao;
import eu.dariah.de.minfba.schereg.service.interfaces.IdentifiableService;

@Service
public class IdentifiableServiceImpl implements IdentifiableService {

	@Autowired private ElementDao elementDao;
	@Autowired private GrammarDao grammarDao;
	@Autowired private FunctionDao functionDao;
	
	@Override
	public List<Identifiable> findByNameAndSchemaId(String query, String schemaId, Class<?>[] entityTypes) {
		List<Identifiable> result = new ArrayList<Identifiable>();
		Pattern searchPattern = Pattern.compile(query, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
		
		if (entityTypes==null) {
			entityTypes = new Class<?>[]{ Nonterminal.class, Label.class, DescriptionGrammarImpl.class, TransformationFunctionImpl.class };
		}
		for (Class<?> entityType : entityTypes) {
			if (entityType.equals(Nonterminal.class)) {
				result.addAll(elementDao.find(Query.query((new Criteria()).andOperator(
						Criteria.where("entityId").is(schemaId),
						Criteria.where("_class").is(Nonterminal.class.getName()),
						Criteria.where("name").regex(searchPattern)))));
			} else if (entityType.equals(Label.class)) {
				result.addAll(elementDao.find(Query.query((new Criteria()).andOperator(
						Criteria.where("entityId").is(schemaId),
						Criteria.where("_class").is(Label.class.getName()),
						Criteria.where("name").regex(searchPattern)))));
			} else if (entityType.equals(DescriptionGrammarImpl.class)) {
				result.addAll(grammarDao.find(Query.query((new Criteria()).andOperator(
						Criteria.where("entityId").is(schemaId), 
						Criteria.where("grammarName").regex(searchPattern)))));
			} else if (entityType.equals(TransformationFunctionImpl.class)) {
				result.addAll(functionDao.find(Query.query((new Criteria()).andOperator(
						Criteria.where("entityId").is(schemaId), 
						Criteria.where("name").regex(searchPattern)))));
			}
		}
		return result;
	}
	
	
}
