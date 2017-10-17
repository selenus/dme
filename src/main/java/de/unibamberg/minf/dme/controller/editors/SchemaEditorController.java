package de.unibamberg.minf.dme.controller.editors;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import de.unibamberg.minf.dme.controller.base.BaseMainEditorController;
import de.unibamberg.minf.dme.exception.GenericScheregException;
import de.unibamberg.minf.dme.importer.DatamodelImportWorker;
import de.unibamberg.minf.dme.importer.datamodel.DatamodelImporter;
import de.unibamberg.minf.dme.model.PersistedSession;
import de.unibamberg.minf.dme.model.RightsContainer;
import de.unibamberg.minf.dme.model.base.Element;
import de.unibamberg.minf.dme.model.base.Function;
import de.unibamberg.minf.dme.model.base.Grammar;
import de.unibamberg.minf.dme.model.base.Identifiable;
import de.unibamberg.minf.dme.model.base.ModelElement;
import de.unibamberg.minf.dme.model.base.Nonterminal;
import de.unibamberg.minf.dme.model.base.Terminal;
import de.unibamberg.minf.dme.model.datamodel.DatamodelImpl;
import de.unibamberg.minf.dme.model.datamodel.NonterminalImpl;
import de.unibamberg.minf.dme.model.datamodel.base.Datamodel;
import de.unibamberg.minf.dme.model.datamodel.base.DatamodelNature;
import de.unibamberg.minf.dme.model.datamodel.natures.XmlDatamodelNature;
import de.unibamberg.minf.dme.model.function.FunctionImpl;
import de.unibamberg.minf.dme.model.grammar.GrammarImpl;
import de.unibamberg.minf.dme.model.mapping.base.Mapping;
import de.unibamberg.minf.dme.model.serialization.DatamodelContainer;
import de.unibamberg.minf.dme.model.tracking.ChangeSet;
import de.unibamberg.minf.dme.pojo.ModelElementPojo;
import de.unibamberg.minf.dme.pojo.converter.AuthWrappedPojoConverter;
import de.unibamberg.minf.dme.pojo.converter.ModelElementPojoConverter;
import de.unibamberg.minf.dme.service.IdentifiableServiceImpl;
import de.unibamberg.minf.dme.service.base.BaseEntityService;
import de.unibamberg.minf.dme.service.interfaces.GrammarService;
import de.unibamberg.minf.dme.service.interfaces.IdentifiableService;
import eu.dariah.de.dariahsp.model.web.AuthPojo;
import de.unibamberg.minf.core.web.pojo.ModelActionPojo;
import de.unibamberg.minf.core.web.pojo.MessagePojo;

@Controller
@RequestMapping(value="/model/editor/{entityId}/")
public class SchemaEditorController extends BaseMainEditorController implements InitializingBean {	
	@Autowired private DatamodelImportWorker importWorker;
	@Autowired private AuthWrappedPojoConverter authPojoConverter;
	@Autowired private GrammarService grammarService;
	
	@Autowired private IdentifiableService identifiableService;
	
	
	@Override protected String getPrefix() { return "/model/editor/"; }
	@Override protected DatamodelImportWorker getImportWorker() { return this.importWorker; }
	@Override protected BaseEntityService getMainEntityService() { return this.schemaService; }
	
	
	public SchemaEditorController() {
		super("schemaEditor");
	}
		
	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		Files.createDirectories(Paths.get(tmpUploadDirPath));
	}
	
	@RequestMapping(value="/query/{query}", method=RequestMethod.GET)
	public @ResponseBody List<Identifiable> queryElements(@PathVariable String entityId, @PathVariable String query) {
		return identifiableService.findByNameAndSchemaId(query, entityId, new Class<?>[] { Nonterminal.class, GrammarImpl.class });
	}
	
	@RequestMapping(method=GET, value="")
	public String getEditor(@PathVariable String entityId, Model model, @ModelAttribute String sample, Locale locale, HttpServletRequest request, HttpServletResponse response) throws IOException {
		AuthPojo auth = authInfoHelper.getAuth(request);
		RightsContainer<Datamodel> schema = schemaService.findByIdAndAuth(entityId, auth);
		if (schema==null) {
			response.sendRedirect("/registry/");
			return null;
		}
		
		boolean oversized = false;
		
		model.addAttribute("schema", authPojoConverter.convert(schema, auth.getUserId()));
		
		List<RightsContainer<Mapping>> mappings = mappingService.getMappings(entityId);
		model.addAttribute("mapped", mappings!=null && mappings.size()>0);
		try {
			PersistedSession s = sessionService.accessOrCreate(entityId, request.getSession().getId(), auth.getUserId(), messageSource, locale);
			model.addAttribute("session", s);
			
			if (s.getSampleInput()!=null) {
				if (s.getSampleInput().getBytes().length>this.maxTravelSize) {
					oversized = true;
				} else {
					model.addAttribute("sampleInput", s.getSampleInput());
				}
			}
			
		} catch (Exception e) {
			logger.error("Failed to load/initialize persisted session", e);
		}
		
		model.addAttribute("sampleInputOversize", oversized);
		return "schemaEditor";
	}
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method=GET, value={"/forms/edit"})
	public String getEditForm(@PathVariable String entityId, Model model, Locale locale, HttpServletRequest request, HttpServletResponse response) {
		if (!schemaService.getUserCanWriteEntity(entityId, authInfoHelper.getAuth(request).getUserId())) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return null;
		}
		RightsContainer<Datamodel> schema = schemaService.findByIdAndAuth(entityId, authInfoHelper.getAuth(request));
		model.addAttribute("actionPath", "/model/async/save");
		model.addAttribute("datamodelImpl", schema.getElement());
		model.addAttribute("readOnly", schema.isReadOnly());
		return "schema/form/edit";
	}
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method=GET, value={"/async/delete"}, produces = "application/json; charset=utf-8")
	public @ResponseBody ModelActionPojo deleteSchema(@PathVariable String entityId, HttpServletRequest request, HttpServletResponse response) {
		if (!schemaService.getUserCanWriteEntity(entityId, authInfoHelper.getAuth(request).getUserId())) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return new ModelActionPojo(false);
		}
		ModelActionPojo result;
		if (entityId!=null && !entityId.isEmpty()) {
			schemaService.deleteSchemaById(entityId, authInfoHelper.getAuth(request));
			result = new ModelActionPojo(true);
		} else {
			result = new ModelActionPojo(false);
		}		
		return result;
	}
	
	
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method=GET, value={"/forms/import"})
	public String getImportForm(@PathVariable String entityId, @RequestParam(required=false) String elementId, Model model, Locale locale, HttpServletRequest request, HttpServletResponse response) {
		AuthPojo auth = authInfoHelper.getAuth(request);
		if(!schemaService.getUserCanWriteEntity(entityId, auth.getUserId())) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return null;
		}
		model.addAttribute("actionPath", "/model/editor/" + entityId + "/async/import");
		model.addAttribute("schema", schemaService.findSchemaById(entityId));
		if (elementId!=null){
			model.addAttribute("elementId", elementId);
		}
		return "schemaEditor/form/import";
	}
	
	
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method = RequestMethod.GET, value = "/form/createRoot")
	public String getNewNonterminalForm(@PathVariable String entityId, Model model, Locale locale, HttpServletRequest request, HttpServletResponse response) {
		AuthPojo auth = authInfoHelper.getAuth(request);
		if(!schemaService.getUserCanWriteEntity(entityId, auth.getUserId())) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return null;
		}
		model.addAttribute("element", new NonterminalImpl());
		model.addAttribute("availableTerminals", schemaService.getAvailableTerminals(entityId));
		model.addAttribute("actionPath", "/model/editor/" + entityId + "/async/saveNewRoot");
		return "elementEditor/form/edit_nonterminal";
	}
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method = RequestMethod.POST, value = "/async/saveNewRoot")
	public @ResponseBody ModelActionPojo saveNonterminal(@PathVariable String entityId, @Valid NonterminalImpl element, BindingResult bindingResult, Locale locale, HttpServletRequest request, HttpServletResponse response) {
		AuthPojo auth = authInfoHelper.getAuth(request);
		if(!schemaService.getUserCanWriteEntity(entityId, auth.getUserId())) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return new ModelActionPojo(false);
		}
		ModelActionPojo result = this.getActionResult(bindingResult, locale);
		if (result.isSuccess()) {
			element.setEntityId(entityId);
			elementService.saveOrReplaceRoot(entityId, element, authInfoHelper.getAuth(request));
		}		
		return result;
	}
	
	
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method=POST, value={"/async/import"}, produces = "application/json; charset=utf-8")
	public @ResponseBody ModelActionPojo importSchemaElements(@PathVariable String entityId, @RequestParam(value="file.id") String fileId, @RequestParam(required=false, value="elementId") String elementId, 
			@RequestParam(value="schema_root_qn") String schemaRoot, @RequestParam(required=false, value="schema_root_type") String schemaRootType, 
			@RequestParam(defaultValue="false", value="keep-imported-ids") boolean keepImportedIds,			
			Locale locale, HttpServletRequest request, HttpServletResponse response) {
		AuthPojo auth = authInfoHelper.getAuth(request);
		if(!schemaService.getUserCanWriteEntity(entityId, auth.getUserId())) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return new ModelActionPojo(false);
		}
		ModelActionPojo result = new ModelActionPojo();
		try {
			if (temporaryFilesMap.containsKey(fileId)) {
				if (schemaRoot.isEmpty()) {
					result.setSuccess(false);
					result.addFieldError("schema_root", messageSource.getMessage("~de.unibamberg.minf.dme.notification.import.root_missing", null, locale));
					
					return result;
				}
				
				if (elementId!=null) {
					importWorker.importSubtree(temporaryFilesMap.remove(fileId), entityId, elementId, schemaRoot, schemaRootType, keepImportedIds, authInfoHelper.getAuth(request));
				} else {
					Datamodel m = schemaService.findByIdAndAuth(entityId, auth).getElement();
					m.setNatures(null);
					
					elementService.clearElementTree(entityId, auth);
					
					schemaService.saveSchema(m, auth);
					
					importWorker.importSchema(temporaryFilesMap.remove(fileId), entityId, schemaRoot, keepImportedIds, authInfoHelper.getAuth(request));
				}
				result.setSuccess(true);
				return result;
			}
		} catch (Exception e) {
			MessagePojo msg = new MessagePojo("danger", 
					messageSource.getMessage("~de.unibamberg.minf.common.view.forms.file.generalerror.head", null, locale), 
					messageSource.getMessage("~de.unibamberg.minf.common.view.forms.file.generalerror.body", new Object[] {e.getLocalizedMessage()}, locale));
			result.setMessage(msg);
		}
		result.setSuccess(false);
		return result;
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/export")
	public @ResponseBody ModelActionPojo exportSchema(@PathVariable String entityId, Model model, Locale locale) {
		Datamodel s = schemaService.findSchemaById(entityId);
		Element r = elementService.findRootBySchemaId(entityId, true);
		
		DatamodelContainer sp = new DatamodelContainer();
		sp.setModel((DatamodelImpl)s);
		sp.setRoot(r);
		
		ChangeSet ch = schemaService.getLatestChangeSetForEntity(s.getId());
		if (ch!=null) {
			s.setVersionId(ch.getId());
		}
		
		sp.setRoot(r);
		sp.setGrammars(grammarService.serializeGrammarSources(entityId));
		
		ModelActionPojo result = new ModelActionPojo(true);
		result.setPojo(sp);
		return result;
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/exportSubtree")
	public @ResponseBody ModelActionPojo exportSubtree(@PathVariable String entityId, @RequestParam String elementId, Model model, Locale locale) {
		Datamodel s = schemaService.findSchemaById(entityId);
		Identifiable rootE = elementService.getElementSubtree(entityId, elementId);
		Element expE;
		if (Element.class.isAssignableFrom(rootE.getClass())) {
			expE = (Element)rootE;
		} else {
			expE = new NonterminalImpl(s.getId(), "EXPORT_CONTAINER");
			expE.setGrammars(new ArrayList<Grammar>());
			
			GrammarImpl expG;
			if (GrammarImpl.class.isAssignableFrom(rootE.getClass())) {
				expG = (GrammarImpl)rootE;
			} else {
				expG = new GrammarImpl(entityId, "EXPORT_CONTAINER");
				expG.setFunctions(new ArrayList<Function>());
				
				FunctionImpl expF;
				if (FunctionImpl.class.isAssignableFrom(rootE.getClass())) {
					expF = (FunctionImpl)rootE;
				} else {
					return null;
				}
				expG.getFunctions().add(expF);
			}
			expE.getGrammars().add(expG);
		}

		DatamodelContainer sp = new DatamodelContainer();
		sp.setModel(schemaService.cloneSchemaForSubtree(s, expE));
		sp.setRoot(expE);
		
		ChangeSet ch = schemaService.getLatestChangeSetForEntity(s.getId());
		if (ch!=null) {
			s.setVersionId(ch.getId());
		}
		
		List<ModelElement> relevantGrammarsI = IdentifiableServiceImpl.extractAllByTypes(expE, IdentifiableServiceImpl.getGrammarClasses());
		if (relevantGrammarsI!=null && relevantGrammarsI.size()>0) {
			List<Grammar> relevantGrammars = new ArrayList<Grammar>(relevantGrammarsI.size());
			for (ModelElement g : relevantGrammarsI) {
				if (!relevantGrammars.contains(g)) {
					relevantGrammars.add((Grammar)g);
				}
			}
			sp.setGrammars(grammarService.serializeGrammarSources(relevantGrammars));
		}
		ModelActionPojo result = new ModelActionPojo(true);
		result.setPojo(sp);
		return result;
	}
	
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/getHierarchy")
	public @ResponseBody ModelElementPojo getHierarchy(@PathVariable String entityId, @RequestParam(defaultValue="false") boolean staticElementsOnly, @RequestParam(defaultValue="false") boolean collectNatureClasses,
			@RequestParam(defaultValue="logical_model", name="model") String modelClass, Model model, Locale locale, HttpServletRequest request, HttpServletResponse response) throws IOException, GenericScheregException {
		AuthPojo auth = authInfoHelper.getAuth(request);
		Element result = elementService.findRootBySchemaId(entityId, true);
		if (result==null) {
			response.getWriter().print("null");
			response.setContentType("application/json");
		}
		
		Map<String, List<String>> nonterminalNatureClassesMap = new HashMap<String, List<String>>();
		Datamodel m = schemaService.findByIdAndAuth(entityId, auth).getElement();
		if (modelClass.equals("logical_model")) {
			if (m.getNatures()!=null) {
				for (DatamodelNature n : m.getNatures()) {
					if (n.getNonterminalTerminalIdMap()!=null) {
						for (String nId : n.getNonterminalTerminalIdMap().keySet()) {
							List<String> natureClasses = nonterminalNatureClassesMap.get(nId);
							if (natureClasses==null) {
								natureClasses = new ArrayList<String>();
							}
							natureClasses.add(n.getClass().getName());
							nonterminalNatureClassesMap.put(nId, natureClasses);
						}
					}
				}
			}
			return ModelElementPojoConverter.convertModelElement(result, nonterminalNatureClassesMap, staticElementsOnly);
		} else {
			try {
				@SuppressWarnings("unchecked")
				Class<? extends DatamodelNature> modelClazz = (Class<? extends DatamodelNature>)Class.forName(modelClass);
				DatamodelNature n = schemaService.findByIdAndAuth(entityId, auth).getElement().getNature(modelClazz);
				
				return ModelElementPojoConverter.convertModelElementTerminal(result, n);
			} catch (Exception e) {
				logger.error(String.format("Failed to retrieve model of class %s for datamodel %s", modelClass, entityId), e);
			}
		}
		return null;
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/getTerminals")
	public @ResponseBody List<? extends Terminal> getTerminals(@PathVariable String entityId) {
		Datamodel s = schemaService.findSchemaById(entityId);
		
		if (s.getNature(XmlDatamodelNature.class)!=null) {
			return s.getNature(XmlDatamodelNature.class).getTerminals();
		}
		return null;
	}

	@Override
	protected ModelActionPojo validateImportedFile(String entityId, String fileId, String elementId, Locale locale) {
		JsonNode rootNodes;
		ModelActionPojo result = new ModelActionPojo();
		DatamodelImporter importer = importWorker.getSupportingImporter(temporaryFilesMap.get(fileId));
		
		if (elementId==null) {
			rootNodes = objectMapper.valueToTree(importer.getPossibleRootElements());
		} else {
			List<Class<? extends ModelElement>> allowedSubtreeRoots = identifiableService.getAllowedSubelementTypes(elementId);
			rootNodes = objectMapper.valueToTree(importer.getElementsByTypes(allowedSubtreeRoots));
		}
		
		if (rootNodes!=null) {
			result.setSuccess(true);
			MessagePojo msg = new MessagePojo("success", 
					messageSource.getMessage("~de.unibamberg.minf.common.view.forms.file.validationsucceeded.head", null, locale), 
					messageSource.getMessage("~de.unibamberg.minf.common.view.forms.file.validationsucceeded.body", null, locale));
			result.setMessage(msg);
			
			ObjectNode pojoNode = objectMapper.createObjectNode();
			pojoNode.set("elements", rootNodes);
			pojoNode.set("keepIdsAllowed", BooleanNode.valueOf(importer.isKeepImportedIdsSupported()));
			pojoNode.set("importerMainType", TextNode.valueOf(importer.getMainImporterType()));
			pojoNode.set("importerSubtype", TextNode.valueOf(importer.getImporterSubtype()));
			
			result.setPojo(pojoNode);
		}
		return result;
	}
}