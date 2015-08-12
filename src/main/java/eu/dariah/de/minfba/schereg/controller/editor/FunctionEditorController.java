package eu.dariah.de.minfba.schereg.controller.editor;

import java.util.List;
import java.util.Locale;

import javax.validation.Valid;

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
import de.unibamberg.minf.gtf.exception.DataTransformationException;
import de.unibamberg.minf.gtf.exception.GrammarProcessingException;
import de.unibamberg.minf.gtf.transformation.CompiledTransformationFunction;
import de.unibamberg.minf.gtf.transformation.processing.params.OutputParam;
import eu.dariah.de.minfba.core.metamodel.function.DescriptionGrammarImpl;
import eu.dariah.de.minfba.core.metamodel.function.TransformationFunctionImpl;
import eu.dariah.de.minfba.core.metamodel.function.interfaces.DescriptionGrammar;
import eu.dariah.de.minfba.core.metamodel.function.interfaces.TransformationFunction;
import eu.dariah.de.minfba.core.web.pojo.ModelActionPojo;
import eu.dariah.de.minfba.schereg.service.interfaces.FunctionService;
import eu.dariah.de.minfba.schereg.service.interfaces.GrammarService;
import eu.dariah.de.minfba.schereg.service.interfaces.ReferenceService;

@Controller
@RequestMapping(value="/schema/editor/{schemaId}/function/{functionId}")
public class FunctionEditorController {
	@Autowired private ReferenceService referenceService;
	@Autowired private FunctionService functionService;
	@Autowired private GrammarService grammarService;
	@Autowired private TransformationEngine engine;
	
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/remove")
	public @ResponseBody TransformationFunction removeElement(@PathVariable String schemaId, @PathVariable String functionId) {
		return functionService.deleteFunctionById(schemaId, functionId);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/get")
	public @ResponseBody TransformationFunction getElement(@PathVariable String schemaId, @PathVariable String functionId) {
		return functionService.findById(functionId);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/form/edit")
	public String getEditForm(@PathVariable String schemaId, @PathVariable String functionId, Model model, Locale locale) {
		String grammarId = referenceService.findReferenceBySchemaAndChildId(schemaId, functionId).getId();
		
		model.addAttribute("grammar", grammarService.findById(grammarId));
		model.addAttribute("function", functionService.findById(functionId));
		model.addAttribute("actionPath", "/schema/editor/" + schemaId + "/function/" + functionId + "/async/save");
		return "schemaEditor/form/function/edit";
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/process")
	public String validateFunction(Model model, Locale locale) {		
		return "schemaEditor/form/function/process";
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/async/validate")
	public @ResponseBody ModelActionPojo validateFunction(@PathVariable String schemaId, @PathVariable String functionId, @RequestParam String func) {
		ModelActionPojo result = new ModelActionPojo();
		
		try {
			TransformationFunctionImpl f = new TransformationFunctionImpl(schemaId, functionId);
			f.setFunction(func);
			
			CompiledTransformationFunction fCompiled = engine.compileOutputFunction(f, true);
			
			result.setPojo(fCompiled.getSvg());
			
			result.setSuccess(true);
		} catch (Exception e) {	}
		return result;
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/async/save")
	public @ResponseBody ModelActionPojo saveNonterminal(@Valid TransformationFunctionImpl function, BindingResult bindingResult) {
		ModelActionPojo result = new ModelActionPojo(true); //this.getActionResult(bindingResult, locale);
		if (function.getId().isEmpty()) {
			function.setId(null);
		}
		functionService.saveFunction(function);
		return result;
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/async/parseSample")
	public @ResponseBody ModelActionPojo parseSampleInput(@PathVariable String schemaId, @PathVariable String functionId, @RequestParam String func, @RequestParam String sample) {
		String grammarId = referenceService.findReferenceBySchemaAndChildId(schemaId, functionId).getId();
		DescriptionGrammar g = grammarService.findById(grammarId);
		
		TransformationFunction f = new TransformationFunctionImpl(schemaId, functionId);
		f.setFunction(func);
		
		ModelActionPojo result = new ModelActionPojo();
		
		try {
			engine.checkGrammar(g);
			
			List<OutputParam> pResult = engine.process(sample, g, f);
			result.setSuccess(true);
			result.setPojo(pResult);
		} catch (GrammarProcessingException | DataTransformationException e) {
			e.printStackTrace();
		}
		
		
		return result;
	}
}
