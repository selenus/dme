package eu.dariah.de.minfba.schereg.controller.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import eu.dariah.de.minfba.core.metamodel.function.DescriptionGrammarImpl;
import eu.dariah.de.minfba.core.metamodel.function.GrammarContainer;
import eu.dariah.de.minfba.core.metamodel.function.interfaces.DescriptionGrammar;
import eu.dariah.de.minfba.schereg.service.interfaces.GrammarService;

public abstract class BaseApiController {
	@Autowired private GrammarService grammarService;
	
	protected Map<String, GrammarContainer> serializeGrammarSources(String entityId) {
		Map<String, GrammarContainer> containers = new HashMap<String, GrammarContainer>();
		List<DescriptionGrammar> grammars = grammarService.findByEntityId(entityId, true);

		if (grammars!=null) {
			for (DescriptionGrammar g : grammars) {
				if (g.isPassthrough() || g.isError() || g.isTemporary()) {
					continue;
				}
				
				if (DescriptionGrammarImpl.class.isAssignableFrom(g.getClass())) {
					containers.put(g.getId(), ((DescriptionGrammarImpl)g).getGrammarContainer());
				}
			}
		}
		return containers;
	}
}
