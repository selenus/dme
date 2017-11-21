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
import de.unibamberg.minf.dme.dao.base.BaseDaoImpl;
import de.unibamberg.minf.dme.model.base.Terminal;
import de.unibamberg.minf.dme.model.datamodel.TerminalImpl;
import de.unibamberg.minf.dme.model.datamodel.base.Datamodel;
import de.unibamberg.minf.dme.model.datamodel.base.DatamodelNature;
import de.unibamberg.minf.dme.model.datamodel.natures.XmlDatamodelNature;
import de.unibamberg.minf.dme.model.datamodel.natures.xml.XmlNamespace;
import de.unibamberg.minf.dme.model.datamodel.natures.xml.XmlTerminal;
import de.unibamberg.minf.dme.model.exception.MetamodelConsistencyException;
import de.unibamberg.minf.dme.model.tracking.ChangeType;
import de.unibamberg.minf.dme.service.interfaces.ElementService;
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
			if (modelClazz.equals(XmlDatamodelNature.class)) {
				model.addAttribute("terminal", new XmlTerminal());
			} else {
				model.addAttribute("terminal", new TerminalImpl());
			}
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
		model.addAttribute("natureType", modelClass);
		
		if (terminalId!=null) {
			model.addAttribute("actionPath", "/model/editor/" + entityId + "/terminal/" + terminalId + "/async/save");
		} else {
			model.addAttribute("actionPath", "/model/editor/" + entityId + "/terminal/" + nonterminalId + "/async/append");
		}
		
		if (modelClazz.equals(XmlDatamodelNature.class)) {
			return "elementEditor/form/edit_xml_terminal";
		} else {
			return "elementEditor/form/edit_terminal";
		}
		
	}
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method = RequestMethod.POST, value = {"/{terminalId}/async/save", "/{nonterminalId}/async/append"})
	public @ResponseBody ModelActionPojo saveTerminal(@PathVariable String entityId, @PathVariable(required=false) String terminalId, 
			@PathVariable(required=false) String nonterminalId, @RequestParam(defaultValue="false") boolean attribute, 
			@RequestParam String name, @RequestParam(required=false) String namespace, @RequestParam(name="natureType") String natureType, Locale locale, 
			HttpServletRequest request, HttpServletResponse response) throws ClassNotFoundException, MetamodelConsistencyException {
		AuthPojo auth = authInfoHelper.getAuth(request);
		if (!schemaService.getUserCanWriteEntity(entityId, authInfoHelper.getAuth(request).getUserId())) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return null;
		}
		
		@SuppressWarnings("unchecked")
		Class<? extends DatamodelNature> modelClazz = (Class<? extends DatamodelNature>)Class.forName(natureType);
		Datamodel m = schemaService.findByIdAndAuth(entityId, auth).getElement();
		DatamodelNature n = m.getNature(modelClazz);
				
		// Update existing terminal
		if (terminalId!=null) {
			for (Terminal t : n.getTerminals()) {
				if (t.getId().equals(terminalId)) {
					t.setName(name);
					if (modelClazz.equals(XmlDatamodelNature.class)) {
						((XmlTerminal)t).setAttribute(attribute);
						if (namespace.trim().isEmpty()) {
							((XmlTerminal)t).setNamespace(null);
						} else {
							((XmlTerminal)t).setNamespace(namespace);
						}
					}
					break;
				}
			}
		} else if (nonterminalId!=null) {
			TerminalImpl tAppend;
			if (modelClazz.equals(XmlDatamodelNature.class)) {
				tAppend = new XmlTerminal();
				((XmlTerminal)tAppend).setAttribute(attribute);
				if (namespace.trim().isEmpty()) {
					((XmlTerminal)tAppend).setNamespace(null);
				} else {
					((XmlTerminal)tAppend).setNamespace(namespace);
				}
			} else {
				tAppend = new TerminalImpl();
			}
			tAppend.setName(name);
			tAppend.setId(BaseDaoImpl.createNewObjectId());
			n.addTerminal(tAppend);
			n.mapNonterminal(nonterminalId, tAppend.getId());
		}
		
		schemaService.saveSchema(m, auth);
		
		return new ModelActionPojo(true);
	}
}