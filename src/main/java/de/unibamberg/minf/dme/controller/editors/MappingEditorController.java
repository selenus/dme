package de.unibamberg.minf.dme.controller.editors;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bouncycastle.jce.provider.symmetric.RC6.Mappings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import de.unibamberg.minf.core.util.Stopwatch;
import de.unibamberg.minf.dme.controller.base.BaseMainEditorController;
import de.unibamberg.minf.dme.exception.GenericScheregException;
import de.unibamberg.minf.dme.importer.BaseImportWorker;
import de.unibamberg.minf.dme.importer.DatamodelImportWorker;
import de.unibamberg.minf.dme.importer.Importer;
import de.unibamberg.minf.dme.importer.MappingImportWorker;
import de.unibamberg.minf.dme.importer.datamodel.DatamodelImporter;
import de.unibamberg.minf.dme.importer.mapping.MappingImporter;
import de.unibamberg.minf.dme.model.PersistedSession;
import de.unibamberg.minf.dme.model.RightsContainer;
import de.unibamberg.minf.dme.model.LogEntry.LogType;
import de.unibamberg.minf.dme.model.base.Element;
import de.unibamberg.minf.dme.model.base.Function;
import de.unibamberg.minf.dme.model.base.Grammar;
import de.unibamberg.minf.dme.model.base.ModelElement;
import de.unibamberg.minf.dme.model.datamodel.DatamodelImpl;
import de.unibamberg.minf.dme.model.datamodel.base.Datamodel;
import de.unibamberg.minf.dme.model.mapping.MappingImpl;
import de.unibamberg.minf.dme.model.mapping.base.MappedConcept;
import de.unibamberg.minf.dme.model.mapping.base.Mapping;
import de.unibamberg.minf.dme.model.serialization.DatamodelContainer;
import de.unibamberg.minf.dme.model.serialization.MappingContainer;
import de.unibamberg.minf.dme.model.tracking.ChangeSet;
import de.unibamberg.minf.dme.pojo.AuthWrappedPojo;
import de.unibamberg.minf.dme.pojo.converter.AuthWrappedPojoConverter;
import de.unibamberg.minf.dme.processing.CollectingResourceConsumptionService;
import de.unibamberg.minf.dme.service.base.BaseEntityService;
import de.unibamberg.minf.dme.service.interfaces.FunctionService;
import de.unibamberg.minf.dme.service.interfaces.GrammarService;
import de.unibamberg.minf.dme.service.interfaces.MappedConceptService;
import de.unibamberg.minf.dme.service.interfaces.PersistedSessionService;
import de.unibamberg.minf.dme.service.interfaces.SchemaService;
import de.unibamberg.minf.mapping.model.MappingExecGroup;
import de.unibamberg.minf.mapping.service.MappingExecutionService;
import de.unibamberg.minf.processing.model.base.Resource;
import eu.dariah.de.dariahsp.model.web.AuthPojo;
import de.unibamberg.minf.core.web.pojo.MessagePojo;
import de.unibamberg.minf.core.web.pojo.ModelActionPojo;

@Controller
@RequestMapping(value="/mapping/editor/{entityId}/")
public class MappingEditorController extends BaseMainEditorController {
	@Autowired private MappingImportWorker importWorker;
	
	@Autowired private GrammarService grammarService;
	@Autowired private FunctionService functionService;
	@Autowired private MappedConceptService mappedConceptService;
	@Autowired private AuthWrappedPojoConverter authPojoConverter;
	@Autowired private PersistedSessionService sessionService;
	

	@Override protected String getPrefix() { return "/mapping/editor/"; }
	@Override protected MappingImportWorker getImportWorker() { return this.importWorker; }
	@Override protected BaseEntityService getMainEntityService() { return this.mappingService; }
	
	
	public MappingEditorController() {
		super("mappingEditor");
	}
	
	@RequestMapping(value="", method=RequestMethod.GET)
	public String getEditor(@PathVariable String entityId, Model model, HttpServletRequest request, Locale locale) {
		AuthPojo auth = authInfoHelper.getAuth(request);
		
		RightsContainer<Mapping> mapping = mappingService.findByIdAndAuth(entityId, auth);
		
		AuthWrappedPojo<Mapping> mappingPojo = authPojoConverter.convert(mapping, auth.getUserId()); 
		if (mapping==null) {
			return "redirect:/registry/";
		}
		
		model.addAttribute("mapping", mappingPojo);
		model.addAttribute("source", authPojoConverter.convert(schemaService.findByIdAndAuth(mappingPojo.getPojo().getSourceId(), auth), auth.getUserId()));
		model.addAttribute("target", authPojoConverter.convert(schemaService.findByIdAndAuth(mappingPojo.getPojo().getTargetId(), auth), auth.getUserId()));
		
		boolean oversized = false;
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
		
		return "mappingEditor";
	}
	
	@RequestMapping(value="/async/getConcepts", method=RequestMethod.GET)
	public @ResponseBody List<MappedConcept> getMappedConcepts(@PathVariable String entityId, Model model) {
		return mappedConceptService.findAllByMappingId(entityId);
	}
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method=GET, value={"/forms/import"})
	public String getImportForm(@PathVariable String entityId, Model model, Locale locale, HttpServletRequest request, HttpServletResponse response) {
		AuthPojo auth = authInfoHelper.getAuth(request);
		if(!schemaService.getUserCanWriteEntity(entityId, auth.getUserId())) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return null;
		}
		model.addAttribute("actionPath", "/mapping/editor/" + entityId + "/async/import");
		model.addAttribute("mapping", mappingService.findMappingById(entityId));

		return "mapping/form/import";
	}
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method=POST, value={"/async/import"}, produces = "application/json; charset=utf-8")
	public @ResponseBody ModelActionPojo importSchemaElements(@PathVariable String entityId, @RequestParam(value="file.id") String fileId, @RequestParam(defaultValue="false", value="keep-imported-ids") boolean keepImportedIds,			
			Locale locale, HttpServletRequest request, HttpServletResponse response) {
		AuthPojo auth = authInfoHelper.getAuth(request);
		if(!schemaService.getUserCanWriteEntity(entityId, auth.getUserId())) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return new ModelActionPojo(false);
		}
		ModelActionPojo result = new ModelActionPojo();
		try {
			if (temporaryFilesMap.containsKey(fileId)) {
				
				importWorker.importMapping(temporaryFilesMap.remove(fileId), entityId, keepImportedIds, authInfoHelper.getAuth(request));
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
	
	@Override
	protected ModelActionPojo validateImportedFile(String entityId, String fileId, String elementId, Locale locale) {
		ModelActionPojo result = new ModelActionPojo();
		MappingImporter importer = importWorker.getSupportingImporter(temporaryFilesMap.get(fileId));
				
		if (importer!=null) {
			result.setSuccess(true);
			MessagePojo msg = new MessagePojo("success", 
					messageSource.getMessage("~de.unibamberg.minf.common.view.forms.file.validationsucceeded.head", null, locale), 
					messageSource.getMessage("~de.unibamberg.minf.common.view.forms.file.validationsucceeded.body", null, locale));
			result.setMessage(msg);
			
			ObjectNode pojoNode = objectMapper.createObjectNode();
			pojoNode.set("keepIdsAllowed", BooleanNode.valueOf(importer.isKeepImportedIdsSupported()));
			pojoNode.set("importerMainType", TextNode.valueOf(importer.getMainImporterType()));
			pojoNode.set("importerSubtype", TextNode.valueOf(importer.getImporterSubtype()));
			
			result.setPojo(pojoNode);
		}
		return result;
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/executeSampleMapping")
	public @ResponseBody ModelActionPojo executeSampleMapping(@PathVariable String entityId, HttpServletRequest request, HttpServletResponse response, Locale locale) {
		Stopwatch sw = new Stopwatch().start();
		ModelActionPojo result = new ModelActionPojo(true);
		result.setPojo(0);
		
		PersistedSession session = sessionService.access(entityId, request.getSession().getId(), authInfoHelper.getUserId(request));
		if (session==null) {
			response.setStatus(HttpServletResponse.SC_RESET_CONTENT);
			return null;
		}

		
		Mapping m = mappingService.findMappingById(entityId);
		List<Resource> inputResources = session.getSampleOutput();
		Element r = elementService.findRootBySchemaId(m.getTargetId(), true);
		List<MappedConcept> concepts = mappedConceptService.findAllByMappingId(m.getId(), true);
				
		MappingExecutionService mappingExecService = appContext.getBean(MappingExecutionService.class);
		CollectingResourceConsumptionService consumptionService = new CollectingResourceConsumptionService();
		
		// TODO Cache mapping execution group -> in ScheReg
		
		MappingExecGroup mapExecGroup = new MappingExecGroup();
		mapExecGroup.setMapping(m);
		mapExecGroup.setTargetElementTree(r);
		mapExecGroup.setTargetSchemaId(m.getTargetId());
		
		// TODO: Sources really needed?
		for (Grammar g : grammarService.findByEntityId(m.getId(), true)) {
			mapExecGroup.addGrammar(g);
		}
		
		for (MappedConcept c : concepts) {			
			mapExecGroup.addMappedConcept(c, functionService.findById(c.getFunctionId()));
		}
		
		
		
		
		mappingExecService.init(mapExecGroup, inputResources);
		mappingExecService.addConsumptionService(consumptionService);
		
		mappingExecService.run();
		
		session.setSampleMapped(consumptionService.getResources());

		if (session.getSampleMapped()!=null && session.getSampleMapped().size()>0) {
			result.setPojo(session.getSampleMapped().size());
			
			if (session.getSampleOutput().size()==1) {
				session.addLogEntry(LogType.SUCCESS, messageSource.getMessage("~de.unibamberg.minf.dme.editor.sample.log.translated_1_results", new Object[]{sw.getElapsedTime()}, locale));
			} else {
				session.addLogEntry(LogType.SUCCESS, messageSource.getMessage("~de.unibamberg.minf.dme.editor.sample.log.translated_n_results", new Object[]{sw.getElapsedTime(), consumptionService.getResources().size()}, locale));	
			}
		} else {
			session.addLogEntry(LogType.WARNING, messageSource.getMessage("~de.unibamberg.minf.dme.editor.sample.log.translated_no_results", null, locale));
		}
		
		sessionService.saveSession(session);
		return result;
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/export")
	public @ResponseBody ModelActionPojo exportMapping(@PathVariable String entityId, Model model, Locale locale, HttpServletRequest request, HttpServletResponse response) {
		AuthPojo auth = authInfoHelper.getAuth(request);
		if(!mappingService.getUserCanWriteEntity(entityId, auth.getUserId())) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return new ModelActionPojo(false);
		}
		
		MappingImpl m = (MappingImpl)mappingService.findMappingById(entityId);
		m.setConcepts(mappedConceptService.findAllByMappingId(entityId, true));
		m.flush();
		
		MappingContainer mc = new MappingContainer();
		mc.setMapping(m);

		ChangeSet ch = schemaService.getLatestChangeSetForEntity(m.getId());
		if (ch!=null) {
			m.setVersionId(ch.getId());
		}

		mc.setGrammars(grammarService.serializeGrammarSources(entityId));
		
		Map<String, String> serializedFunctions = new HashMap<String, String>();
		
		List<Function> functions = functionService.findByEntityId(entityId);
		for (Function f : functions) {
			serializedFunctions.put(f.getId(), f.getFunction());
		}
		mc.setFunctions(serializedFunctions);
		
		ModelActionPojo result = new ModelActionPojo(true);
		result.setPojo(mc);
		return result;
	}
}