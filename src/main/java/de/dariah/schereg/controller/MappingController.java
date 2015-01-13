package de.dariah.schereg.controller;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.reflect.TypeToken;

import de.dariah.schereg.base.model.Mapping;
import de.dariah.schereg.base.model.Schema;
import de.dariah.schereg.base.model.validation.MappingValidator;
import de.dariah.schereg.base.service.MappingService;
import de.dariah.schereg.base.service.SchemaElementService;
import de.dariah.schereg.base.service.SchemaService;
import de.dariah.schereg.controller.base.BaseController;
import de.dariah.schereg.matcher.SchemaMatcher;
import de.dariah.schereg.util.ImmutableDateTimeSerializer;
import de.dariah.schereg.util.ScheRegConstants;
import de.dariah.schereg.view.ModelToJsonConverter;

@Controller
@RequestMapping("/mapping")
public class MappingController extends BaseController {

	@Autowired private MappingService mappingService;
	@Autowired private SchemaService schemaService;
	@Autowired private SchemaElementService schemaElementService;
	@Autowired private MappingValidator mappingValidator;
	@Autowired private ApplicationContext appContext;
	@Autowired private SchemaMatcher schemaMatcher;
	
	@InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.setValidator(mappingValidator);
    }	
		
	@RequestMapping(method=GET, value={"", "/", "view"})
	public String listMappings(@RequestParam(value="v", required=false) Integer view, Model model) {
		if (view == null) {
			view = 0;
		}
		model.addAttribute("mappings", mappingService.listMappings());
		model.addAttribute("view", view);
		
		return "mapping/list";
	}
	
	@RequestMapping(method=GET, value={"", "/", "view"}, params="mapping")
	public String listMappings(@RequestParam("mapping") int id, @RequestParam(value="v", required=false) Integer view, Model model) {
		if (view == null) {
			view = 0;
		}
		model.addAttribute("mappings", mappingService.listMappings());
		model.addAttribute("view", view);
		model.addAttribute("selectedMapping", mappingService.getMapping(id));
		return "mapping/list"; 
	}
	

	@RequestMapping(method=GET, value="/new")
	public String createMapping(Model model) throws Exception {
		
		List<Schema> schemas = schemaService.listSchemas();
		
		model.addAttribute("mapping", mappingService.createNewMapping());
		model.addAttribute("schemaList", schemas);
				
		return "mapping/edit";
	}
	
	@RequestMapping(method=GET, value="/edit")
	public String editMapping(@RequestParam("mapping") int id, Model model) {
		
		Mapping mapping = mappingService.getMapping(id);
		
		if (mapping==null) {
			return "redirect:/mapping/";
		}
		
		List<Schema> schemas = schemaService.listSchemas();
		model.addAttribute("schemaList", schemas);
		
		model.addAttribute("mapping", mapping);
		return "mapping/edit";
	}
	
	
	
	@RequestMapping(method=GET, value="/delete")
	public String deleteMapping(@RequestParam("mapping") int id, Model model) {
		mappingService.removeMapping(id);
		
		return "redirect:/mapping";
	}
		
	@RequestMapping(method=POST, value={"/edit", "/new"})
	public String savemappingFromForm(@Valid Mapping mapping, BindingResult bindingResult, Map<String, Object> model) {
		
		if (bindingResult.hasErrors()) {
			List<Schema> schemas = schemaService.listSchemas();
			model.put("schemaList", schemas);
			
			return "mapping/edit";
		}
		
		if (!mapping.isPerformMatching()) {
			mapping.setIsLocked(false);
			
			mappingService.saveMapping(mapping);

			return "redirect:/mapping/";
		}
		
		mapping.setIsLocked(true);
		
		mappingService.saveMapping(mapping);

		SchemaMatcher mapExSvc = appContext.getBean(SchemaMatcher.class);
				
		mapExSvc.setMapping(mapping);
		mapExSvc.match();
		
		return "redirect:/mapping/";
	}
	
	
	
	@RequestMapping(value={"/loadRow", "/ajax/loadRow"}, method = RequestMethod.GET)
	public String getRow(@RequestParam int id, Model model) {
		Mapping mapping = mappingService.getMapping(id);
		model.addAttribute("mapping", mapping);
		return "mapping/list/tr";
	}
		
	@RequestMapping(method = RequestMethod.GET, value = "/ajax/delete", produces = "application/json; charset=utf-8")
	public @ResponseBody String deleteMapping(@RequestParam int id, Model model, Locale locale) {	
		logger.debug(String.format("Command received to delete mapping [%s]", id));
			
		GsonBuilder bldr = new GsonBuilder();
		Gson gson = bldr.create();
		
		JsonObject result = new JsonObject();

		mappingService.removeMapping(id);
		result.addProperty("success", true);
		result.addProperty("message_type", "success");
		result.addProperty("message_head", messageSource.getMessage("~crosswalkRegistry.message.mapping.delete.success.head", null, locale));
		result.addProperty("message_body", messageSource.getMessage("~crosswalkRegistry.message.mapping.delete.success.body", null, locale));
	
		return gson.toJson(result);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/ajax/getNewForm")
	public String getNewForm(Model model) {
		List<Schema> schemas = schemaService.listSchemas();

		model.addAttribute("mapping", mappingService.createNewMapping());
		model.addAttribute("schemaList", schemas);
		
		model.addAttribute("actionPath", "/mapping/ajax/save");
		
		return "mapping/edit/editForm";
	}
			
	@RequestMapping(method = RequestMethod.POST, value = "/ajax/save", produces = "application/json; charset=utf-8")
	public @ResponseBody String saveMapping(@Valid Mapping mapping, BindingResult bindingResult, HttpServletResponse response, Model model, Locale locale) {
		
		GsonBuilder bldr = new GsonBuilder();
		Gson gson = bldr.create();
		response.setCharacterEncoding("UTF-8");

		JsonObject result = new JsonObject();
		result.addProperty("success", !bindingResult.hasErrors());
		result.addProperty("errorCount", bindingResult.getErrorCount());

		if (bindingResult.hasErrors()) {
			result.add("errors", getJsonErrorList(bindingResult, locale));
		} else {
			
			/* TODO: Automatic matching is disabled for now */
			/*if (mapping.isPerformMatching()) {
				mapping.setIsLocked(true);
				mapping.setState(ScheRegConstants.STATE_IN_PROGRESS);
				
				mappingService.saveMapping(mapping);
				
				schemaMatcher.setMapping(mapping);
				schemaMatcher.match();
			} else {*/
				mapping.setIsLocked(false);
				mapping.setState(ScheRegConstants.STATE_OK);
				
				mappingService.saveMapping(mapping);
			//}
			result.addProperty("id", mapping.getId());
		}		
		return gson.toJson(result);
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/ajax/refresh", produces = "application/json; charset=utf-8")
	public @ResponseBody String refreshAsync(@RequestParam String items, Locale locale) {
		
		GsonBuilder bldr = new GsonBuilder();
		bldr.registerTypeAdapter(DateTime.class, new ImmutableDateTimeSerializer());
		
		Gson gson = bldr.create();
		Type collectionType = new TypeToken<Collection<Mapping>>(){}.getType();
		Collection<Mapping> mappings = gson.fromJson(items, collectionType);

		Map<Integer, JsonElement> result = new HashMap<Integer, JsonElement>();
		
		// Updates and deletions
		if (mappings != null && mappings.size()>0) {
			for (Mapping mapping : mappings) {
				if (mapping.getId() <= 0) {
					continue;
				}
				
				Mapping dbMapping = mappingService.getMapping(mapping.getId());
				if (dbMapping != null && dbMapping.getModified().isAfter(mapping.getModified())) {
					result.put(mapping.getId(), ModelToJsonConverter.getMappingAsJson(dbMapping, ScheRegConstants.MODEL_JSON_STATUS_UPDATE, locale));
				} else if (dbMapping == null) {
					result.put(mapping.getId(), ModelToJsonConverter.getMappingAsJson(mapping, ScheRegConstants.MODEL_JSON_STATUS_DELETE, locale));
				} else {
					result.put(mapping.getId(), new JsonPrimitive(ScheRegConstants.MODEL_JSON_STATUS_NOCHANGE));
				}
			}
		}
		
		// Added mappings
		DateTime maxModified = DateTime.now().minusMinutes(5);
		Collection<Mapping> newMappings = mappingService.getMappingsCreatedAfter(maxModified);
		if (newMappings != null && newMappings.size() > 0) {
			for (Mapping newMapping : newMappings) {
				if (!result.containsKey(newMapping.getId())) {
					result.put(newMapping.getId(), ModelToJsonConverter.getMappingAsJson(newMapping, ScheRegConstants.MODEL_JSON_STATUS_ADD, locale));
				}
			}
		}	
		
		return gson.toJson(result.values());
	}
	
	/* 
	 * TODO: This duplicate came up when merging controllers -> which one is the correct one
	 */
	/*@RequestMapping(value = "/refresh", method = RequestMethod.POST)
	public @ResponseBody String refreshAsync(@RequestParam String displayedItems, Locale locale) {
		
		GsonBuilder bldr = new GsonBuilder();
		bldr.registerTypeAdapter(DateTime.class, new ImmutableDateTimeSerializer());
		
		
		Gson gson = bldr.create();
				
		Type collectionType = new TypeToken<Collection<Mapping>>(){}.getType();
		Collection<Mapping> mappings = gson.fromJson(displayedItems, collectionType);

		Collection<Mapping> polledMappings = new ArrayList<Mapping>();
		
		for (Mapping mapping : mappings) {
			if (mapping.getId() != 0) {
				polledMappings.add(mapping);
			}
		}
		
		Map<Integer, JsonObject> retMapping = new HashMap<Integer, JsonObject>();
		
		DateTime maxModified = DateTime.now().minusMinutes(5);
		
		if (polledMappings != null && polledMappings.size() > 0) {
			for (Mapping mapping : polledMappings) {

				Mapping dbMapping = mappingService.getMapping(mapping.getId());
				if (dbMapping != null && dbMapping.getModified().isAfter(mapping.getModified().minusMinutes(1))) {
					retMapping.put(mapping.getId(), ModelToJsonConverter.getMappingAsJson(dbMapping, ScheRegConstants.MODEL_JSON_STATUS_UPDATE, locale));
				} else if (dbMapping == null) {
					retMapping.put(mapping.getId(), ModelToJsonConverter.getMappingAsJson(mapping, ScheRegConstants.MODEL_JSON_STATUS_DELETE, locale));
				} else {
					retMapping.put(mapping.getId(), null);
				}
			}
		}
		
		Collection<Mapping> newMappings = mappingService.getMappingsCreatedAfter(maxModified);
		if (newMappings != null && newMappings.size() > 0) {
			for (Mapping newMapping : newMappings) {
				if (!retMapping.containsKey(newMapping.getId())) {
					retMapping.put(newMapping.getId(), ModelToJsonConverter.getMappingAsJson(newMapping, ScheRegConstants.MODEL_JSON_STATUS_ADD, locale));
				}
			}
		}	
		
		List<JsonObject> resultList = new ArrayList<JsonObject>();
		for(int key : retMapping.keySet()) {
			JsonObject obj = retMapping.get(key);
			
			if (obj != null) {
				resultList.add(obj);
			}
		}
			
		String result = gson.toJson(resultList);
		return result;
	}*/
}
