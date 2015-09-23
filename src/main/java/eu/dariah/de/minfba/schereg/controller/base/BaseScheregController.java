package eu.dariah.de.minfba.schereg.controller.base;

import java.util.Collection;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import de.dariah.aai.javasp.web.helper.AuthInfoHelper;
import eu.dariah.de.minfba.core.web.controller.BaseTranslationController;
import eu.dariah.de.minfba.schereg.pojo.ChangeSetPojo;
import eu.dariah.de.minfba.schereg.pojo.converter.ChangeSetPojoConverter;
import eu.dariah.de.minfba.schereg.service.interfaces.SchemaService;

public abstract class BaseScheregController extends BaseTranslationController {

	@Autowired protected AuthInfoHelper authInfoHelper;
	@Autowired private SchemaService schemaService;
	@Autowired private ChangeSetPojoConverter changeSetPojoConverter;
	
	
	public BaseScheregController(String mainNavId) {
		super(mainNavId);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/getChangesForElement/{id}")
	public @ResponseBody Collection<ChangeSetPojo> getChangesForElement(@PathVariable String id, Model model, Locale locale) {
		// Actually any service loads any change set as long as ids are matching
		return changeSetPojoConverter.convert(schemaService.getChangeSetForElement(id));
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/getChangesForEntity/{id}")
	public @ResponseBody Collection<ChangeSetPojo> getChangesForEntity(@PathVariable String id, Model model, Locale locale) {
		// Actually any service loads any change set as long as ids are matching
		return changeSetPojoConverter.convert(schemaService.getChangeSetForEntity(id));
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/getChanges")
	public @ResponseBody Collection<ChangeSetPojo> getChanges(Model model, Locale locale) {
		return changeSetPojoConverter.convert(schemaService.getChangeSetForAllSchemas());
	}
	
}
