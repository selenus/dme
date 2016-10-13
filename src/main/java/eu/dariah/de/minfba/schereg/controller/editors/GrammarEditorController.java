package eu.dariah.de.minfba.schereg.controller.editors;

import static org.mockito.Mockito.RETURNS_DEEP_STUBS;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.antlr.v4.tool.ANTLRMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.dariah.aai.javasp.web.helper.AuthInfoHelper;
import de.dariah.samlsp.model.pojo.AuthPojo;
import de.unibamberg.minf.gtf.DescriptionEngine;
import de.unibamberg.minf.gtf.exception.GrammarGenerationException;
import de.unibamberg.minf.gtf.transformation.CompiledTransformationFunction;
import de.unibamberg.minf.gtf.transformation.processing.ExecutionGroup;
import de.unibamberg.minf.gtf.transformation.processing.params.TransformationParamDefinition;
import eu.dariah.de.minfba.core.metamodel.Label;
import eu.dariah.de.minfba.core.metamodel.Nonterminal;
import eu.dariah.de.minfba.core.metamodel.function.DescriptionGrammarImpl;
import eu.dariah.de.minfba.core.metamodel.function.GrammarContainer;
import eu.dariah.de.minfba.core.metamodel.function.TransformationFunctionImpl;
import eu.dariah.de.minfba.core.metamodel.function.interfaces.DescriptionGrammar;
import eu.dariah.de.minfba.core.metamodel.function.interfaces.TransformationFunction;
import eu.dariah.de.minfba.core.metamodel.interfaces.Identifiable;
import eu.dariah.de.minfba.core.metamodel.interfaces.MappedConcept;
import eu.dariah.de.minfba.core.metamodel.mapping.MappedConceptImpl;
import eu.dariah.de.minfba.core.web.controller.BaseTranslationController;
import eu.dariah.de.minfba.core.web.pojo.ModelActionPojo;
import eu.dariah.de.minfba.core.web.pojo.FieldErrorPojo;
import eu.dariah.de.minfba.schereg.controller.base.BaseFunctionController;
import eu.dariah.de.minfba.schereg.controller.base.BaseScheregController;
import eu.dariah.de.minfba.schereg.model.PersistedSession;
import eu.dariah.de.minfba.schereg.serialization.Reference;
import eu.dariah.de.minfba.schereg.service.ElementServiceImpl;
import eu.dariah.de.minfba.schereg.service.interfaces.FunctionService;
import eu.dariah.de.minfba.schereg.service.interfaces.GrammarService;
import eu.dariah.de.minfba.schereg.service.interfaces.MappedConceptService;
import eu.dariah.de.minfba.schereg.service.interfaces.MappingService;
import eu.dariah.de.minfba.schereg.service.interfaces.PersistedSessionService;
import eu.dariah.de.minfba.schereg.service.interfaces.ReferenceService;

@Controller
@RequestMapping(value={"/schema/editor/{entityId}/grammar/{grammarId}", "/mapping/editor/{entityId}/grammar/{grammarId}"})
public class GrammarEditorController extends BaseFunctionController {
	
	@Autowired private GrammarService grammarService;
	@Autowired private FunctionService functionService;
	@Autowired protected DescriptionEngine engine;
	
		
	public GrammarEditorController() {
		super("schemaEditor");
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/remove")
	public @ResponseBody DescriptionGrammar removeElement(@PathVariable String entityId, @PathVariable String grammarId, HttpServletRequest request) {
		return grammarService.deleteGrammarById(entityId, grammarId, authInfoHelper.getAuth(request));
	}
	
	
	@RequestMapping(method = RequestMethod.GET, value = "/form/new_function")
	public String getNewGrammarForm(@PathVariable String entityId, @PathVariable String grammarId, Model model, Locale locale) {
		model.addAttribute("function", new TransformationFunctionImpl(entityId, null));
		model.addAttribute("actionPath", "/schema/editor/" + entityId + "/grammar/" + grammarId + "/async/saveNewFunction");
		return "schemaEditor/form/function/new";
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/async/saveNewFunction")
	public @ResponseBody ModelActionPojo saveNewGrammar(@PathVariable String entityId, @PathVariable String grammarId, @Valid TransformationFunctionImpl function, BindingResult bindingResult, Locale locale, HttpServletRequest request) {
		ModelActionPojo result = this.getActionResult(bindingResult, locale);
		if (result.isSuccess()) {
			functionService.createAndAppendFunction(entityId, grammarId, function.getName(), authInfoHelper.getAuth(request));
		}
		return result;
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/get")
	public @ResponseBody DescriptionGrammar getElement(@PathVariable String entityId, @PathVariable String grammarId) {
		return grammarService.findById(grammarId);
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/form/editWdata")
	public String getEditFormWithData(@PathVariable String entityId, @PathVariable String grammarId, @RequestParam String sample, HttpServletRequest request, Model model, Locale locale) {
		
		AuthPojo auth = authInfoHelper.getAuth(request);
		Identifiable entity = this.getEntity(entityId);
				
		DescriptionGrammarImpl g;
		if (grammarId.equals("undefined")) {
			g = new DescriptionGrammarImpl(entityId, "");
		} else {
			g = (DescriptionGrammarImpl)grammarService.findById(grammarId);
		}
		if (g.getGrammarContainer()==null) {
			g.setGrammarContainer(new GrammarContainer());
		}

		if (sample==null) {
			model.addAttribute("elementSample", this.getSampleInputValue(entity, grammarId, request.getSession().getId(), auth.getUserId()));
		} else {
			model.addAttribute("elementSample", sample);
		}
		model.addAttribute("grammar", g);	
		model.addAttribute("readonly", this.getIsReadOnly(entity, auth.getUserId()));
		model.addAttribute("actionPath", "/schema/editor/" + entityId + "/grammar/" + grammarId + "/async/save");
		return "schemaEditor/form/grammar/edit";
		
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/form/edit")
	public String getEditForm(@PathVariable String entityId, @PathVariable String grammarId, HttpServletRequest request, Model model, Locale locale) {
		return this.getEditFormWithData(entityId, grammarId, null, request, model, locale);
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/async/save")
	public @ResponseBody ModelActionPojo saveGrammar(@Valid DescriptionGrammarImpl grammar, 
			@RequestParam(value="lexer-parser-options", defaultValue="combined") String lexerParserOption, BindingResult bindingResult, Locale locale, HttpServletRequest request) {
		ModelActionPojo result = this.getActionResult(bindingResult, locale);
		if (!result.isSuccess()) {
			return result;
		}
		if (grammar.getId().isEmpty()) {
			grammar.setId(null);
		}
		
		DescriptionGrammarImpl gSave = null;
		if (grammar.getId()!=null) {
			gSave = (DescriptionGrammarImpl)grammarService.findById(grammar.getId());
			if (gSave!=null) {
				gSave.setBaseMethod(grammar.getBaseMethod());
				gSave.setError(grammar.isError());
				gSave.setGrammarName(grammar.getGrammarName());
				gSave.setPassthrough(grammar.isPassthrough());
				gSave.setEntityId(grammar.getEntityId());
				gSave.setTemporary(grammar.isTemporary());
				gSave.setGrammarContainer(grammar.getGrammarContainer());
			}
		}
		if (gSave==null) {
			gSave = grammar;
		}

		AuthPojo auth = authInfoHelper.getAuth(request);
		DescriptionGrammar gTmp = this.getTemporaryGrammar(gSave.getId(), auth.getUserId());
		grammarService.clearGrammar(gTmp);
		
		grammarService.clearGrammar(gSave);
		grammarService.saveGrammar((DescriptionGrammarImpl)gSave, auth);
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
	public @ResponseBody ModelActionPojo uploadGrammar(@PathVariable String grammarId, @RequestParam boolean combined, @RequestParam String lexerGrammar, @RequestParam String parserGrammar, HttpServletRequest request, Locale locale) {
		ModelActionPojo result = new ModelActionPojo(false);
		if (parserGrammar==null || parserGrammar.trim().isEmpty()) {			
			result.addFieldError("grammarContainer_parserGrammar", messageSource.getMessage("~eu.dariah.de.minfba.schereg.model.grammar.validation.parser_grammar_empty", null, locale));
		}
		if (!combined && (lexerGrammar==null || lexerGrammar.trim().isEmpty())) {
			result.addFieldError("grammarContainer_lexerGrammar", messageSource.getMessage("~eu.dariah.de.minfba.schereg.model.grammar.validation.lexer_grammar_empty", null, locale));
		}
		
		if (result.getErrorCount()==0) {
			try {
				DescriptionGrammar g = getTemporaryGrammar(grammarId, authInfoHelper.getUserId(request));
				grammarService.clearGrammar(g);
				
				result.setPojo(grammarService.saveTemporaryGrammar(g, lexerGrammar, parserGrammar));
				result.setSuccess(true);
			} catch (IOException e) {
				result.addObjectError("Failed to upload grammar: " + e.getClass().getName());
			}
		}
		return result;
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/parse")
	public @ResponseBody ModelActionPojo parseGrammar(@PathVariable String grammarId, HttpServletRequest request) {
		ModelActionPojo result = new ModelActionPojo(false);
		try {
			DescriptionGrammar g = getTemporaryGrammar(grammarId, authInfoHelper.getUserId(request));
			result.setPojo(grammarService.parseTemporaryGrammar(g));
			result.setSuccess(true);
		} catch (GrammarGenerationException e) {
			for (ANTLRMessage m : e.getErrors()) {
				// TODO: Revert this
				result.addFieldError(e.getGrammarType().name(), String.format("~%s:%s => %s", m.line, m.charPosition, m.toString() /*m.getMessageTemplate(true).render()*/));
			}
		} catch (Exception e) {
			result.addObjectError("Unspecified error while parsing grammar: " + e.getClass().getName());
		}
		return result;
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/compile")
	public @ResponseBody ModelActionPojo validateGrammar(@PathVariable String grammarId, HttpServletRequest request) {
		ModelActionPojo result = new ModelActionPojo(false);
		try {
			DescriptionGrammar g = getTemporaryGrammar(grammarId, authInfoHelper.getUserId(request));
			result.setPojo(grammarService.compileTemporaryGrammar(g));
			result.setSuccess(true);
		} catch (Exception e) {
			result.addObjectError("Unspecified error while compiling grammar: " + e.getClass().getName());
		}
		return result;
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/sandbox")
	public @ResponseBody ModelActionPojo sandboxGrammar(@PathVariable String grammarId, @RequestParam String baseMethod, HttpServletRequest request, Locale locale) {
		ModelActionPojo result = new ModelActionPojo(false);
		try {
			if (baseMethod==null || baseMethod.trim().isEmpty()) {
				result.setSuccess(true);				
			} else {
				DescriptionGrammar g = getTemporaryGrammar(grammarId, authInfoHelper.getUserId(request));
				List<String> parserRules = grammarService.getParserRules(g);
				if (parserRules.contains(baseMethod.trim())) {
					result.setSuccess(true);
				} else {
					result.addFieldError("base_method", messageSource.getMessage("~eu.dariah.de.minfba.schereg.model.grammar.validation.base_rule_not_found", null, locale));
				}
			}
			
			
		} catch (Exception e) {
			result.addObjectError("Unspecified error while compiling grammar: " + e.getClass().getName());
		}
		return result;
	}
	
	
	
	
	@RequestMapping(method = RequestMethod.POST, value = "/async/parseSample")
	public @ResponseBody ModelActionPojo parseSampleInput(@PathVariable String grammarId, @RequestParam String initRule, @RequestParam String sample, @RequestParam(defaultValue="true") Boolean temporary, HttpServletRequest request, Locale locale) {
		ModelActionPojo result = new ModelActionPojo(false);
		try {
			DescriptionGrammar g;
			
			if (temporary) {
				g = getTemporaryGrammar(grammarId, authInfoHelper.getUserId(request));
			} else {
				g = grammarService.findById(grammarId);
			}
			List<String> parserRules = grammarService.getParserRules(g);
			
			if (initRule==null || initRule.trim().isEmpty()) {
				g.setBaseMethod(parserRules.get(0));
			} else {
				g.setBaseMethod(initRule);
				if (!parserRules.contains(initRule.trim())) {
					result.addObjectError(messageSource.getMessage("~eu.dariah.de.minfba.schereg.model.grammar.validation.base_rule_not_found", null, locale));
					return result;
				}
			}
			if (engine.checkAndLoadGrammar(g)!=null) {
				result.setSuccess(true);
				result.setPojo(engine.processGrammarToSVG(sample, g, new HashMap<String, TransformationParamDefinition>()));
			} else {
				// Grammar not on server yet (new or error)
				result.addObjectWarning(messageSource.getMessage("~eu.dariah.de.minfba.schereg.model.grammar.validation.no_grammar_found", null, locale));
			}
			
		} catch (Exception e) {
			logger.error("Transformation error", e);
		}
		return result;
	}
	
	private DescriptionGrammar getTemporaryGrammar(String id, String userId) {
		DescriptionGrammar g = new DescriptionGrammarImpl();
		g.setTemporary(true);
		g.setId(id);
		g.setUserId(userId);
		g.setGrammarName(g.getIdentifier());
		return g;
	}
}
