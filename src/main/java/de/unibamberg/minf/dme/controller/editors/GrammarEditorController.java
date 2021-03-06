package de.unibamberg.minf.dme.controller.editors;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.antlr.v4.tool.ANTLRMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import de.unibamberg.minf.dme.controller.base.BaseFunctionController;
import de.unibamberg.minf.dme.model.PersistedSession;
import de.unibamberg.minf.dme.model.base.Grammar;
import de.unibamberg.minf.dme.model.base.Identifiable;
import de.unibamberg.minf.dme.model.function.FunctionImpl;
import de.unibamberg.minf.dme.model.grammar.GrammarContainer;
import de.unibamberg.minf.dme.model.grammar.GrammarImpl;
import de.unibamberg.minf.dme.service.interfaces.FunctionService;
import de.unibamberg.minf.dme.service.interfaces.GrammarService;
import de.unibamberg.minf.gtf.MainEngine;
import de.unibamberg.minf.gtf.compilation.GrammarGenerationException;
import de.unibamberg.minf.gtf.transformation.processing.params.TransformationParamDefinition;
import eu.dariah.de.dariahsp.model.web.AuthPojo;
import de.unibamberg.minf.core.web.pojo.ModelActionPojo;

@Controller
@RequestMapping(value={"/model/editor/{entityId}/grammar/{grammarId}", "/mapping/editor/{entityId}/grammar/{grammarId}"})
public class GrammarEditorController extends BaseFunctionController {
	
	@Autowired private GrammarService grammarService;
	@Autowired private FunctionService functionService;
	
	@Autowired private MainEngine mainEngine;
	
		
	public GrammarEditorController() {
		super("schemaEditor");
	}
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method = RequestMethod.GET, value = "/async/disable")
	public @ResponseBody ModelActionPojo disableElement(@PathVariable String entityId, @PathVariable String grammarId, @RequestParam boolean disabled, HttpServletRequest request, HttpServletResponse response) {
		if (!schemaService.getUserCanWriteEntity(entityId, authInfoHelper.getAuth(request).getUserId())) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return new ModelActionPojo(false);
		}
		
		GrammarImpl g = (GrammarImpl)grammarService.findById(grammarId);
		g.setDisabled(disabled);
		
		grammarService.saveGrammar(g, authInfoHelper.getAuth(request));
		return new ModelActionPojo(true);
	}
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method = RequestMethod.GET, value = "/async/remove")
	public @ResponseBody Grammar removeElement(@PathVariable String entityId, @PathVariable String grammarId, HttpServletRequest request, HttpServletResponse response) {
		AuthPojo auth = authInfoHelper.getAuth(request);
		if(!schemaService.getUserCanWriteEntity(entityId, auth.getUserId())) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return null;
		}
		return grammarService.deleteGrammarById(entityId, grammarId, authInfoHelper.getAuth(request));
	}
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method = RequestMethod.GET, value = "/form/new_function")
	public String getNewGrammarForm(@PathVariable String entityId, @PathVariable String grammarId, Model model, Locale locale, HttpServletRequest request, HttpServletResponse response) {
		AuthPojo auth = authInfoHelper.getAuth(request);
		if(!schemaService.getUserCanWriteEntity(entityId, auth.getUserId())) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return null;
		}
		model.addAttribute("function", new FunctionImpl(entityId, null));
		model.addAttribute("actionPath", "/model/editor/" + entityId + "/grammar/" + grammarId + "/async/saveNewFunction");
		return "functionEditor/form/new";
	}
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method = RequestMethod.POST, value = "/async/saveNewFunction")
	public @ResponseBody ModelActionPojo saveNewGrammar(@PathVariable String entityId, @PathVariable String grammarId, @Valid FunctionImpl function, BindingResult bindingResult, Locale locale, HttpServletRequest request, HttpServletResponse response) {
		AuthPojo auth = authInfoHelper.getAuth(request);
		if(!schemaService.getUserCanWriteEntity(entityId, auth.getUserId())) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return new ModelActionPojo(false);
		}
		ModelActionPojo result = this.getActionResult(bindingResult, locale);
		if (result.isSuccess()) {
			functionService.createAndAppendFunction(entityId, grammarId, function.getName(), authInfoHelper.getAuth(request));
		}
		return result;
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/get")
	public @ResponseBody Grammar getElement(@PathVariable String entityId, @PathVariable String grammarId) {
		return grammarService.findById(grammarId);
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/form/editWdata")
	public String getEditFormWithData(@PathVariable String entityId, @PathVariable String grammarId, @RequestParam String sample, HttpServletRequest request, HttpServletResponse response, Model model, Locale locale) {
		AuthPojo auth = authInfoHelper.getAuth(request);
		Identifiable entity = this.getEntity(entityId);
		
		PersistedSession s = sessionService.access(entityId, auth.getSessionId(), auth.getUserId());
		if (s==null) {
			response.setStatus(HttpServletResponse.SC_RESET_CONTENT);
			return null;
		}
				
		GrammarImpl g;
		if (grammarId.equals("undefined")) {
			g = new GrammarImpl(entityId, "");
		} else {
			g = (GrammarImpl)grammarService.findById(grammarId);
		}
		if (g.getGrammarContainer()==null) {
			g.setGrammarContainer(new GrammarContainer());
		}

		if (sample==null) {
			model.addAttribute("elementSample", this.getSampleInputValue(entity, grammarId, request.getSession().getId(), auth.getUserId(), response));
		} else {
			model.addAttribute("elementSample", sample);
		}
		model.addAttribute("grammar", g);	
		model.addAttribute("readonly", this.getIsReadOnly(entity, auth.getUserId()));
		model.addAttribute("actionPath", "/model/editor/" + entityId + "/grammar/" + grammarId + "/async/save");
		return "grammarEditor/form/edit";
		
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/form/edit")
	public String getEditForm(@PathVariable String entityId, @PathVariable String grammarId, HttpServletRequest request, HttpServletResponse response, Model model, Locale locale) {
		return this.getEditFormWithData(entityId, grammarId, null, request, response, model, locale);
	}
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method = RequestMethod.POST, value = "/async/save")
	public @ResponseBody ModelActionPojo saveGrammar(@PathVariable String entityId, @Valid GrammarImpl grammar, BindingResult bindingResult, 
			@RequestParam(value="lexer-parser-options", defaultValue="combined") String lexerParserOption, Locale locale, HttpServletRequest request, HttpServletResponse response) {
		
		AuthPojo auth = authInfoHelper.getAuth(request);
		if(!schemaService.getUserCanWriteEntity(entityId, auth.getUserId())) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return new ModelActionPojo(false);
		}
		ModelActionPojo result = this.getActionResult(bindingResult, locale);
		if (!result.isSuccess()) {
			return result;
		}
		if (grammar.getId().isEmpty()) {
			grammar.setId(null);
		}
		
		GrammarImpl gSave = null;
		if (grammar.getId()!=null) {
			gSave = (GrammarImpl)grammarService.findById(grammar.getId());
			if (gSave!=null) {
				gSave.setBaseMethod(grammar.getBaseMethod());
				gSave.setError(grammar.isError());
				gSave.setName(grammar.getName());
				gSave.setPassthrough(grammar.isPassthrough());
				gSave.setEntityId(grammar.getEntityId());
				gSave.setTemporary(grammar.isTemporary());
				gSave.setGrammarContainer(grammar.getGrammarContainer());
			}
		}
		if (gSave==null) {
			gSave = grammar;
		}

		Grammar gTmp = this.getTemporaryGrammar(gSave.getId(), auth.getUserId());
		grammarService.clearGrammar(gTmp);
		
		grammarService.clearGrammar(gSave);
		grammarService.saveGrammar((GrammarImpl)gSave, auth);
		return result;
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/processGrammarDialog")
	public String getProcessGrammarDialog(@PathVariable String grammarId, Model model, Locale locale) {
		GrammarImpl g = (GrammarImpl)grammarService.findById(grammarId);
		if (g.getGrammarContainer()==null) {
			g.setGrammarContainer(new GrammarContainer());
		}
		model.addAttribute("grammar", g);		
		return "grammarEditor/form/process";
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/help/editGrammar")
	public String showHelpEditGrammar(Model model, Locale locale) {	
		return "schemaEditor/help/grammar/editGrammar";
	}
		
	@RequestMapping(method = RequestMethod.POST, value = "/async/upload")
	public @ResponseBody ModelActionPojo uploadGrammar(@PathVariable String grammarId, @RequestParam boolean combined, @RequestParam String lexerGrammar, @RequestParam String parserGrammar, HttpServletRequest request, Locale locale) {
		ModelActionPojo result = new ModelActionPojo(false);
		if (parserGrammar==null || parserGrammar.trim().isEmpty()) {			
			result.addFieldError("grammarContainer_parserGrammar", messageSource.getMessage("~de.unibamberg.minf.dme.model.grammar.validation.parser_grammar_empty", null, locale));
		}
		if (!combined && (lexerGrammar==null || lexerGrammar.trim().isEmpty())) {
			result.addFieldError("grammarContainer_lexerGrammar", messageSource.getMessage("~de.unibamberg.minf.dme.model.grammar.validation.lexer_grammar_empty", null, locale));
		}
		
		if (result.getErrorCount()==0) {
			try {
				Grammar g = getTemporaryGrammar(grammarId, authInfoHelper.getUserId(request));
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
			Grammar g = getTemporaryGrammar(grammarId, authInfoHelper.getUserId(request));
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
			Grammar g = getTemporaryGrammar(grammarId, authInfoHelper.getUserId(request));
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
				Grammar g = getTemporaryGrammar(grammarId, authInfoHelper.getUserId(request));
				List<String> parserRules = grammarService.getParserRules(g);
				if (parserRules.contains(baseMethod.trim())) {
					result.setSuccess(true);
				} else {
					result.addFieldError("base_method", messageSource.getMessage("~de.unibamberg.minf.dme.model.grammar.validation.base_rule_not_found", null, locale));
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
			Grammar g;
			
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
					result.addObjectError(messageSource.getMessage("~de.unibamberg.minf.dme.model.grammar.validation.base_rule_not_found", null, locale));
					return result;
				}
			}
			if (mainEngine.getDescriptionEngine().checkAndLoadGrammar(g)!=null) {
				result.setSuccess(true);
				result.setPojo(mainEngine.getDescriptionEngine().processDescriptionGrammarToSVG(sample, g, new HashMap<String, TransformationParamDefinition>()));
			} else {
				// Grammar not on server yet (new or error)
				result.addObjectWarning(messageSource.getMessage("~de.unibamberg.minf.dme.model.grammar.validation.no_grammar_found", null, locale));
			}
			
		} catch (Exception e) {
			logger.error("Transformation error", e);
		}
		return result;
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/move")
	public @ResponseBody ModelActionPojo parseSampleInput(@PathVariable String entityId, @PathVariable String grammarId, @RequestParam int delta, HttpServletRequest request, HttpServletResponse response) {
		AuthPojo auth = authInfoHelper.getAuth(request);
		if(!schemaService.getUserCanWriteEntity(entityId, auth.getUserId())) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return null;
		}
		grammarService.moveGrammar(entityId, grammarId, delta, authInfoHelper.getAuth(request));
		
		return new ModelActionPojo(true);
	}
	
	private Grammar getTemporaryGrammar(String id, String userId) {
		Grammar g = new GrammarImpl();
		g.setTemporary(true);
		g.setId(id);
		g.setUserId(userId);
		g.setName(g.getIdentifier());
		return g;
	}
}
