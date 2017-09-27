package de.unibamberg.minf.dme.controller;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.util.Locale;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import de.unibamberg.minf.dme.controller.base.BaseScheregController;
import de.unibamberg.minf.core.web.controller.BaseTranslationController;

@Controller
@RequestMapping(value="/common")
public class CommonController extends BaseScheregController {
	
	public CommonController() {
		super("common");
	}

	@RequestMapping(method=GET, value={"/forms/maximizeSvg"})
	public String showParsedInputDialog(Model model, Locale locale) {	
		return "common/forms/maximizeSvg";
	}
}
