package de.unibamberg.minf.dme.controller.editors;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import de.unibamberg.minf.dme.controller.base.BaseScheregController;
import de.unibamberg.minf.dme.model.base.Element;
import de.unibamberg.minf.dme.model.base.Nonterminal;
import de.unibamberg.minf.dme.model.base.Terminal;
import de.unibamberg.minf.dme.model.datamodel.base.Datamodel;
import de.unibamberg.minf.dme.model.datamodel.base.DatamodelNature;
import de.unibamberg.minf.dme.model.datamodel.natures.XmlDatamodelNature;
import de.unibamberg.minf.dme.model.datamodel.natures.xml.XmlNamespace;
import de.unibamberg.minf.dme.model.datamodel.natures.xml.XmlTerminal;
import de.unibamberg.minf.dme.model.tracking.ChangeType;
import de.unibamberg.minf.dme.service.interfaces.ElementService;
import de.unibamberg.minf.dme.service.interfaces.SchemaService;
import eu.dariah.de.dariahsp.model.web.AuthPojo;
import de.unibamberg.minf.core.web.pojo.ModelActionPojo;

@Controller
@RequestMapping(value="/model/editor/{entityId}/terminal")
public class TerminalEditorController extends BaseScheregController {
	@Autowired private ElementService elementService;
	
	public TerminalEditorController() {
		super("schemaEditor");
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/missing/{nonterminalId}/async/get")
	public @ResponseBody Terminal getMissingTerminal(@PathVariable String entityId, @PathVariable String nonterminalId) throws Exception {
		XmlTerminal t = new XmlTerminal();
		t.setId(nonterminalId);
		t.setName("?");
		t.setNamespace("?");
		return t;
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/{terminalId}/async/get")
	public @ResponseBody Terminal getTerminal(@PathVariable String entityId, @PathVariable String terminalId, 
			@RequestParam(name="n") String modelClass, HttpServletRequest request) throws Exception {
		AuthPojo auth = authInfoHelper.getAuth(request);
		@SuppressWarnings("unchecked")
		Class<? extends DatamodelNature> modelClazz = (Class<? extends DatamodelNature>)Class.forName(modelClass);
		DatamodelNature n = schemaService.findByIdAndAuth(entityId, auth).getElement().getNature(modelClazz);
		for (Terminal t : n.getTerminals()) {
			if (t.getId().equals(terminalId)) {
				return t;
			}
		}
		return null;
	}
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method = RequestMethod.GET, value = "/{terminalId}/async/remove")
	public @ResponseBody Terminal removeElement(@PathVariable String entityId, @PathVariable String terminalId, HttpServletRequest request, HttpServletResponse response) {
		AuthPojo auth = authInfoHelper.getAuth(request);
		if (!schemaService.getUserCanWriteEntity(entityId, auth.getUserId())) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return null;
		}
		return elementService.removeTerminal(entityId, terminalId, authInfoHelper.getAuth(request));
	}
	
	@RequestMapping(method = RequestMethod.GET, value = {"/{terminalId}/form/edit", "/missing/{nonterminalId}/form/edit"})
	public String getElement(@PathVariable String entityId, @PathVariable(required=false) String terminalId, @PathVariable(required=false) String nonterminalId, 
			Model model, Locale locale, @RequestParam(name="n") String modelClass, HttpServletRequest request) throws ClassNotFoundException {
		AuthPojo auth = authInfoHelper.getAuth(request);
		if (!schemaService.getUserCanWriteEntity(entityId, auth.getUserId())) {
			model.addAttribute("readonly", true);
		} else {
			model.addAttribute("readonly", false);
		}
		
		@SuppressWarnings("unchecked")
		Class<? extends DatamodelNature> modelClazz = (Class<? extends DatamodelNature>)Class.forName(modelClass);
		DatamodelNature n = schemaService.findByIdAndAuth(entityId, auth).getElement().getNature(modelClazz);
		
		if (terminalId!=null) {
			for (Terminal t : n.getTerminals()) {
				if (t.getId().equals(terminalId)) {
					model.addAttribute("terminal", t);
				}
			}
		} else if (nonterminalId!=null) {
			// TODO Also for the others not only XML
		}
				
		List<String> availableNamespaces = new ArrayList<String>();
		if (n instanceof XmlDatamodelNature) {	
			if (((XmlDatamodelNature)n).getNamespaces()!=null) {
				for (XmlNamespace ns : ((XmlDatamodelNature)n).getNamespaces()) {
					availableNamespaces.add(ns.getUrl());
				}
			}
		}
		model.addAttribute("availableNamespaces", availableNamespaces);
		
		if (terminalId!=null) {
			model.addAttribute("actionPath", "/model/editor/" + entityId + "/terminal/" + terminalId + "/async/save");
		} else {
			model.addAttribute("actionPath", "/model/editor/" + entityId + "/terminal/" + nonterminalId + "/async/append");
		}
		return "elementEditor/form/edit_terminal";
	}
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method = RequestMethod.POST, value = {"/{terminalId}/async/save", "/{nonterminalId}/async/append"})
	public @ResponseBody ModelActionPojo saveTerminal(@PathVariable String entityId, @Valid XmlTerminal element, BindingResult bindingResult, Locale locale, HttpServletRequest request, HttpServletResponse response) {
		AuthPojo auth = authInfoHelper.getAuth(request);
		if (!schemaService.getUserCanWriteEntity(entityId, authInfoHelper.getAuth(request).getUserId())) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return null;
		}
		ModelActionPojo result = this.getActionResult(bindingResult, locale);
		if (result.isSuccess()) {	
			if (element.getId().isEmpty() || element.getId().trim().equals("-1")) {
				element.setId(null);
			}
			
			Datamodel s = schemaService.findSchemaById(entityId);
			XmlDatamodelNature xmlSchemaNature = s.getNature(XmlDatamodelNature.class);
					
			if (xmlSchemaNature.getTerminals()!=null) {
				for (int i=0; i<xmlSchemaNature.getTerminals().size(); i++) {
					XmlTerminal t = xmlSchemaNature.getTerminals().get(i);
					if (t.getName().equals(element.getName()) && t.getNamespace().equals(element.getNamespace()) &&
							!t.getId().equals(element.getId())) {
						result.setSuccess(false);
						result.setObjectErrors(new ArrayList<String>());
						result.getObjectErrors().add("~Duplicate");
						return result;
					}
				}
			}
			if (xmlSchemaNature.getTerminals()==null) {
				xmlSchemaNature.setTerminals(new ArrayList<XmlTerminal>());
			}
			if (element.getId()==null) {
				element.setId(new ObjectId().toString());
				element.addChange(ChangeType.NEW_OBJECT, "terminal", null, element.getId());
				xmlSchemaNature.getTerminals().add(element);
			} else {
				for (int i=0; i<xmlSchemaNature.getTerminals().size(); i++) {
					if (xmlSchemaNature.getTerminals().get(i).getId().equals(element.getId())) {
						xmlSchemaNature.getTerminals().set(i, element);
						break;
					}
				}
			}
			
			schemaService.saveSchema(s, authInfoHelper.getAuth(request));			
		}
		return result;
	}
}