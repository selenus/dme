package eu.dariah.de.minfba.schereg.controller.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import eu.dariah.de.minfba.core.metamodel.function.interfaces.DescriptionGrammar;
import eu.dariah.de.minfba.schereg.service.interfaces.GrammarService;

public abstract class BaseApiController {
	@Autowired private GrammarService grammarService;
	
	protected Map<String, DescriptionGrammar> serializeGrammarSources(String entityId) {
		Map<String, DescriptionGrammar> containers = new HashMap<String, DescriptionGrammar>();
		List<DescriptionGrammar> grammars = grammarService.findByEntityId(entityId, true);

		if (grammars!=null) {
			for (DescriptionGrammar g : grammars) {
				containers.put(g.getId(), g);
			}
		}
		return containers;
	}
}
