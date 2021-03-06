package de.unibamberg.minf.dme.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import de.unibamberg.minf.dme.dao.interfaces.GrammarDao;
import de.unibamberg.minf.dme.model.base.Function;
import de.unibamberg.minf.dme.model.base.Grammar;
import de.unibamberg.minf.dme.model.grammar.GrammarContainer;
import de.unibamberg.minf.dme.model.grammar.GrammarImpl;
import de.unibamberg.minf.dme.model.reference.Reference;
import de.unibamberg.minf.dme.model.reference.ReferenceHelper;
import de.unibamberg.minf.dme.service.base.BaseReferenceServiceImpl;
import de.unibamberg.minf.dme.service.interfaces.GrammarService;
import de.unibamberg.minf.gtf.MainEngine;
import de.unibamberg.minf.gtf.compilation.GrammarCompiler;
import de.unibamberg.minf.gtf.exceptions.GrammarProcessingException;
import eu.dariah.de.dariahsp.model.web.AuthPojo;

@Service
public class GrammarServiceImpl extends BaseReferenceServiceImpl implements GrammarService {
		
	@Autowired private GrammarDao grammarDao;
	@Autowired private MainEngine engine;

	@Value(value="${paths.grammars}")
	private String grammarsRootPath;
	
		
	@Override
	public Grammar createAndAppendGrammar(String schemaId, String parentElementId, String label, AuthPojo auth) {
		Reference rRoot = this.findReferenceById(schemaId);
		Reference rParent = ReferenceHelper.findSubreference(rRoot, parentElementId);
		
		GrammarImpl grammar = new GrammarImpl(schemaId, getNormalizedName(label));
		grammar.setPassthrough(true);
		grammarDao.save(grammar, auth.getUserId(), auth.getSessionId());
		
		addChildReference(rParent, grammar);
		saveRootReference(rRoot);

		return grammar;
	}

	private String getGrammarDirectory(String grammarId, boolean temporary) {
		return grammarsRootPath + File.separator + grammarId + File.separator;
	}
	
	private String getGrammarFilePrefix(String grammarId, boolean temporary) {
		return getGrammarDirectory(grammarId, temporary) + grammarId;
	}
	
	@Override
	public void clearGrammar(Grammar g) {
		logger.info(String.format("Clearing %s grammar %s", g.isTemporary() ? "temporary" : "persistent", g.getIdentifier()));
		engine.getDescriptionEngine().unloadGrammar(g.getIdentifier());
		engine.getDescriptionEngine().deleteGrammar(g.getIdentifier());
	}
	
	@Override
	public Collection<String> saveTemporaryGrammar(Grammar grammar, String lexerGrammar, String parserGrammar) throws IOException {
		saveGrammarToFilesystem(grammar, lexerGrammar, parserGrammar, true);
		
		File dirPath = new File(getGrammarDirectory(grammar.getIdentifier(), true));
		return collectFileNames(dirPath, ".g4");
	}
		
	@Override
	public Collection<String> parseTemporaryGrammar(Grammar grammar) throws GrammarProcessingException {
		GrammarCompiler grammarCompiler = new GrammarCompiler();
		File dirPath = new File(getGrammarDirectory(grammar.getIdentifier(), true));
		grammarCompiler.init(dirPath, grammar.getIdentifier());
		grammarCompiler.generateGrammar();
		return collectFileNames(dirPath, ".java");
	}
	
	@Override
	public Collection<String> compileTemporaryGrammar(Grammar grammar) throws GrammarProcessingException, IOException {
		GrammarCompiler grammarCompiler = new GrammarCompiler();
		File dirPath = new File(getGrammarDirectory(grammar.getIdentifier(), true));
		grammarCompiler.init(dirPath, grammar.getIdentifier());
		grammarCompiler.compileGrammar();
		return collectFileNames(dirPath, ".class");
	}
	
	@Override
	public List<String> getParserRules(Grammar grammar) throws GrammarProcessingException {
		GrammarCompiler grammarCompiler = new GrammarCompiler();
		grammarCompiler.init(new File(getGrammarDirectory(grammar.getIdentifier(), true)), grammar.getIdentifier());		
		return grammarCompiler.getParserRules();
	}
	
	 
	
	public void copyTemporaryGrammar(String grammarId) throws GrammarProcessingException {
		
	}
	
	private Collection<String> collectFileNames(File dirPath, String suffix) {
		if (dirPath==null || suffix==null) {
			return null;
		}
		Collection<File> files = FileUtils.listFiles(dirPath, FileFilterUtils.suffixFileFilter(suffix), null);
		Collection<String> names = new ArrayList<String>();
		for (File f : files) {
			names.add(f.getName());
		}
		return names;
	}
	
	@Override
	public void deleteGrammarsBySchemaId(String schemaId, AuthPojo auth) {}

	@Override
	public Grammar deleteGrammarById(String schemaId, String id, AuthPojo auth) {
		Grammar grammar = grammarDao.findById(id);
		if (grammar != null) {
			try {
				this.removeReference(schemaId, id, auth);
				//grammarDao.delete(grammar, auth.getUserId(), auth.getSessionId());
				return grammar;
			} catch (Exception e) {
				logger.warn("An error occurred while deleting an element or its references. "
						+ "The owning schema {} might be in an inconsistent state", schemaId, e);
			}
		}
		return null;
	}

	@Override
	public void moveGrammar(String entityId, String grammarId, int delta, AuthPojo auth) {
		Reference entityReference = referenceDao.findById(entityId);
		Assert.notNull(entityReference);
		
		Reference parentReference = referenceDao.findParentByChildId(entityReference, grammarId);
		Assert.notNull(parentReference);
		
		List<Reference> gRefs = parentReference.getChildReferences().get(GrammarImpl.class.getName());
		for (int i=0; i<gRefs.size(); i++) {
			if (gRefs.get(i).getId().equals(grammarId)) {
				boolean change = false;				
				if (i>0 && delta==1) {
					Reference gTmp = gRefs.get(i-1);
					gRefs.set(i-1, gRefs.get(i));
					gRefs.set(i, gTmp);
					change = true;
				} else if (i<gRefs.size()-1 && delta==-1) {
					Reference gTmp = gRefs.get(i+1);
					gRefs.set(i+1, gRefs.get(i));
					gRefs.set(i, gTmp);
					change = true;
				}
				
				if (change) {
					parentReference.getChildReferences().put(GrammarImpl.class.getName(), gRefs);
					referenceDao.save(entityReference);
				}
				return;
			}
		}
	}
	
	@Override
	public Grammar findById(String grammarId) {
		return grammarDao.findById(grammarId);
	}

	@Override
	public void saveGrammar(GrammarImpl grammar, AuthPojo auth) {
		List<Function> transformationFunctions = grammar.getFunctions();
		grammar.setFunctions(null);
		grammar.setLocked(true);
		grammar.setTemporary(false);
		grammar.setName(getNormalizedName(grammar.getName()));
		if (auth!=null) {
			grammarDao.save(grammar, auth.getUserId(), auth.getSessionId());
		} else {
			grammarDao.save(grammar);
		}
		
		grammar.setFunctions(transformationFunctions);
		
		if (grammar.isPassthrough()) {
			grammar.setError(false);
		} else {
			try {
				if (grammar.getGrammarContainer()==null) {
					grammar.setGrammarContainer(new GrammarContainer());
				}
				
				this.saveGrammarToFilesystem(grammar, grammar.getGrammarContainer().getLexerGrammar(), grammar.getGrammarContainer().getParserGrammar(), false);
				
				GrammarCompiler grammarCompiler = new GrammarCompiler();
				grammarCompiler.init(new File(this.getGrammarDirectory(grammar.getIdentifier(), false)), grammar.getIdentifier());
				grammarCompiler.generateGrammar();
				grammarCompiler.compileGrammar();
				
				if ( (grammar.getBaseMethod()==null || grammar.getBaseMethod().trim().isEmpty()) && 
						(grammarCompiler.getParserRules()!=null && grammarCompiler.getParserRules().size()>0 ) ) {
					grammar.setBaseMethod(grammarCompiler.getParserRules().get(0));
				}
				if (grammarCompiler.getParserRules()!=null && !grammarCompiler.getParserRules().contains(grammar.getBaseMethod())) {
					grammar.setError(true);
				} else {
					grammar.setError(false);
				}
			} catch (IOException | GrammarProcessingException e) {
				grammar.setError(true);
				logger.error("Error while processing saved grammar", e);
			}
		}
		
		grammar.setLocked(false);
		if (auth!=null) {
			grammarDao.save(grammar, auth.getUserId(), auth.getSessionId());
		} else {
			grammarDao.save(grammar);
		}
		
	}


	private void saveGrammarToFilesystem(Grammar grammar, String lexerGrammar, String parserGrammar, boolean temporary) throws IOException {
		String dirPath = getGrammarDirectory(grammar.getIdentifier(), temporary);
		String filePathPrefix = getGrammarFilePrefix(grammar.getIdentifier(), temporary);
		
		if (Files.exists(Paths.get(dirPath))) {				
			FileUtils.deleteDirectory(new File(dirPath));
		}
		Files.createDirectories(Paths.get(dirPath));
				
		if (lexerGrammar!=null && !lexerGrammar.trim().isEmpty()) {
			lexerGrammar = "lexer grammar " + grammar.getIdentifier() + "Lexer;\n\n" + lexerGrammar;
			Files.write(Paths.get(filePathPrefix + "Lexer.g4"), lexerGrammar.getBytes());
			
			parserGrammar = "parser grammar " + grammar.getIdentifier() + "Parser;\n\n" + 
							"options { tokenVocab= " + grammar.getIdentifier() + "Lexer; }\n\n" + 
							parserGrammar;
			Files.write(Paths.get(filePathPrefix + "Parser.g4"), parserGrammar.getBytes());
		} else {
			parserGrammar = "grammar " + grammar.getIdentifier() + ";\n\n" + parserGrammar;
			Files.write(Paths.get(filePathPrefix + ".g4"), parserGrammar.getBytes());
		}
	}

	@Override
	public List<Grammar> findByEntityId(String entityId, boolean includeSources) {
		if (!includeSources) {		
			return grammarDao.findByEntityId(entityId);
		} else {
			return grammarDao.find(Query.query(Criteria.where("entityId").is(entityId)));
		}
	}

	@Override
	public List<Grammar> findByIds(List<Object> grammarIds) {
		return grammarDao.find(Query.query(Criteria.where("_id").in(grammarIds)));
	}

	@Override
	public Map<String, GrammarContainer> serializeGrammarSources(String entityId) {
		List<Grammar> grammars = this.findByEntityId(entityId, false);
		return serializeGrammarSources(grammars);
	}

	@Override
	public Map<String, GrammarContainer> serializeGrammarSources(List<Grammar> grammars) {
		Map<String, GrammarContainer> containers = new HashMap<String, GrammarContainer>();
		if (grammars!=null) {
			for (Grammar g : this.getNonPassthroughGrammars(grammars)) {
				if (GrammarImpl.class.isAssignableFrom(g.getClass())) {
					containers.put(g.getId(), ((GrammarImpl)g).getGrammarContainer());
				}
			}
		}
		return containers;
	}
	
	@Override
	public List<Grammar> getNonPassthroughGrammars(String entityId) {
		List<Grammar> grammars = this.findByEntityId(entityId, false);
		return this.getNonPassthroughGrammars(grammars);
	}

	@Override
	public List<Grammar> getNonPassthroughGrammars(List<Grammar> grammars) {
		List<Grammar> result = new ArrayList<Grammar>(); 
		for (Grammar g : grammars) {
			if (g.isPassthrough() || g.isError() || g.isTemporary()) {
				continue;
			}
			
			if (GrammarImpl.class.isAssignableFrom(g.getClass())) {
				g = this.findById(g.getId());
				result.add(g);
			}
		}
		return result;
	}
	

}