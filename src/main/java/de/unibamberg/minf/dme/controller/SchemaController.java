package de.unibamberg.minf.dme.controller;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.IOException;
import java.util.Collection;
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
import org.springframework.web.bind.annotation.RequestAttribute;
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
import de.unibamberg.minf.dme.model.tracking.ChangeSet;
import de.unibamberg.minf.dme.pojo.AuthWrappedPojo;
import de.unibamberg.minf.dme.pojo.ChangeSetPojo;
import de.unibamberg.minf.dme.pojo.converter.AuthWrappedPojoConverter;
import de.unibamberg.minf.dme.pojo.converter.ChangeSetPojoConverter;
import de.unibamberg.minf.dme.service.interfaces.SchemaService;
import eu.dariah.de.dariahsp.model.web.AuthPojo;
import de.unibamberg.minf.core.web.controller.DataTableList;
import de.unibamberg.minf.core.web.pojo.ModelActionPojo;

@Controller
@RequestMapping(value="/model")
public class SchemaController extends BaseScheregController {
	@Autowired private AuthWrappedPojoConverter authPojoConverter;
	
	public SchemaController() {
		super("schema");
	}

	
	@RequestMapping(value = "/editor", method = RequestMethod.GET)
	public String getEditorRedirect(HttpServletResponse response) throws IOException {
		// Editor called without an id...
		response.sendRedirect("../");
		return null;
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/getData")
	public @ResponseBody DataTableList<AuthWrappedPojo<Datamodel>> getData(Model model, Locale locale, HttpServletRequest request) {
		AuthPojo auth = authInfoHelper.getAuth(request);
		List<RightsContainer<Datamodel>> schemas = schemaService.findAllByAuth(authInfoHelper.getAuth(request));
		List<AuthWrappedPojo<Datamodel>> pojos = authPojoConverter.convert(schemas, auth.getUserId());	
		return new DataTableList<AuthWrappedPojo<Datamodel>>(pojos);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/getData/{id}")
	public @ResponseBody AuthWrappedPojo<Datamodel> getSchema(@PathVariable String id, Model model, Locale locale, HttpServletRequest request) {
		AuthPojo auth = authInfoHelper.getAuth(request);
		return authPojoConverter.convert(schemaService.findByIdAndAuth(id, auth), auth.getUserId());
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/getElements/{id}")
	public @ResponseBody Datamodel getElements(@PathVariable String id, Model model, Locale locale) {
		return schemaService.findSchemaById(id);
	}
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method=GET, value={"/forms/add"})
	public String getAddForm(Model model, Locale locale) {		
		model.addAttribute("datamodelImpl", new DatamodelImpl());
		model.addAttribute("actionPath", "/model/async/save");
		return "schema/form/edit";
	}
		
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method=POST, value={"/async/save"}, produces = "application/json; charset=utf-8")
	public @ResponseBody ModelActionPojo saveSchema(@Valid DatamodelImpl datamodelImpl, BindingResult bindingResult, @RequestParam String currentId, @RequestParam(defaultValue="false") boolean readOnly, Locale locale, HttpServletRequest request, HttpServletResponse response) throws IOException {
		AuthPojo auth = authInfoHelper.getAuth(request);
		if(!schemaService.getUserCanWriteEntity(currentId, auth.getUserId())) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return new ModelActionPojo(false);
		}
		
		ModelActionPojo result = this.getActionResult(bindingResult, locale);
		if (!result.isSuccess()) {
			return result;
		} else if (currentId.isEmpty()) {
			datamodelImpl.setId(null);
		}
		
		RightsContainer<Datamodel> existSchema = schemaService.findByIdAndAuth(currentId, auth); 
		Datamodel saveSchema = existSchema==null ? null : existSchema.getElement();
		boolean draft = existSchema==null ? true : existSchema.isDraft();
		
		if (saveSchema==null) {
			saveSchema = datamodelImpl;
		} else {
			saveSchema.setName(datamodelImpl.getName());
			saveSchema.setDescription(datamodelImpl.getDescription());
		}
		
		schemaService.saveSchema(new AuthWrappedPojo<Datamodel>(saveSchema, true, false, false, draft, readOnly), auth);
		
		if (!currentId.equals(datamodelImpl.getId())) {
			if (!schemaService.changeId(currentId, datamodelImpl.getId())) {
				result.setSuccess(false);
				result.addObjectError("~Labamba");
			}
		}
		
		return result;
	}
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method=GET, value={"/async/delete/{id}"}, produces = "application/json; charset=utf-8")
	public @ResponseBody ModelActionPojo deleteSchema(@PathVariable String id, HttpServletRequest request, HttpServletResponse response) {
		AuthPojo auth = authInfoHelper.getAuth(request);		
		if(!schemaService.getUserCanWriteEntity(id, auth.getUserId())) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return new ModelActionPojo(false);
		}
		ModelActionPojo result;
		if (id!=null && !id.isEmpty()) {
			schemaService.deleteSchemaById(id, auth);
			result = new ModelActionPojo(true);
		} else {
			result = new ModelActionPojo(false);
		}		
		return result;
	}
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method=GET, value={"/async/publish/{id}"}, produces = "application/json; charset=utf-8")
	public @ResponseBody ModelActionPojo publishSchema(@PathVariable String id, HttpServletRequest request, HttpServletResponse response) {
		ModelActionPojo result = new ModelActionPojo(false);
		if (id!=null && !id.isEmpty()) {
			AuthPojo auth = authInfoHelper.getAuth(request);		
			if(!schemaService.getUserCanWriteEntity(id, auth.getUserId())) {
				response.setStatus(HttpServletResponse.SC_FORBIDDEN);
				return new ModelActionPojo(false);
			}			
			RightsContainer<Datamodel> existSchema = schemaService.findByIdAndAuth(id, auth);
			if (existSchema!=null) {
				existSchema.setDraft(false);
				schemaService.saveSchema(authPojoConverter.convert(existSchema, auth.getUserId()), auth);
				result.setSuccess(true);
			}
		} 
		return result;
	}
}