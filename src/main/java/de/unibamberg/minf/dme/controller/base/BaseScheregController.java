package de.unibamberg.minf.dme.controller.base;

import java.util.Collection;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import de.unibamberg.minf.dme.model.base.Identifiable;
import de.unibamberg.minf.dme.pojo.ChangeSetPojo;
import de.unibamberg.minf.dme.pojo.converter.ChangeSetPojoConverter;
import de.unibamberg.minf.dme.service.interfaces.MappingService;
import de.unibamberg.minf.dme.service.interfaces.SchemaService;
import eu.dariah.de.dariahsp.web.AuthInfoHelper;
import de.unibamberg.minf.core.web.controller.BaseTranslationController;

public abstract class BaseScheregController extends BaseTranslationController {

	@Autowired protected AuthInfoHelper authInfoHelper;
	@Autowired protected SchemaService schemaService;
	@Autowired protected MappingService mappingService;
	
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
	
	protected Identifiable getEntity(String entityId) {
		Identifiable entity = mappingService.findMappingById(entityId);
		if (entity==null) {
			entity = schemaService.findSchemaById(entityId);
		}
		return entity;
	}
	
	protected String getLimitedString(String string, int limit) {
		if (string==null || string.trim().length()<limit) {
			return string;
		} else {
			return string.substring(0, limit-3) + "...";
		}
	}
}
