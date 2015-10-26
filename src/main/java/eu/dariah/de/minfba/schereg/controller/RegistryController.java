package eu.dariah.de.minfba.schereg.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import eu.dariah.de.minfba.core.web.controller.BaseTranslationController;

@Controller
@RequestMapping(value="/registry")
public class RegistryController extends BaseTranslationController {
	public RegistryController() {
		super("registry");
	}
	
	/* Registry has not logic on its own -> split between mapping and schema controller */
	@RequestMapping(value="", method = RequestMethod.GET)
	public String redirectRoot() {
		return "redirect:/registry/";
	}
	
	@RequestMapping(value="/", method = RequestMethod.GET)
	public String getList(Model model) {
		return "registry/home";
	}
}
