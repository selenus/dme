package de.unibamberg.minf.dme.controller.editors;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import de.unibamberg.minf.core.web.pojo.ModelActionPojo;
import de.unibamberg.minf.dme.controller.base.BaseScheregController;
import de.unibamberg.minf.dme.model.datamodel.base.Datamodel;
import de.unibamberg.minf.dme.model.datamodel.natures.XmlDatamodelNature;
import de.unibamberg.minf.dme.model.datamodel.natures.xml.XmlNamespace;
import de.unibamberg.minf.dme.model.exception.MetamodelConsistencyException;
import eu.dariah.de.dariahsp.model.web.AuthPojo;

@Controller
@RequestMapping(value="/model/editor/{entityId}/natures")
public class NaturesEditorController extends BaseScheregController {


	public NaturesEditorController() {
		super("schemaEditor");
	}

	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method = RequestMethod.GET, value = "/form/add")
	public String getAddNatureForm(@PathVariable String entityId, Model model, HttpServletRequest request, HttpServletResponse response, Locale locale) {
		if (!schemaService.getUserCanWriteEntity(entityId, authInfoHelper.getAuth(request).getUserId())) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return null;
		}
		model.addAttribute("actionPath", "/model/editor/" + entityId + "/natures/async/add");
		model.addAttribute("natures", schemaService.getMissingNatures(entityId));
		
		return "naturesEditor/form/add";
	}
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method = RequestMethod.GET, value = "/form/edit")
	public String getEditNatureForm(@PathVariable String entityId, Model model, HttpServletRequest request, HttpServletResponse response, Locale locale) {
		AuthPojo auth = authInfoHelper.getAuth(request);
		if (!schemaService.getUserCanWriteEntity(entityId, auth.getUserId())) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return null;
		}
		// NOTE: Currently only the XML nature has options that can be edited
		Datamodel m = schemaService.findSchemaById(entityId);
		
		model.addAttribute("actionPath", "/model/editor/" + entityId + "/natures/async/updateXml");
		model.addAttribute("xmlNature", m.getNature(XmlDatamodelNature.class));
		
		return "naturesEditor/form/editXml";
	}
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method = RequestMethod.POST, value = "/async/updateXml")
	public @ResponseBody ModelActionPojo updateXmlNature(@PathVariable String entityId, @ModelAttribute XmlDatamodelNature xmlNature, HttpServletRequest request, HttpServletResponse response, Locale locale) {
		AuthPojo auth = authInfoHelper.getAuth(request);
		if (!schemaService.getUserCanWriteEntity(entityId, auth.getUserId())) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return new ModelActionPojo(false);
		}
		ModelActionPojo result;
		List<String> prefices = new ArrayList<String>();
		for (XmlNamespace xmlNs : xmlNature.getNamespaces()) {
			if (xmlNs.getPrefix()!=null && !xmlNs.getPrefix().isEmpty()) {
				if (prefices.contains(xmlNs.getPrefix())) {
					result = new ModelActionPojo(false);
					result.addFieldError("edit-nature-namespaces", messageSource.getMessage("~de.unibamberg.minf.dme.form.nature.hint.duplicate_namespace_prefices", null, locale));
					return result;
				} else {
					prefices.add(xmlNs.getPrefix());
				}
			}
		}
		schemaService.updateNature(entityId, xmlNature, auth);
		return new ModelActionPojo(true); 
	}
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method = RequestMethod.POST, value = "/async/add")
	public @ResponseBody ModelActionPojo addNewNature(@PathVariable String entityId, @RequestParam(name="n") String natureClass, @RequestParam String autocreate, @RequestParam(name="element-naming") String naming, HttpServletRequest request, HttpServletResponse response) throws ClassNotFoundException, MetamodelConsistencyException {
		AuthPojo auth = authInfoHelper.getAuth(request);
		if (!schemaService.getUserCanWriteEntity(entityId, auth.getUserId())) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return new ModelActionPojo(false);
		}
		schemaService.addNature(entityId, natureClass, auth);
		schemaService.createTerminals(entityId, natureClass, naming, auth);
		
		ModelActionPojo result = new ModelActionPojo(true); 
		result.setPojo(natureClass);
		return result; 
	}
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method = RequestMethod.GET, value = "/async/remove")
	public @ResponseBody ModelActionPojo removeNature(@PathVariable String entityId, @RequestParam(name="n") String natureClass, HttpServletRequest request, HttpServletResponse response) {
		AuthPojo auth = authInfoHelper.getAuth(request);
		if (!schemaService.getUserCanWriteEntity(entityId, auth.getUserId())) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return new ModelActionPojo(false);
		}
		schemaService.removeNature(entityId, natureClass, auth);	
		ModelActionPojo result = new ModelActionPojo(true); 
		result.setPojo(natureClass);
		return result; 
	}
	
}
