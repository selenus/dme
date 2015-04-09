package eu.dariah.de.minfba.schereg.controller.editor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.dariah.de.minfba.core.metamodel.function.interfaces.DescriptionGrammar;
import eu.dariah.de.minfba.core.metamodel.function.interfaces.TransformationFunction;
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
}
