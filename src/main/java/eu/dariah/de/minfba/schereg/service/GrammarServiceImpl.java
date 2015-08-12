package eu.dariah.de.minfba.schereg.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import de.unibamberg.minf.gtf.compilation.GrammarCompiler;
import de.unibamberg.minf.gtf.exception.GrammarProcessingException;
import eu.dariah.de.minfba.core.metamodel.function.DescriptionGrammarImpl;
import eu.dariah.de.minfba.core.metamodel.function.TransformationFunctionImpl;
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

	@Value(value="${paths.grammars}")
	private String grammarsRootPath;
	
		
	@Override
	public DescriptionGrammar createAndAppendGrammar(String schemaId, String parentElementId, String label) {
		String rootElementId = schemaDao.findById(schemaId).getRootNonterminalId();
		Reference rRoot = this.findRootReferenceById(rootElementId);
		Reference rParent = findSubreference(rRoot, parentElementId);
		
		DescriptionGrammarImpl grammar = new DescriptionGrammarImpl(schemaId, getNormalizedName(label));
		grammar.setPassthrough(true);
		grammarDao.save(grammar);
		
		addChildReference(rParent, grammar);
		saveRootReference(rRoot);

		return grammar;
	}

	private String getGrammarDirectory(DescriptionGrammar grammar, boolean temporary) {
		return grammarsRootPath + File.separator + grammar.getIdentifier() + File.separator;
	}
	
	private String getGrammarFilePrefix(DescriptionGrammar grammar, boolean temporary) {
		return getGrammarDirectory(grammar, temporary) + grammar.getIdentifier();
	}
	
	@Override
	public void saveTemporaryGrammar(DescriptionGrammar grammar, String lexerGrammar, String parserGrammar) throws IOException {
		saveGrammarToFilesystem(grammar, lexerGrammar, parserGrammar, true);
	}
	
	
	private void saveGrammarToFilesystem(DescriptionGrammar grammar, String lexerGrammar, String parserGrammar, boolean temporary) throws IOException {
		String dirPath = getGrammarDirectory(grammar, temporary);
		String filePathPrefix = getGrammarFilePrefix(grammar, temporary);
		
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
	public void parseTemporaryGrammar(DescriptionGrammar grammar) throws GrammarProcessingException {
		GrammarCompiler grammarCompiler = new GrammarCompiler();
		
		grammarCompiler.init(new File(getGrammarDirectory(grammar, true)), grammar.getIdentifier());
		grammarCompiler.generateGrammar();
	}
	
	@Override
	public void compileTemporaryGrammar(DescriptionGrammar grammar) throws GrammarProcessingException, IOException {
		GrammarCompiler grammarCompiler = new GrammarCompiler();
		
		grammarCompiler.init(new File(getGrammarDirectory(grammar, true)), grammar.getIdentifier());
		grammarCompiler.compileGrammar();
	}
	
	@Override
	public List<String> getParserRules(DescriptionGrammar grammar) throws GrammarProcessingException {
		GrammarCompiler grammarCompiler = new GrammarCompiler();
		grammarCompiler.init(new File(getGrammarDirectory(grammar, true)), grammar.getIdentifier());		
		return grammarCompiler.getParserRules();
	}
	
	public void copyTemporaryGrammar(String grammarId) throws GrammarProcessingException {
		
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
		grammar.setLocked(true);
		grammar.setTemporary(false);
		grammarDao.save(grammar);
		grammar.setTransformationFunctions(transformationFunctions);
		
		if (grammar.isPassthrough()) {
			grammar.setError(false);
		} else {
			try {
				this.saveGrammarToFilesystem(grammar, grammar.getGrammarContainer().getLexerGrammar(), grammar.getGrammarContainer().getParserGrammar(), false);
				
				GrammarCompiler grammarCompiler = new GrammarCompiler();
				grammarCompiler.init(new File(this.getGrammarDirectory(grammar, false)), grammar.getIdentifier());
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
		grammarDao.save(grammar);
	}


}