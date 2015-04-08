package eu.dariah.de.minfba.schereg.controller.editor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.dariah.de.minfba.core.metamodel.function.interfaces.TransformationFunction;
import eu.dariah.de.minfba.schereg.service.GrammarService;
import eu.dariah.de.minfba.schereg.service.SchemaService;

@Controller
@RequestMapping(value="/schema/editor/{schemaId}/function/{functionId}")
public class FunctionEditorController {
	@Autowired private SchemaService schemaService;
	@Autowired private GrammarService grammarService;
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/remove")
	public @ResponseBody TransformationFunction removeElement(@PathVariable String schemaId, @PathVariable String functionId) {
		return grammarService.deleteFunctionById(schemaId, functionId);
	}
}
