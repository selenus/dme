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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.dariah.de.minfba.core.metamodel.Nonterminal;
import eu.dariah.de.minfba.core.metamodel.function.DescriptionGrammarImpl;
import eu.dariah.de.minfba.core.metamodel.function.interfaces.DescriptionGrammar;
import eu.dariah.de.minfba.core.metamodel.function.interfaces.TransformationFunction;
import eu.dariah.de.minfba.core.metamodel.interfaces.Element;
import eu.dariah.de.minfba.core.web.pojo.ModelActionPojo;
import eu.dariah.de.minfba.schereg.service.interfaces.FunctionService;
import eu.dariah.de.minfba.schereg.service.interfaces.GrammarService;
import eu.dariah.de.minfba.schereg.service.interfaces.SchemaService;

@Controller
@RequestMapping(value="/schema/editor/{schemaId}/grammar/{grammarId}")
public class GrammarEditorController {
	@Autowired private GrammarService grammarService;
	@Autowired private FunctionService functionService;
	
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
		model.addAttribute("grammar", grammarService.findById(grammarId));
		model.addAttribute("actionPath", "/schema/editor/" + schemaId + "/grammar/" + grammarId + "/async/save");
		return "schemaEditor/form/grammar/edit";
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/async/save")
	public @ResponseBody ModelActionPojo saveNonterminal(@Valid DescriptionGrammarImpl grammar, BindingResult bindingResult) {
		ModelActionPojo result = new ModelActionPojo(true); //this.getActionResult(bindingResult, locale);
		if (grammar.getId().isEmpty()) {
			grammar.setId(null);
		}
		grammarService.saveGrammar(grammar);
		return result;
	} 
}
