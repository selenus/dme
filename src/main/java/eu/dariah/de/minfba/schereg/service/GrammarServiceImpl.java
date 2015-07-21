package eu.dariah.de.minfba.schereg.service;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import de.unibamberg.minf.gtf.compilation.GrammarCompiler;
import de.unibamberg.minf.gtf.exception.GrammarProcessingException;
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
import eu.dariah.de.minfba.schereg.service.interfaces.TemporaryFileService;

@Service
public class GrammarServiceImpl extends BaseReferenceServiceImpl implements GrammarService {
	@Autowired private GrammarDao grammarDao;
	@Autowired private SchemaDao schemaDao;
	@Autowired private TemporaryFileService tmpFileService;
		
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
	public void saveTemporaryGrammar(String grammarId, String lexerGrammar, String parserGrammar) throws IOException {
		tmpFileService.createDirectory("g" + grammarId);
		
		if (lexerGrammar!=null && !lexerGrammar.trim().isEmpty()) {
			lexerGrammar = "lexer grammar g" + grammarId + "Lexer;\n\n" + lexerGrammar;
			parserGrammar = "parser grammar g" + grammarId + "Parser;\n\n" + parserGrammar;
			tmpFileService.writeString("g" + grammarId + File.separator + "g" + grammarId + "Lexer.g4", lexerGrammar);
			tmpFileService.writeString("g" + grammarId + File.separator + "g" + grammarId + "Parser.g4", parserGrammar);
		} else {
			parserGrammar = "grammar g" + grammarId + ";\n\n" + parserGrammar;
			tmpFileService.writeString("g" + grammarId + File.separator + "g" + grammarId + ".g4", parserGrammar);
		}
	}
	
	@Override
	public void parseTemporaryGrammar(String grammarId) throws GrammarProcessingException {
		GrammarCompiler grammarCompiler = new GrammarCompiler();
		//try {
			grammarCompiler.init(new File(tmpFileService.getTmpUploadDirPath() + File.separator + "g" + grammarId), "g" + grammarId);
			grammarCompiler.generateGrammar();
		/*} catch (GrammarProcessingException e) {
			logger.error("Failed to parse temporary grammar", e);
		}*/
	}
	
	@Override
	public void compileTemporaryGrammar(String grammarId) throws GrammarProcessingException, IOException {
		GrammarCompiler grammarCompiler = new GrammarCompiler();
		//try {
			grammarCompiler.init(new File(tmpFileService.getTmpUploadDirPath() + File.separator + "g" + grammarId), "g" + grammarId);
			grammarCompiler.compileGrammar();
		/*} catch (GrammarProcessingException | IOException e) {
			logger.error("Failed to compile temporary grammar", e);
		}*/
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