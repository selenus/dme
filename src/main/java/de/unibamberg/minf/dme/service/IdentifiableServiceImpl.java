package de.unibamberg.minf.dme.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import de.unibamberg.minf.dme.dao.base.DaoImpl;
import de.unibamberg.minf.dme.dao.interfaces.ElementDao;
import de.unibamberg.minf.dme.dao.interfaces.FunctionDao;
import de.unibamberg.minf.dme.dao.interfaces.GrammarDao;
import de.unibamberg.minf.dme.model.base.Element;
import de.unibamberg.minf.dme.model.base.Function;
import de.unibamberg.minf.dme.model.base.Grammar;
import de.unibamberg.minf.dme.model.base.Identifiable;
import de.unibamberg.minf.dme.model.base.Label;
import de.unibamberg.minf.dme.model.base.ModelElement;
import de.unibamberg.minf.dme.model.base.Nonterminal;
import de.unibamberg.minf.dme.model.datamodel.LabelImpl;
import de.unibamberg.minf.dme.model.datamodel.NonterminalImpl;
import de.unibamberg.minf.dme.model.function.FunctionImpl;
import de.unibamberg.minf.dme.model.grammar.GrammarImpl;
import de.unibamberg.minf.dme.model.reference.Reference;
import de.unibamberg.minf.dme.service.base.BaseServiceImpl;
import de.unibamberg.minf.dme.service.interfaces.GrammarService;
import de.unibamberg.minf.dme.service.interfaces.IdentifiableService;
import eu.dariah.de.dariahsp.model.web.AuthPojo;

@Service
public class IdentifiableServiceImpl extends BaseServiceImpl implements IdentifiableService {

	@Autowired private ElementDao elementDao;
	@Autowired private GrammarDao grammarDao;
	@Autowired private FunctionDao functionDao;
	
	@Autowired private GrammarService grammarService;
	
	@Override
	public List<Identifiable> findByEntityId(String entityId) {
		List<Identifiable> result = new ArrayList<Identifiable>();

		Class<?>[] entityTypes = new Class<?>[]{ Nonterminal.class, Label.class, GrammarImpl.class, FunctionImpl.class };
		for (Class<?> entityType : entityTypes) {
			if (entityType.equals(Nonterminal.class)) {
				result.addAll(elementDao.find(Query.query(Criteria.where("entityId").is(entityId))));
			} else if (entityType.equals(Label.class)) {
				result.addAll(elementDao.find(Query.query(Criteria.where("entityId").is(entityId))));
			} else if (entityType.equals(GrammarImpl.class)) {
				result.addAll(grammarDao.find(Query.query(Criteria.where("entityId").is(entityId))));
			} else if (entityType.equals(FunctionImpl.class)) {
				result.addAll(functionDao.find(Query.query(Criteria.where("entityId").is(entityId))));
			}
		}
		return result;
	}
	
	@Override
	public List<Identifiable> findByNameAndSchemaId(String query, String schemaId, Class<?>[] entityTypes) {
		List<Identifiable> result = new ArrayList<Identifiable>();
		Pattern searchPattern = Pattern.compile(query, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
		
		if (entityTypes==null) {
			entityTypes = new Class<?>[]{ Nonterminal.class, Label.class, GrammarImpl.class, FunctionImpl.class };
		}
		for (Class<?> entityType : entityTypes) {
			if (entityType.equals(Nonterminal.class)) {
				result.addAll(elementDao.find(Query.query((new Criteria()).andOperator(
						Criteria.where("entityId").is(schemaId),
						//Criteria.where("_class").is(NonterminalImpl.class.getName()),
						Criteria.where("name").regex(searchPattern)))));
			} else if (entityType.equals(Label.class)) {
				result.addAll(elementDao.find(Query.query((new Criteria()).andOperator(
						Criteria.where("entityId").is(schemaId),
						//Criteria.where("_class").is(LabelImpl.class.getName()),
						Criteria.where("name").regex(searchPattern)))));
			} else if (entityType.equals(GrammarImpl.class)) {
				result.addAll(grammarDao.find(Query.query((new Criteria()).andOperator(
						Criteria.where("entityId").is(schemaId), 
						Criteria.where("name").regex(searchPattern)))));
			} else if (entityType.equals(FunctionImpl.class)) {
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
	public List<Class<? extends ModelElement>> getAllowedSubelementTypes(String elementId) {
		Identifiable i = this.findById(elementId);
		
		List<Class<? extends ModelElement>> allowedSubelementTypes; 
		if (i!=null) { 
			if (Nonterminal.class.isAssignableFrom(i.getClass())) {
				allowedSubelementTypes = new ArrayList<Class<? extends ModelElement>>(); 
				allowedSubelementTypes.addAll(getGrammarClasses());
				allowedSubelementTypes.addAll(getNonterminalClasses());
				return allowedSubelementTypes;
			} else if (Label.class.isAssignableFrom(i.getClass())) {
				allowedSubelementTypes = new ArrayList<Class<? extends ModelElement>>(); 
				allowedSubelementTypes.addAll(getGrammarClasses());
				allowedSubelementTypes.addAll(getLabelClasses());
				return allowedSubelementTypes;
			} else if (Grammar.class.isAssignableFrom(i.getClass())) {
				return getFunctionClasses();
			} else if (Function.class.isAssignableFrom(i.getClass())) {
				return getLabelClasses();
			}
		}
		return null;
	}
	
	public static List<Class<? extends ModelElement>> getNonterminalClasses() {
		List<Class<? extends ModelElement>> result = new ArrayList<Class<? extends ModelElement>>();
		result.add(NonterminalImpl.class);
		result.add(Nonterminal.class);
		return result;
	}
	
	public static List<Class<? extends ModelElement>> getLabelClasses() {
		List<Class<? extends ModelElement>> result = new ArrayList<Class<? extends ModelElement>>();
		result.add(LabelImpl.class);
		result.add(Label.class);
		return result;
	}
	
	public static List<Class<? extends ModelElement>> getGrammarClasses() {
		List<Class<? extends ModelElement>> result = new ArrayList<Class<? extends ModelElement>>();
		result.add(GrammarImpl.class);
		result.add(Grammar.class);
		return result;
	}
	
	public static List<Class<? extends ModelElement>> getFunctionClasses() {
		List<Class<? extends ModelElement>> result = new ArrayList<Class<? extends ModelElement>>();
		result.add(FunctionImpl.class);
		result.add(Function.class);
		return result;
	}

	public static List<ModelElement> extractAllByTypes(ModelElement i, List<Class<? extends ModelElement>> allowedSubtreeRoots) {
		List<ModelElement> result = new ArrayList<ModelElement>();
		if (i!=null) {
			if (allowedSubtreeRoots.contains(i.getClass())) {				
				result.add(i);
			}
			if (Nonterminal.class.isAssignableFrom(i.getClass())) {
				Nonterminal n = (Nonterminal)i;
				if (n.getChildNonterminals()!=null) {
					for (Nonterminal nChild : n.getChildNonterminals()) {
						result.addAll(extractAllByTypes(nChild, allowedSubtreeRoots));
					}
				}
				if (n.getGrammars()!=null) {
					for (Grammar g : n.getGrammars()) {
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
					for (Grammar g : l.getGrammars()) {
						result.addAll(extractAllByTypes(g, allowedSubtreeRoots));
					}
				}
			} else if (Grammar.class.isAssignableFrom(i.getClass())) {
				Grammar g = (Grammar)i;
				if (g.getFunctions()!=null) {
					for (Function t : g.getFunctions()) {
						result.addAll(extractAllByTypes(t, allowedSubtreeRoots));
					}
				}
			} else if (Function.class.isAssignableFrom(i.getClass())) {
				Function t = (Function)i;
				if (t.getOutputElements()!=null) {
					for (Label l : t.getOutputElements()) {
						result.addAll(extractAllByTypes(l, allowedSubtreeRoots));
					}
				}
			}
		}
		return result;
	}

	public static List<ModelElement> extractAllByType(ModelElement i, String rootElementType) {
		if (rootElementType.equals(Nonterminal.class.getName()) || rootElementType.equals(NonterminalImpl.class.getName())) {
			return extractAllByTypes(i, getNonterminalClasses());
		} else if (rootElementType.equals(Label.class.getName()) || rootElementType.equals(LabelImpl.class.getName())) {
			return extractAllByTypes(i, getLabelClasses());
		} else if (rootElementType.equals(Grammar.class.getName()) || rootElementType.equals(GrammarImpl.class.getName())) {
			return extractAllByTypes(i, getGrammarClasses());
		} else if (rootElementType.equals(Nonterminal.class.getName()) || rootElementType.equals(NonterminalImpl.class.getName())) {
			return extractAllByTypes(i, getFunctionClasses());
		}
		return null;
	}
	
	@Override
	public Reference saveHierarchy(ModelElement me, AuthPojo auth) {
		return this.saveHierarchy(me, auth, false);
	}
	
	@Override
	public List<Reference> saveHierarchies(List<ModelElement> elements, AuthPojo auth) {
		List<Reference> references = new ArrayList<Reference>();
		List<Element> saveElements = new ArrayList<Element>();
		List<Grammar> saveGrammars = new ArrayList<Grammar>();
		List<Function> saveFunctions = new ArrayList<Function>();
		
		for (ModelElement me : elements) {
			references.add(this.saveElementsInHierarchy(me, saveElements, saveGrammars, saveFunctions, false));
		}
		
		if (!saveElements.isEmpty()) {
			elementDao.saveNew(saveElements, auth.getUserId(), auth.getSessionId());
		}
		if (!saveGrammars.isEmpty()) {
			for (Grammar g : saveGrammars) {
				grammarService.saveGrammar((GrammarImpl)g, auth);
			}
		}
		if (!saveFunctions.isEmpty()) {
			functionDao.saveNew(saveFunctions, auth.getUserId(), auth.getSessionId());
		}
		return references;
	}

	@Override
	public Reference saveHierarchy(ModelElement me, AuthPojo auth, boolean skipIdExisting) {
		List<Element> saveElements = new ArrayList<Element>();
		List<Grammar> saveGrammars = new ArrayList<Grammar>();
		List<Function> saveFunctions = new ArrayList<Function>();
		Reference r = this.saveElementsInHierarchy(me, saveElements, saveGrammars, saveFunctions, skipIdExisting);
		
		if (!saveElements.isEmpty()) {
			elementDao.saveNew(saveElements, auth.getUserId(), auth.getSessionId());
		}
		if (!saveGrammars.isEmpty()) {
			for (Grammar g : saveGrammars) {
				grammarService.saveGrammar((GrammarImpl)g, auth);
			}
		}
		if (!saveFunctions.isEmpty()) {
			functionDao.saveNew(saveFunctions, auth.getUserId(), auth.getSessionId());
		}
		return r;
	}
	

	
	private Reference saveElementsInHierarchy(ModelElement me, List<Element> saveElements, List<Grammar> saveGrammars, List<Function> saveFunctions, boolean skipIdExisting) {
		Reference r = new Reference();
		Map<String, List<? extends ModelElement>> subElementsMap = new HashMap<String, List<? extends ModelElement>>();
		
		boolean skip = false;
		if (me.getId()==null) {
			me.setId(DaoImpl.createNewObjectId());
		} else {
			skip = skipIdExisting;
		}
		r.setId(me.getId());
				
		if (Element.class.isAssignableFrom(me.getClass())) {
			Element e = (Element)me;
			if (e.getGrammars()!=null) {
				subElementsMap.put(GrammarImpl.class.getName(), e.getGrammars());
				e.setGrammars(null);
			}
			if (Nonterminal.class.isAssignableFrom(me.getClass())) {
				Nonterminal n = ((Nonterminal)me);
				if (n.getChildNonterminals()!=null) {
					subElementsMap.put(NonterminalImpl.class.getName(), n.getChildNonterminals());
					n.setChildNonterminals(null); // or empty?
				}
			} else {
				Label l = ((Label)e);
				if (l.getSubLabels()!=null) {
					subElementsMap.put(LabelImpl.class.getName(), l.getSubLabels());
					l.setSubLabels(null); // or empty?
				}
			}
			if (saveElements.contains(e) || saveGrammars.contains(me) || saveFunctions.contains(me)) {
				r.setReuse(true);
				//logger.debug("Recursion at " + e.getId());
			} else if (skip) {
				r.setReuse(true);
				//logger.debug("Skipping existing...implied recursion at " + e.getId());
			} else {
				saveElements.add(e);
			}
		} else if (Grammar.class.isAssignableFrom(me.getClass())) {
			Grammar g = (Grammar)me;
			if (g.getFunctions()!=null) {
				subElementsMap.put(FunctionImpl.class.getName(), g.getFunctions());
				g.setFunctions(null);
			}
			if (saveGrammars.contains(me)) {
				r.setReuse(true);
				logger.debug("Recursion at " + g.getId());
			} else {
				saveGrammars.add(g);
			}
			
		} else if (Function.class.isAssignableFrom(me.getClass())) {
			Function f = (Function)me;
			if (f.getOutputElements()!=null) {
				subElementsMap.put(LabelImpl.class.getName(), f.getOutputElements());
				f.setOutputElements(null);
			}
			if (saveFunctions.contains(f)) {
				r.setReuse(true);
				logger.debug("Recursion at " + f.getId());
			} else {
				saveFunctions.add(f);
			}
			
		}
		
		if (!subElementsMap.isEmpty()) {
			r.setChildReferences(new HashMap<String, Reference[]>());		
			List<Reference> subreferences;
			for (String subclass : subElementsMap.keySet()) {
				subreferences = new ArrayList<Reference>();
				for (ModelElement childMe : subElementsMap.get(subclass)) {
					subreferences.add(this.saveElementsInHierarchy(childMe, saveElements, saveGrammars, saveFunctions, skipIdExisting));
				}
				r.getChildReferences().put(subclass, subreferences.toArray(new Reference[0]));
			}
		}
		return r;
	}	
}
