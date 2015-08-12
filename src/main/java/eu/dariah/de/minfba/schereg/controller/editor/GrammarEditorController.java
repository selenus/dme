package eu.dariah.de.minfba.schereg.controller.editor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.validation.Valid;

import org.antlr.v4.tool.ANTLRMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import de.unibamberg.minf.gtf.TransformationEngine;
import de.unibamberg.minf.gtf.exception.GrammarGenerationException;
import de.unibamberg.minf.gtf.transformation.CompiledTransformationFunction;
import de.unibamberg.minf.gtf.transformation.processing.ExecutionGroup;
import eu.dariah.de.minfba.core.metamodel.function.DescriptionGrammarImpl;
import eu.dariah.de.minfba.core.metamodel.function.GrammarContainer;
import eu.dariah.de.minfba.core.metamodel.function.interfaces.DescriptionGrammar;
import eu.dariah.de.minfba.core.metamodel.function.interfaces.TransformationFunction;
import eu.dariah.de.minfba.core.web.controller.BaseTranslationController;
import eu.dariah.de.minfba.core.web.pojo.ModelActionPojo;
import eu.dariah.de.minfba.core.web.pojo.ModelActionPojo.FieldErrorPojo;
import eu.dariah.de.minfba.schereg.service.interfaces.FunctionService;
import eu.dariah.de.minfba.schereg.service.interfaces.GrammarService;

@Controller
@RequestMapping(value="/schema/editor/{schemaId}/grammar/{grammarId}")
public class GrammarEditorController extends BaseTranslationController {
	@Autowired private GrammarService grammarService;
	@Autowired private FunctionService functionService;
	@Autowired protected TransformationEngine engine;
		
	public GrammarEditorController() {
		super("schemaEditor");
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/remove")
	public @ResponseBody DescriptionGrammar removeElement(@PathVariable String schemaId, @PathVariable String grammarId) {
		return grammarService.deleteGrammarById(schemaId, grammarId);
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/async/create/function")
	public @ResponseBody TransformationFunction createFunction(@PathVariable String schemaId, @PathVariable String grammarId, @RequestParam String label) {		
		return functionService.createAndAppendFunction(schemaId, grammarId, label);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/get")
	public @ResponseBody DescriptionGrammar getElement(@PathVariable String schemaId, @PathVariable String grammarId) {
		return grammarService.findById(grammarId);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/form/edit")
	public String getEditForm(@PathVariable String schemaId, @PathVariable String grammarId, Model model, Locale locale) {
		DescriptionGrammarImpl g = (DescriptionGrammarImpl)grammarService.findById(grammarId);
		if (g.getGrammarContainer()==null) {
			g.setGrammarContainer(new GrammarContainer());
		}
		model.addAttribute("grammar", g);		
		model.addAttribute("actionPath", "/schema/editor/" + schemaId + "/grammar/" + grammarId + "/async/save");
		return "schemaEditor/form/grammar/edit";
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/async/save")
	public @ResponseBody ModelActionPojo saveGrammar(@Valid DescriptionGrammarImpl grammar, 
			@RequestParam(value="lexer-parser-options", defaultValue="combined") String lexerParserOption, BindingResult bindingResult, Locale locale) {
		ModelActionPojo result = this.getActionResult(bindingResult, locale);
		if (grammar.getId().isEmpty()) {
			grammar.setId(null);
		}
		grammarService.saveGrammar(grammar);
		return result;
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/processGrammarDialog")
	public String getProcessGrammarDialog(@PathVariable String grammarId, Model model, Locale locale) {
		DescriptionGrammarImpl g = (DescriptionGrammarImpl)grammarService.findById(grammarId);
		if (g.getGrammarContainer()==null) {
			g.setGrammarContainer(new GrammarContainer());
		}
		model.addAttribute("grammar", g);		
		return "schemaEditor/form/grammar/process";
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/help/editGrammar")
	public String showHelpEditGrammar(Model model, Locale locale) {	
		return "schemaEditor/help/grammar/editGrammar";
	}
		
	@RequestMapping(method = RequestMethod.POST, value = "/async/upload")
	public @ResponseBody ModelActionPojo uploadGrammar(@PathVariable String grammarId, @RequestParam boolean combined, @RequestParam String lexerGrammar, @RequestParam String parserGrammar) {
		ModelActionPojo result = new ModelActionPojo(false);
		if (parserGrammar==null || parserGrammar.trim().isEmpty()) {
			result.addFieldError("grammarContainer_parserGrammar", "~Parser grammar cannot be empty!");
		}
		if (!combined && (lexerGrammar==null || lexerGrammar.trim().isEmpty())) {
			result.addFieldError("grammarContainer_lexerGrammar", "~Lexer grammar cannot be empty for separate layout grammars!");
		}
		
		if (result.getErrorCount()==0) {
			try {
				DescriptionGrammar g = getTemporaryGrammar(grammarId);
				grammarService.saveTemporaryGrammar(g, lexerGrammar, parserGrammar);
				result.setSuccess(true);
			} catch (IOException e) {
				result.addObjectError("Failed to upload grammar: " + e.getClass().getName());
			}
		}
		return result;
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/parse")
	public @ResponseBody ModelActionPojo parseGrammar(@PathVariable String grammarId) {
		ModelActionPojo result = new ModelActionPojo(false);
		try {
			DescriptionGrammar g = getTemporaryGrammar(grammarId);
			grammarService.parseTemporaryGrammar(g);
			result.setSuccess(true);
		} catch (GrammarGenerationException e) {
			for (ANTLRMessage m : e.getErrors()) {
				result.addFieldError(e.getGrammarType().name(), String.format("~%s:%s => %s", m.line, m.charPosition, m.getMessageTemplate(true).render()));
			}
		} catch (Exception e) {
			result.addObjectError("Unspecified error while parsing grammar: " + e.getClass().getName());
		}
		return result;
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/compile")
	public @ResponseBody ModelActionPojo validateGrammar(@PathVariable String grammarId) {
		ModelActionPojo result = new ModelActionPojo(false);
		try {
			DescriptionGrammar g = getTemporaryGrammar(grammarId);
			grammarService.compileTemporaryGrammar(g);
			result.setSuccess(true);
		} catch (Exception e) {
			result.addObjectError("Unspecified error while compiling grammar: " + e.getClass().getName());
		}
		return result;
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/sandbox")
	public @ResponseBody ModelActionPojo sandboxGrammar(@PathVariable String grammarId, @RequestParam String baseMethod) {
		ModelActionPojo result = new ModelActionPojo(false);
		try {
			if (baseMethod==null || baseMethod.trim().isEmpty()) {
				result.setSuccess(true);				
			} else {
				DescriptionGrammar g = getTemporaryGrammar(grammarId);
				List<String> parserRules = grammarService.getParserRules(g);
				if (parserRules.contains(baseMethod.trim())) {
					result.setSuccess(true);
				} else {
					result.addFieldError("base_method", "~Specified base method was not found in grammar");
				}
			}
			
			
		} catch (Exception e) {
			result.addObjectError("Unspecified error while compiling grammar: " + e.getClass().getName());
		}
		return result;
	}
	
	
	
	
	@RequestMapping(method = RequestMethod.POST, value = "/async/parseSample")
	public @ResponseBody ModelActionPojo parseSampleInput(@PathVariable String grammarId, @RequestParam String initRule, @RequestParam String sample) {
		ModelActionPojo result = new ModelActionPojo(false);
		try {
			// TODO Find out if there is a gTmp - else use the (already existing g)
			
			DescriptionGrammar g = getTemporaryGrammar(grammarId);
			g.setBaseMethod(initRule);
			
			if (engine.checkGrammar(g)!=null) {
				String svg = engine.processGrammarToSVG(sample, new ExecutionGroup(g, new ArrayList<CompiledTransformationFunction>()));
				result.setSuccess(true);
				result.setPojo(svg);
			}
			
		} catch (Exception e) {
			logger.error("Transformation error", e);
		}
		return result;
	}
	
	private DescriptionGrammar getTemporaryGrammar(String id) {
		DescriptionGrammar g = new DescriptionGrammarImpl();
		g.setTemporary(true);
		g.setId(id);
		g.setGrammarName(g.getIdentifier());
		return g;
	}
}
