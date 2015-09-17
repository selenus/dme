package eu.dariah.de.minfba.schereg.controller.base;

import org.springframework.beans.factory.annotation.Autowired;

import de.dariah.aai.javasp.web.helper.AuthInfoHelper;
import eu.dariah.de.minfba.core.web.controller.BaseTranslationController;

public abstract class BaseScheregController extends BaseTranslationController {

	@Autowired protected AuthInfoHelper authInfoHelper;
	
	public BaseScheregController(String mainNavId) {
		super(mainNavId);
	}
}
