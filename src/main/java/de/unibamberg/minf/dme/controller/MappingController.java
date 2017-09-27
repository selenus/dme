package de.unibamberg.minf.dme.controller;

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
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import de.unibamberg.minf.dme.controller.base.BaseScheregController;
import de.unibamberg.minf.dme.model.RightsContainer;
import de.unibamberg.minf.dme.model.datamodel.DatamodelImpl;
import de.unibamberg.minf.dme.model.datamodel.TerminalImpl;
import de.unibamberg.minf.dme.model.datamodel.base.Datamodel;
import de.unibamberg.minf.dme.model.datamodel.base.DatamodelNature;
import de.unibamberg.minf.dme.model.datamodel.natures.XmlDatamodelNature;
import de.unibamberg.minf.dme.model.mapping.MappingImpl;
import de.unibamberg.minf.dme.model.mapping.base.Mapping;
import de.unibamberg.minf.dme.pojo.AuthWrappedPojo;
import de.unibamberg.minf.dme.pojo.converter.AuthWrappedPojoConverter;
import de.unibamberg.minf.dme.service.interfaces.MappingService;
import de.unibamberg.minf.dme.service.interfaces.SchemaService;
import eu.dariah.de.dariahsp.model.web.AuthPojo;
import de.unibamberg.minf.core.web.controller.DataTableList;
import de.unibamberg.minf.core.web.pojo.MessagePojo;
import de.unibamberg.minf.core.web.pojo.ModelActionPojo;

@Controller
@RequestMapping(value="/mapping/")
public class MappingController extends BaseScheregController {
	@Autowired private AuthWrappedPojoConverter authPojoConverter;
	
	public MappingController() {
		super("mapping");
	}

	@RequestMapping(method = RequestMethod.GET, value = "/async/getData")
	public @ResponseBody DataTableList<AuthWrappedPojo<Mapping>> getData(Model model, Locale locale, HttpServletRequest request) {
		AuthPojo auth = authInfoHelper.getAuth(request);
		List<RightsContainer<Mapping>> mappings = mappingService.findAllByAuth(authInfoHelper.getAuth(request), true);
		List<AuthWrappedPojo<Mapping>> pojos = authPojoConverter.convert(mappings, auth.getUserId());	
		return new DataTableList<AuthWrappedPojo<Mapping>>(pojos);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/getData/{id}")
	public @ResponseBody AuthWrappedPojo<Mapping> getMapping(@PathVariable String id, Model model, Locale locale, HttpServletRequest request) {
		AuthPojo auth = authInfoHelper.getAuth(request);
		return authPojoConverter.convert(mappingService.findByIdAndAuth(id, auth), auth.getUserId());
	}
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method=GET, value="forms/add")
	public String getAddForm(Model model, Locale locale, HttpServletRequest request) {
		List<RightsContainer<Datamodel>> schemas = schemaService.findAllByAuth(authInfoHelper.getAuth(request));
		Mapping m = new MappingImpl();
		if (schemas.size()>1) {
			m.setSourceId(schemas.get(0).getId());
			m.setTargetId(schemas.get(1).getId());
		}
		
		model.addAttribute("mapping", m);
		model.addAttribute("schemas", schemas);
		model.addAttribute("draft", true);
		model.addAttribute("actionPath", "/mapping/async/save");
		return "mapping/form/edit";
	}
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method=GET, value="forms/edit/{id}")
	public String getEditForm(@PathVariable String id, Model model, Locale locale, HttpServletRequest request, HttpServletResponse response) {
		if (!mappingService.getUserCanWriteEntity(id, authInfoHelper.getAuth(request).getUserId())) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return null;
		}
		AuthPojo auth = authInfoHelper.getAuth(request);
		RightsContainer<Mapping> mapping = mappingService.findByIdAndAuth(id, auth);
		
		List<RightsContainer<Datamodel>> schemas = new ArrayList<RightsContainer<Datamodel>>(2);
		schemas.add(schemaService.findByIdAndAuth(mapping.getElement().getSourceId(), auth));
		schemas.add(schemaService.findByIdAndAuth(mapping.getElement().getTargetId(), auth));
		
		model.addAttribute("schemas", schemas);
		
		model.addAttribute("actionPath", "/mapping/async/save");
		model.addAttribute("draft", mapping.isDraft());
		model.addAttribute("readOnly", mapping.isReadOnly());
		model.addAttribute("mapping", mapping.getElement());
		return "mapping/form/edit";
	}
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method=POST, value="async/save")
	public @ResponseBody ModelActionPojo saveMapping(@Valid MappingImpl mapping, @RequestParam(defaultValue="false") boolean readOnly, BindingResult bindingResult, Locale locale, HttpServletRequest request, HttpServletResponse response) {
		AuthPojo auth = authInfoHelper.getAuth(request);
		if(!mappingService.getUserCanWriteEntity(mapping.getId(), auth.getUserId())) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return new ModelActionPojo(false);
		}		
		
		if (mapping.getId().isEmpty()) {
			mapping.setId(null);
		}
		
		RightsContainer<Mapping> existMapping = mappingService.findByIdAndAuth(mapping.getId(), auth); 
		Mapping saveMapping = existMapping==null ? null : existMapping.getElement();
		boolean draft = existMapping==null ? true : existMapping.isDraft();

		if (!mappingService.getUserCanWriteEntity(saveMapping==null ? mapping.getId() : saveMapping.getId(), authInfoHelper.getAuth(request).getUserId())) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return null;
		}
		
		if (saveMapping==null) {
			saveMapping = mapping;
		} else {
			saveMapping.setDescription(mapping.getDescription());
			/*saveMapping.setSourceId(mapping.getSourceId());
			saveMapping.setTargetId(mapping.getTargetId());*/
		}
		
		if (saveMapping.getSourceId().equals(saveMapping.getTargetId())) {
			bindingResult.addError(new ObjectError("mapping", new String[]{"~de.unibamberg.minf.dme.model.mapping.validation.same_source_and_target"}, null, "Source and target schema cannot be the same"));
		}
		
		ModelActionPojo result = this.getActionResult(bindingResult, locale);		
		if (bindingResult.hasErrors()) {
			return result;
		} 
		
		mappingService.saveMapping(new AuthWrappedPojo<Mapping>(saveMapping, true, false, false, draft, readOnly), auth);
		return result;
	}
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method=GET, value={"/async/delete/{id}"}, produces = "application/json; charset=utf-8")
	public @ResponseBody ModelActionPojo deleteMapping(@PathVariable String id, HttpServletRequest request, HttpServletResponse response) {
		if (!mappingService.getUserCanWriteEntity(id, authInfoHelper.getAuth(request).getUserId())) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return null;
		}
		ModelActionPojo result;
		if (id!=null && !id.isEmpty()) {
			mappingService.deleteMappingById(id, authInfoHelper.getAuth(request));
			result = new ModelActionPojo(true);
		} else {
			result = new ModelActionPojo(false);
		}		
		return result;
	}
	
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method=GET, value={"/async/publish/{id}"}, produces = "application/json; charset=utf-8")
	public @ResponseBody ModelActionPojo publishMapping(@PathVariable String id, HttpServletRequest request, HttpServletResponse response) {
		ModelActionPojo result = new ModelActionPojo(false);
		if (id!=null && !id.isEmpty()) {
			if (!mappingService.getUserCanWriteEntity(id, authInfoHelper.getAuth(request).getUserId())) {
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				return null;
			}
			AuthPojo auth = authInfoHelper.getAuth(request);			
			RightsContainer<Mapping> existMapping = mappingService.findByIdAndAuth(id, auth);
			if (existMapping!=null) {
				RightsContainer<Datamodel> source = schemaService.findByIdAndAuth(existMapping.getElement().getSourceId(), auth);
				RightsContainer<Datamodel> target = schemaService.findByIdAndAuth(existMapping.getElement().getTargetId(), auth);
				
				if (source.isDraft() || target.isDraft()) {
					result.setMessage(new MessagePojo("error", "~de.unibamberg.minf.dme.model.mapping.validation.no_pub_schema_drafts", ""));
					return result;
				}
				
				existMapping.setDraft(false);
				mappingService.saveMapping(authPojoConverter.convert(existMapping, auth.getUserId()), auth);
				result.setSuccess(true);
			}
		} 
		return result;
	}
}
