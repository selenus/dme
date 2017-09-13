package eu.dariah.de.minfba.schereg.controller.editors;

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
import org.springframework.web.bind.annotation.ResponseBody;

import de.unibamberg.minf.dme.model.base.Terminal;
import de.unibamberg.minf.dme.model.datamodel.base.Datamodel;
import de.unibamberg.minf.dme.model.datamodel.base.DatamodelNature;
import de.unibamberg.minf.dme.model.datamodel.natures.XmlDatamodelNature;
import de.unibamberg.minf.dme.model.datamodel.natures.xml.XmlNamespace;
import de.unibamberg.minf.dme.model.datamodel.natures.xml.XmlTerminal;
import de.unibamberg.minf.dme.model.tracking.ChangeType;
import eu.dariah.de.dariahsp.model.web.AuthPojo;
import eu.dariah.de.minfba.core.web.pojo.ModelActionPojo;
import eu.dariah.de.minfba.schereg.controller.base.BaseScheregController;
import eu.dariah.de.minfba.schereg.service.interfaces.ElementService;
import eu.dariah.de.minfba.schereg.service.interfaces.SchemaService;

@Controller
@RequestMapping(value="/model/editor/{schemaId}/terminal/{terminalId}")
public class TerminalEditorController extends BaseScheregController {
	@Autowired private ElementService elementService;
	
	public TerminalEditorController() {
		super("schemaEditor");
	}
	
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method = RequestMethod.GET, value = "/async/remove")
	public @ResponseBody Terminal removeElement(@PathVariable String schemaId, @PathVariable String terminalId, HttpServletRequest request, HttpServletResponse response) {
		AuthPojo auth = authInfoHelper.getAuth(request);
		if (!schemaService.getUserCanWriteEntity(schemaId, authInfoHelper.getAuth(request).getUserId())) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return null;
		}
		return elementService.removeTerminal(schemaId, terminalId, authInfoHelper.getAuth(request));
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/form/edit")
	public String getElement(@PathVariable String schemaId, @PathVariable String terminalId, Model model, Locale locale, HttpServletRequest request) {
		if (!schemaService.getUserCanWriteEntity(schemaId, authInfoHelper.getAuth(request).getUserId())) {
			model.addAttribute("readonly", true);
		} else {
			model.addAttribute("readonly", false);
		}
		
		Datamodel s = schemaService.findSchemaById(schemaId);
		XmlDatamodelNature xmlSchemaNature = s.getNature(XmlDatamodelNature.class);
		
		XmlTerminal terminal = null;
		if (terminalId != "-1") {
			if (xmlSchemaNature.getTerminals()!=null) {
				for (Terminal t : xmlSchemaNature.getTerminals()) {
					if (t.getId().equals(terminalId)) {
						terminal = (XmlTerminal)t;
						break;
					}
				}
			}
		} 
		if (terminal==null) {
			terminal = new XmlTerminal();
			terminal.setId("-1");
		}
		model.addAttribute("terminal", terminal);
		
		
		List<String> availableNamespaces = new ArrayList<String>();
		if (s instanceof XmlDatamodelNature) {	
			if (((XmlDatamodelNature)s).getNamespaces()!=null) {
				for (XmlNamespace ns : ((XmlDatamodelNature)s).getNamespaces()) {
					availableNamespaces.add(ns.getUrl());
				}
			}
		}
		model.addAttribute("availableNamespaces", availableNamespaces);
		model.addAttribute("actionPath", "/model/editor/" + schemaId + "/terminal/" + terminal.getId() + "/async/saveTerminal");
		return "elementEditor/form/edit_terminal";
	}
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method = RequestMethod.POST, value = "/async/saveTerminal")
	public @ResponseBody ModelActionPojo saveTerminal(@PathVariable String schemaId, @Valid XmlTerminal element, BindingResult bindingResult, Locale locale, HttpServletRequest request, HttpServletResponse response) {
		AuthPojo auth = authInfoHelper.getAuth(request);
		if (!schemaService.getUserCanWriteEntity(schemaId, authInfoHelper.getAuth(request).getUserId())) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return null;
		}
		ModelActionPojo result = this.getActionResult(bindingResult, locale);
		if (result.isSuccess()) {	
			if (element.getId().isEmpty() || element.getId().trim().equals("-1")) {
				element.setId(null);
			}
			
			Datamodel s = schemaService.findSchemaById(schemaId);
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