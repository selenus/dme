package eu.dariah.de.minfba.schereg.controller.api;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import eu.dariah.de.minfba.core.metamodel.function.GrammarContainer;
import eu.dariah.de.minfba.schereg.service.interfaces.GrammarService;

public abstract class BaseApiController {
	@Autowired private GrammarService grammarService;
	
	protected Map<String, GrammarContainer> serializeGrammarSources(String entityId) {
		return grammarService.serializeGrammarSources(entityId);
	}
}
