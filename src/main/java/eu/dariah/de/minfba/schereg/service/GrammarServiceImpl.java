package eu.dariah.de.minfba.schereg.service;

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
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import de.unibamberg.minf.gtf.MainEngine;
import de.unibamberg.minf.gtf.compilation.GrammarCompiler;
import de.unibamberg.minf.gtf.exceptions.GrammarProcessingException;
import eu.dariah.de.dariahsp.model.web.AuthPojo;
import eu.dariah.de.minfba.core.metamodel.function.DescriptionGrammarImpl;
import eu.dariah.de.minfba.core.metamodel.function.GrammarContainer;
import eu.dariah.de.minfba.core.metamodel.function.TransformationFunctionImpl;
import eu.dariah.de.minfba.core.metamodel.function.interfaces.DescriptionGrammar;
import eu.dariah.de.minfba.core.metamodel.interfaces.Terminal;
import eu.dariah.de.minfba.schereg.dao.interfaces.GrammarDao;
import eu.dariah.de.minfba.schereg.serialization.Reference;
import eu.dariah.de.minfba.schereg.service.base.BaseReferenceServiceImpl;
import eu.dariah.de.minfba.schereg.service.interfaces.GrammarService;

@Service
public class GrammarServiceImpl extends BaseReferenceServiceImpl implements GrammarService {
		
	@Autowired private GrammarDao grammarDao;
	@Autowired private MainEngine engine;

	@Value(value="${paths.grammars}")
	private String grammarsRootPath;
	
		
	@Override
	public DescriptionGrammar createAndAppendGrammar(String schemaId, String parentElementId, String label, AuthPojo auth) {
		Reference rRoot = this.findReferenceById(schemaId);
		Reference rParent = findSubreference(rRoot, parentElementId);
		
		DescriptionGrammarImpl grammar = new DescriptionGrammarImpl(schemaId, getNormalizedName(label));
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
	public void clearGrammar(DescriptionGrammar g) {
		logger.info(String.format("Clearing %s grammar %s", g.isTemporary() ? "temporary" : "persistent", g.getIdentifier()));
		engine.getDescriptionEngine().unloadGrammar(g.getIdentifier());
		engine.getDescriptionEngine().deleteGrammar(g.getIdentifier());
	}
	
	@Override
	public Collection<String> saveTemporaryGrammar(DescriptionGrammar grammar, String lexerGrammar, String parserGrammar) throws IOException {
		saveGrammarToFilesystem(grammar, lexerGrammar, parserGrammar, true);
		
		File dirPath = new File(getGrammarDirectory(grammar.getIdentifier(), true));
		return collectFileNames(dirPath, ".g4");
	}
		
	@Override
	public Collection<String> parseTemporaryGrammar(DescriptionGrammar grammar) throws GrammarProcessingException {
		GrammarCompiler grammarCompiler = new GrammarCompiler();
		File dirPath = new File(getGrammarDirectory(grammar.getIdentifier(), true));
		grammarCompiler.init(dirPath, grammar.getIdentifier());
		grammarCompiler.generateGrammar();
		return collectFileNames(dirPath, ".java");
	}
	
	@Override
	public Collection<String> compileTemporaryGrammar(DescriptionGrammar grammar) throws GrammarProcessingException, IOException {
		GrammarCompiler grammarCompiler = new GrammarCompiler();
		File dirPath = new File(getGrammarDirectory(grammar.getIdentifier(), true));
		grammarCompiler.init(dirPath, grammar.getIdentifier());
		grammarCompiler.compileGrammar();
		return collectFileNames(dirPath, ".class");
	}
	
	@Override
	public List<String> getParserRules(DescriptionGrammar grammar) throws GrammarProcessingException {
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
	public DescriptionGrammar deleteGrammarById(String schemaId, String id, AuthPojo auth) {
		DescriptionGrammar grammar = grammarDao.findById(id);
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
		
		Reference[] gRefs = parentReference.getChildReferences().get(DescriptionGrammarImpl.class.getName());
		for (int i=0; i<gRefs.length; i++) {
			if (gRefs[i].getId().equals(grammarId)) {
				boolean change = false;				
				if (i>0 && delta==1) {
					Reference gTmp = gRefs[i-1];
					gRefs[i-1] = gRefs[i];
					gRefs[i] = gTmp;
					change = true;
				} else if (i<gRefs.length-1 && delta==-1) {
					Reference gTmp = gRefs[i+1];
					gRefs[i+1] = gRefs[i];
					gRefs[i] = gTmp;
					change = true;
				}
				
				if (change) {
					parentReference.getChildReferences().put(DescriptionGrammarImpl.class.getName(), gRefs);
					referenceDao.save(entityReference);
				}
				return;
			}
		}
	}
	
	@Override
	public DescriptionGrammar findById(String grammarId) {
		return grammarDao.findById(grammarId);
	}

	@Override
	public void saveGrammar(DescriptionGrammarImpl grammar, AuthPojo auth) {
		List<TransformationFunctionImpl> transformationFunctions = grammar.getTransformationFunctions();
		grammar.setTransformationFunctions(null);
		grammar.setLocked(true);
		grammar.setTemporary(false);
		grammar.setGrammarName(getNormalizedName(grammar.getGrammarName()));
		if (auth!=null) {
			grammarDao.save(grammar, auth.getUserId(), auth.getSessionId());
		} else {
			grammarDao.save(grammar);
		}
		
		grammar.setTransformationFunctions(transformationFunctions);
		
		if (grammar.isPassthrough()) {
			grammar.setError(false);
		} else {
			try {
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


	private void saveGrammarToFilesystem(DescriptionGrammar grammar, String lexerGrammar, String parserGrammar, boolean temporary) throws IOException {
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
	public List<DescriptionGrammar> findByEntityId(String entityId, boolean includeSources) {
		if (!includeSources) {		
			return grammarDao.findByEntityId(entityId);
		} else {
			return grammarDao.find(Query.query(Criteria.where("entityId").is(entityId)));
		}
	}

	@Override
	public List<DescriptionGrammar> findByIds(List<Object> grammarIds) {
		return grammarDao.find(Query.query(Criteria.where("_id").in(grammarIds)));
	}

	@Override
	public Map<String, GrammarContainer> serializeGrammarSources(String entityId) {
		List<DescriptionGrammar> grammars = this.findByEntityId(entityId, false);
		return serializeGrammarSources(grammars);
	}

	@Override
	public Map<String, GrammarContainer> serializeGrammarSources(List<DescriptionGrammar> grammars) {
		Map<String, GrammarContainer> containers = new HashMap<String, GrammarContainer>();
		if (grammars!=null) {
			for (DescriptionGrammar g : grammars) {
				if (g.isPassthrough() || g.isError() || g.isTemporary()) {
					continue;
				}
				
				if (DescriptionGrammarImpl.class.isAssignableFrom(g.getClass())) {
					g = this.findById(g.getId());
					containers.put(g.getId(), ((DescriptionGrammarImpl)g).getGrammarContainer());
				}
			}
		}
		return containers;
	}
	

}