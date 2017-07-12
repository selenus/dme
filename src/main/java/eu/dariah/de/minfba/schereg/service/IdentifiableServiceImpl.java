package eu.dariah.de.minfba.schereg.service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import eu.dariah.de.minfba.core.metamodel.LabelImpl;
import eu.dariah.de.minfba.core.metamodel.NonterminalImpl;
import eu.dariah.de.minfba.core.metamodel.function.DescriptionGrammarImpl;
import eu.dariah.de.minfba.core.metamodel.function.TransformationFunctionImpl;
import eu.dariah.de.minfba.core.metamodel.function.interfaces.DescriptionGrammar;
import eu.dariah.de.minfba.core.metamodel.function.interfaces.TransformationFunction;
import eu.dariah.de.minfba.core.metamodel.interfaces.Identifiable;
import eu.dariah.de.minfba.core.metamodel.interfaces.Label;
import eu.dariah.de.minfba.core.metamodel.interfaces.Nonterminal;
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
	
	@Override
	public Identifiable findById(String id) {
		Identifiable i = elementDao.findById(id);
		if (i==null) {
			i = grammarDao.findById(id);
		}
		if (i==null) {
			i = functionDao.findById(id);
		}
		return i;
	}

	@Override
	public List<Class<? extends Identifiable>> getAllowedSubelementTypes(String elementId) {
		Identifiable i = this.findById(elementId);
		
		List<Class<? extends Identifiable>> allowedSubelementTypes; 
		if (i!=null) { 
			if (Nonterminal.class.isAssignableFrom(i.getClass())) {
				allowedSubelementTypes = new ArrayList<Class<? extends Identifiable>>(); 
				allowedSubelementTypes.addAll(getGrammarClasses());
				allowedSubelementTypes.addAll(getNonterminalClasses());
				return allowedSubelementTypes;
			} else if (Label.class.isAssignableFrom(i.getClass())) {
				allowedSubelementTypes = new ArrayList<Class<? extends Identifiable>>(); 
				allowedSubelementTypes.addAll(getGrammarClasses());
				allowedSubelementTypes.addAll(getLabelClasses());
				return allowedSubelementTypes;
			} else if (DescriptionGrammar.class.isAssignableFrom(i.getClass())) {
				return getFunctionClasses();
			} else if (TransformationFunction.class.isAssignableFrom(i.getClass())) {
				return getLabelClasses();
			}
		}
		return null;
	}
	
	public static List<Class<? extends Identifiable>> getNonterminalClasses() {
		List<Class<? extends Identifiable>> result = new ArrayList<Class<? extends Identifiable>>();
		result.add(NonterminalImpl.class);
		result.add(Nonterminal.class);
		return result;
	}
	
	public static List<Class<? extends Identifiable>> getLabelClasses() {
		List<Class<? extends Identifiable>> result = new ArrayList<Class<? extends Identifiable>>();
		result.add(LabelImpl.class);
		result.add(Label.class);
		return result;
	}
	
	public static List<Class<? extends Identifiable>> getGrammarClasses() {
		List<Class<? extends Identifiable>> result = new ArrayList<Class<? extends Identifiable>>();
		result.add(DescriptionGrammarImpl.class);
		result.add(DescriptionGrammar.class);
		return result;
	}
	
	public static List<Class<? extends Identifiable>> getFunctionClasses() {
		List<Class<? extends Identifiable>> result = new ArrayList<Class<? extends Identifiable>>();
		result.add(TransformationFunctionImpl.class);
		result.add(TransformationFunction.class);
		return result;
	}

	public static <T extends Identifiable> List<T> extractAllByTypes(Identifiable i, List<Class<? extends T>> allowedSubtreeRoots) {
		List<T> result = new ArrayList<T>();
		if (i!=null) {
			if (allowedSubtreeRoots.contains(i.getClass())) {				
				result.add((T)i);
			}
			if (Nonterminal.class.isAssignableFrom(i.getClass())) {
				Nonterminal n = (Nonterminal)i;
				if (n.getChildNonterminals()!=null) {
					for (Nonterminal nChild : n.getChildNonterminals()) {
						result.addAll(extractAllByTypes(nChild, allowedSubtreeRoots));
					}
				}
				if (n.getGrammars()!=null) {
					for (DescriptionGrammar g : n.getGrammars()) {
						result.addAll(extractAllByTypes(g, allowedSubtreeRoots));
					}
				}
			} else if (Label.class.isAssignableFrom(i.getClass())) {
				Label l = (Label)i;
				if (l.getSubLabels()!=null) {
					for (Label lChild : l.getSubLabels()) {
						result.addAll(extractAllByTypes(lChild, allowedSubtreeRoots));
					}
				}
				if (l.getGrammars()!=null) {
					for (DescriptionGrammar g : l.getGrammars()) {
						result.addAll(extractAllByTypes(g, allowedSubtreeRoots));
					}
				}
			} else if (DescriptionGrammar.class.isAssignableFrom(i.getClass())) {
				DescriptionGrammar g = (DescriptionGrammar)i;
				if (g.getTransformationFunctions()!=null) {
					for (TransformationFunction t : g.getTransformationFunctions()) {
						result.addAll(extractAllByTypes(t, allowedSubtreeRoots));
					}
				}
			} else if (TransformationFunction.class.isAssignableFrom(i.getClass())) {
				TransformationFunction t = (TransformationFunction)i;
				if (t.getOutputElements()!=null) {
					for (Label l : t.getOutputElements()) {
						result.addAll(extractAllByTypes(l, allowedSubtreeRoots));
					}
				}
			}
		}
		return result;
	}
	
	
}
