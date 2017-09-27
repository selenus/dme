package de.unibamberg.minf.dme.controller.api;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import de.unibamberg.minf.dme.model.grammar.GrammarContainer;
import de.unibamberg.minf.dme.service.interfaces.GrammarService;

public abstract class BaseApiController {
	@Autowired private GrammarService grammarService;
	
	protected Map<String, GrammarContainer> serializeGrammarSources(String entityId) {
		return grammarService.serializeGrammarSources(entityId);
	}
}
