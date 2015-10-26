package eu.dariah.de.minfba.schereg.controller;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import de.dariah.samlsp.model.pojo.AuthPojo;
import eu.dariah.de.minfba.core.metamodel.BaseSchema;
import eu.dariah.de.minfba.core.metamodel.BaseTerminal;
import eu.dariah.de.minfba.core.metamodel.interfaces.Mapping;
import eu.dariah.de.minfba.core.metamodel.interfaces.Schema;
import eu.dariah.de.minfba.core.metamodel.mapping.MappingImpl;
import eu.dariah.de.minfba.core.metamodel.xml.XmlSchema;
import eu.dariah.de.minfba.core.web.controller.DataTableList;
import eu.dariah.de.minfba.core.web.pojo.ModelActionPojo;
import eu.dariah.de.minfba.schereg.controller.base.BaseScheregController;
import eu.dariah.de.minfba.schereg.model.RightsContainer;
import eu.dariah.de.minfba.schereg.pojo.AuthWrappedPojo;
import eu.dariah.de.minfba.schereg.pojo.converter.AuthWrappedPojoConverter;
import eu.dariah.de.minfba.schereg.service.interfaces.MappingService;
import eu.dariah.de.minfba.schereg.service.interfaces.SchemaService;

@Controller
@RequestMapping(value="/mapping/")
public class MappingController extends BaseScheregController {
	@Autowired private MappingService mappingService;
	@Autowired private SchemaService schemaService;
	@Autowired private AuthWrappedPojoConverter authPojoConverter;
	
	public MappingController() {
		super("mapping");
	}

	@RequestMapping(value = "", method = RequestMethod.GET)
	public String getList(Model model) {
		return "mapping/home";
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/getData")
	public @ResponseBody DataTableList<AuthWrappedPojo<Mapping>> getData(Model model, Locale locale, HttpServletRequest request) {
		AuthPojo auth = authInfoHelper.getAuth(request);
		List<RightsContainer<Mapping>> mappings = mappingService.findAllByAuth(authInfoHelper.getAuth(request));
		List<AuthWrappedPojo<Mapping>> pojos = authPojoConverter.convert(mappings, auth.getUserId());	
		return new DataTableList<AuthWrappedPojo<Mapping>>(pojos);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/getData/{id}")
	public @ResponseBody AuthWrappedPojo<Mapping> getMapping(@PathVariable String id, Model model, Locale locale, HttpServletRequest request) {
		AuthPojo auth = authInfoHelper.getAuth(request);
		return authPojoConverter.convert(mappingService.findByIdAndAuth(id, auth), auth.getUserId());
	}
	
	@Secured("IS_AUTHENTICATED_FULLY")
	@RequestMapping(method=GET, value="forms/add")
	public String getAddForm(Model model, Locale locale, HttpServletRequest request) {
		model.addAttribute("mapping", new MappingImpl());
		model.addAttribute("schemas", schemaService.findAllByAuth(authInfoHelper.getAuth(request)));
		model.addAttribute("draft", true);
		model.addAttribute("actionPath", "async/save");
		return "mapping/form/edit";
	}
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method=GET, value="forms/edit/{id}")
	public String getEditForm(@PathVariable String id, Model model, Locale locale, HttpServletRequest request) {
		AuthPojo auth = authInfoHelper.getAuth(request);
		RightsContainer<Mapping> mapping = mappingService.findByIdAndAuth(id, auth);
		
		List<RightsContainer<Schema>> schemas = new ArrayList<RightsContainer<Schema>>(2);
		schemas.add(schemaService.findByIdAndAuth(mapping.getElement().getSourceId(), auth));
		schemas.add(schemaService.findByIdAndAuth(mapping.getElement().getTargetId(), auth));
		
		model.addAttribute("schemas", schemas);
		
		model.addAttribute("actionPath", "async/save");
		model.addAttribute("draft", mapping.isDraft());
		model.addAttribute("mapping", mapping.getElement());
		return "mapping/form/edit";
	}
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method=POST, value="async/save")
	public @ResponseBody ModelActionPojo saveMapping(@Valid MappingImpl mapping, @RequestParam(defaultValue="false") boolean draft, BindingResult bindingResult, Locale locale, HttpServletRequest request, HttpServletResponse response) {
		AuthPojo auth = authInfoHelper.getAuth(request);
		if(!mappingService.getHasWriteAccess(mapping.getId(), auth.getUserId())) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return new ModelActionPojo(false);
		}
		
		ModelActionPojo result = this.getActionResult(bindingResult, locale);
		if (mapping.getId().isEmpty()) {
			mapping.setId(null);
		}
		Mapping saveMapping = mappingService.findMappingById(mapping.getId());
		if (saveMapping==null) {
			saveMapping = mapping;
		} else {
			saveMapping.setLabel(mapping.getLabel());
			saveMapping.setDescription(mapping.getDescription());
		}
		mappingService.saveMapping(new AuthWrappedPojo<Mapping>(saveMapping, true, false, false, draft), auth);
		return result;
	}
}
