package de.unibamberg.minf.dme.controller.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import de.unibamberg.minf.dme.model.base.Grammar;
import de.unibamberg.minf.dme.service.interfaces.GrammarService;

public abstract class BaseApiController {
	@Autowired protected GrammarService grammarService;
	
	protected List<Grammar> getNonPassthroughGrammars(String entityId) {
		return grammarService.getNonPassthroughGrammars(entityId);
	}
}
