package eu.dariah.de.minfba.schereg.controller.editor;

import java.util.Locale;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.dariah.de.minfba.core.metamodel.function.TransformationFunctionImpl;
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
	
	@RequestMapping(method = RequestMethod.POST, value = "/async/save")
	public @ResponseBody ModelActionPojo saveNonterminal(@Valid TransformationFunctionImpl function, BindingResult bindingResult) {
		ModelActionPojo result = new ModelActionPojo(true); //this.getActionResult(bindingResult, locale);
		if (function.getId().isEmpty()) {
			function.setId(null);
		}
		functionService.saveFunction(function);
		return result;
	} 
}
