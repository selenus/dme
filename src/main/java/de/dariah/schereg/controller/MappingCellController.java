package de.dariah.schereg.controller;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.validation.Valid;

import org.apache.commons.lang.NotImplementedException;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import de.dariah.base.model.base.ConfigurableSchemaElementImpl;
import de.dariah.base.model.base.SchemaElement;
import de.dariah.base.view.pojo.ModelActionPojo;
import de.dariah.schereg.base.model.Attribute;
import de.dariah.schereg.base.model.Containment;
import de.dariah.schereg.base.model.Mapping;
import de.dariah.schereg.base.model.MappingCell;
import de.dariah.schereg.base.model.MappingCellInput;
import de.dariah.schereg.base.model.ReadOnlySchemaElement;
import de.dariah.schereg.base.model.Schema;
import de.dariah.schereg.base.service.MappingCellService;
import de.dariah.schereg.base.service.MappingService;
import de.dariah.schereg.base.service.SchemaElementService;
import de.dariah.schereg.base.service.SchemaService;
import de.dariah.schereg.controller.base.BaseController;
import de.dariah.schereg.util.ReadableDateTimeSerializer;
import de.dariah.schereg.util.json.SchemaElementJsonConverter;

@Controller
@RequestMapping("/mapping/analyze")
public class MappingCellController extends BaseController {

	@Autowired private SchemaService schemaService;
	@Autowired private MappingService mappingService;
	@Autowired private MappingCellService mappingCellService;
	@Autowired private SchemaElementService schemaElementService;

	@Autowired private ArrayList availableAnalyzers;
	
	@RequestMapping(method=GET, value={"/", ""})
	public String analyzeMapping(@RequestParam("mapping") int id, Map<String, Object> model) {
		
		Mapping mapping = mappingService.getMapping(id);
		
		model.put("project_id", mapping.getProjectId());
		model.put("mapping", mapping);		
		
		return "mapping/analyze";
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/loadSchemaElements")
	public @ResponseBody String loadSchemaElements(@RequestParam String mappingId) {
		int id = Integer.parseInt(mappingId);
		
		Mapping mapping = mappingService.getMapping(id);
		if (mapping == null) {
			return "Mapping not found";
		}
		
		Schema sourceSchema = schemaService.getSchema(mapping.getSourceId());
		Schema targetSchema = schemaService.getSchema(mapping.getTargetId());
		SchemaElementJsonConverter converter = null;
		
		List<JsonObject> resultList = new ArrayList<JsonObject>();
		JsonObject schema = new JsonObject();
		schema.addProperty("id", sourceSchema.getId());
		schema.addProperty("type", "source");
		schema.addProperty("name", sourceSchema.getName());
		
		converter = new SchemaElementJsonConverter(schemaElementService.getSchemaElements(mapping.getSourceId()).getClassLookupTable());
		schema.add("elements", converter.getAsHierarchicalJson());
		resultList.add(schema);
		
		schema = new JsonObject();
		schema.addProperty("id", targetSchema.getId());
		schema.addProperty("type", "target");
		schema.addProperty("name", targetSchema.getName());
		
		converter = new SchemaElementJsonConverter(schemaElementService.getSchemaElements(mapping.getTargetId()).getClassLookupTable());
		schema.add("elements", converter.getAsHierarchicalJson());
		resultList.add(schema);

		
		GsonBuilder bldr = new GsonBuilder();
		Gson gson = bldr.create();
		
		return gson.toJson(resultList);
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/loadMappingCells")
	public @ResponseBody String loadMappingCells(@RequestParam String mappingId) {
		int id = Integer.parseInt(mappingId);
		
		Mapping mapping = mappingService.getMapping(id);
		if (mapping == null) {
			return "Mapping not found";
		}
		
		
		Collection<MappingCell> cells = mappingService.getMappingCells(id);
		List<JsonObject> resultList = new ArrayList<JsonObject>();
		
		for (MappingCell cell : cells) {
			
			Set<MappingCellInput> inputs = cell.getMappingCellInputs();
			if (inputs == null || inputs.size() == 0) {
				continue;
			}
			
			for (MappingCellInput input : cell.getMappingCellInputs()) {
				JsonObject o = new JsonObject();
				o.addProperty("id", cell.getId());
				o.addProperty("isSplit", inputs.size() > 1);
				o.addProperty("inputId", input.getElementID());
				o.addProperty("outputId", cell.getOutput());
				o.addProperty("score", cell.getScore());
				
				resultList.add(o);
			}
		}
		
		GsonBuilder bldr = new GsonBuilder();
		Gson gson = bldr.create();
		
		return gson.toJson(resultList);
	}	
	
	@RequestMapping(method = RequestMethod.POST, value = "/saveMappingCells")
	public @ResponseBody String saveMappingCell(@RequestParam int mappingId, @RequestParam int sourceCellId, @RequestParam int targetCellId, @RequestParam double score, Locale locale) {
		
		ReadableDateTimeSerializer readableSer = new ReadableDateTimeSerializer(locale);
		
		try {
			
			MappingCell mc = new MappingCell();
			mc.setMapping(mappingService.getMapping(mappingId));
			
			MappingCellInput input = new MappingCellInput();
			input.setElementID(sourceCellId);
			mc.addMappingCellInput(input);
			
			mc.setOutput(targetCellId);
			mc.setScore(score);
			
			mappingService.saveMappingCell(mc);
			
			return messageSource.getMessage("~crosswalkRegistry.vis.mapping.autosaved", new String[] {readableSer.serialize(DateTime.now(), DateTime.class, null).getAsString()}, locale);
			
		} catch (Exception e) {
			logger.error("Exception while saving mapping cell", e);
			return "false";
		}
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/ajax/getCells", produces = "application/json; charset=utf-8")
	public @ResponseBody String getCells(@RequestParam int mapping, @RequestParam int input, @RequestParam int output, Model model) {
		
		List<MappingCell> mcl = mappingCellService.getMappingCellsForInputOutput(mapping, input, output);
		
		GsonBuilder bldr = new GsonBuilder();
		Gson gson = bldr.create();

		List<JsonElement> result = new ArrayList<JsonElement>();
				
		if (mcl!=null && !mcl.isEmpty()) {
			for (MappingCell mc : mcl) {
				JsonObject obj = new JsonObject();
				obj.addProperty("id", mc.getId());
				obj.addProperty("type", mc.getClass().getName());
				
				if (mc.getName()!=null && !mc.getName().isEmpty()) {
					obj.addProperty("name", mc.getName());
				} else {
					obj.addProperty("name", "unnamed: " + mc.getId());
				}
				
				result.add(obj);
			}
		}
		
		return gson.toJson(result);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/ajax/getEditSchemaElementForm")
	public String getEditSchemaElementForm(@RequestParam int id, Model model, Locale locale) throws ClassNotFoundException {
		ReadOnlySchemaElement tmpSe = schemaElementService.getReadOnlySchemaElement(id);
		Class<?> c = Class.forName(tmpSe.getType());

		if (c.equals(Attribute.class) || c.equals(Containment.class)) {
			ConfigurableSchemaElementImpl se = schemaElementService.getSchemaElement(id, c);
			model.addAttribute("schemaElement", se);
		} else {
			throw new NotImplementedException("Configuration is only supported for attributes and containments; type of requested object was " + c.getName());
		}
		
		model.addAttribute("availableAnalyzers", availableAnalyzers);
		model.addAttribute("actionPath", "/mapping/analyze/ajax/saveElement");
		
		return "schema/element/detailsForm";
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/ajax/saveElement", produces = "application/json; charset=utf-8")
	public @ResponseBody ModelActionPojo saveSchemaElementDetails(@Valid ConfigurableSchemaElementImpl schemaElement, BindingResult bindingResult, Model model, Locale locale) throws ClassNotFoundException {
		ReadOnlySchemaElement tmpSe = schemaElementService.getReadOnlySchemaElement(schemaElement.getId());
		Class<?> c = Class.forName(tmpSe.getType());
		ModelActionPojo result = new ModelActionPojo();
		
		if (c.equals(Attribute.class) || c.equals(Containment.class)) {
			ConfigurableSchemaElementImpl se = schemaElementService.getSchemaElement(schemaElement.getId(), c);
			se.setProcessGeoData(schemaElement.isProcessGeoData());
			se.setProcessSourceLinks(schemaElement.isProcessSourceLinks());
			se.setAnalyzers(schemaElement.getAnalyzers());
			se.setUseForTitle(schemaElement.isUseForTitle());
			se.setUseForTopicModelling(schemaElement.isUseForTopicModelling());
			
			schemaElementService.saveOrUpdate(se);
			result.setSuccess(true);
		} 
		
		
		return result;
	}
	
	
	@RequestMapping(method = RequestMethod.GET, value = "/ajax/delete", produces = "application/json; charset=utf-8")
	public @ResponseBody String deleteMappingCell(@RequestParam(value="id") int cellId, @RequestParam(value="mapping.id") int mappingId, @RequestParam(required=false) String comment, Model model, Locale locale) {
		logger.debug(String.format("Command received to delete mapping cell [%s]", cellId));
		
		MappingCell mc = mappingCellService.getMappingCell(cellId);
		GsonBuilder bldr = new GsonBuilder();
		Gson gson = bldr.create();
		
		JsonObject result = new JsonObject();

		try {
			if (mc.getMapping().getId()==mappingId) {
				mappingCellService.remove(mc);
				
				annotationService.createOrUpdateAnnotation(mc, comment);
				
				result.addProperty("success", true);
				result.addProperty("message_type", "success");
				result.addProperty("message_head", "Successful delete - head");
				result.addProperty("message_body", "Successful delete - body");
			} else {
				throw new Exception("Could not delete - mapping id did not match the cell to be deleted.");
			}
		} catch (Exception e) {
			logger.error("Failed to delete mapping cell", e);
			
			result.addProperty("success", false);
			result.addProperty("message_type", "warn");
			result.addProperty("message_head", "Could not save - head");
			result.addProperty("message_body", "Could not save - body");
		}

		return gson.toJson(result);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/ajax/deleteForm", produces = "text/html; charset=utf-8")
	public String getMappingCellDetailsForm(@RequestParam int mappingId, @RequestParam int id, Model model) {
		model.addAttribute("mappingCell", mappingCellService.getMappingCell(id));		
		return "mapping/cell/deleteForm";
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/ajax/detailsForm", produces = "text/html; charset=utf-8")
	public String getMappingCellDetailsForm(@RequestParam int mappingId, @RequestParam int id, @RequestParam int inputId, @RequestParam int outputId, Model model) {
		
		MappingCell mc;
		List<Integer> currentInputs = new ArrayList<Integer>();
		
		if (id>0) {
			mc = mappingCellService.getMappingCell(id);
		} else {
			mc = new MappingCell();
			mc.setMapping(mappingService.getMapping(mappingId));
			mc.setOutput(outputId);
			
			currentInputs.add(inputId);
		}
		
		model.addAttribute("mappingCell", mc);
				
		ReadOnlySchemaElement output = schemaElementService.getReadOnlySchemaElement(mc.getOutput());
		model.addAttribute("output", output);
		
		List<MappingCellInput> constants = new ArrayList<MappingCellInput>();
		if (mc.getMappingCellInputs() != null) {
			for (MappingCellInput mci : mc.getMappingCellInputs()) {
				if (mci.getElementID()!=null && mci.getElementID().intValue()>0) {
					currentInputs.add(schemaElementService.getReadOnlySchemaElement(mci.getElementID()).getId());
				} else if (mci.getConstant() != null && !mci.getConstant().isEmpty()) {
					constants.add(mci);
				}
			}
		}
		model.addAttribute("currentInputs", currentInputs);
		model.addAttribute("constants", constants);
		
		List<ReadOnlySchemaElement> possibleInputs = new ArrayList<ReadOnlySchemaElement>();
		List<MappingCell> mcSiblings = mappingCellService.getMappingCellsForOutput(mappingId, mc.getOutput());
		if (mcSiblings!=null && !mcSiblings.isEmpty()) {
			for (MappingCell mcSibling : mcSiblings) {
				if (mcSibling.getMappingCellInputs() != null) {
					for (MappingCellInput mciSibling : mcSibling.getMappingCellInputs()) {
						// Only actual input elements are considered, not the constants - they are cell-specific
						if (mciSibling.getElementID()!=null && mciSibling.getElementID().intValue()>0) {
							ReadOnlySchemaElement se = schemaElementService.getReadOnlySchemaElement(mciSibling.getElementID());
							if (!possibleInputs.contains(se)) {
								possibleInputs.add(se);
							}
						}
					}
				}
			}
		}
		model.addAttribute("possibleInputs", possibleInputs);
		model.addAttribute("mappingId", mappingId);
				
		return "mapping/cell/detailsForm";
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/ajax/save", produces = "application/json; charset=utf-8")
	public @ResponseBody String saveCellDetails(@Valid MappingCell mappingCell, @RequestParam(value="proxyMcis") String[] proxyMcis, @RequestParam int mappingId, BindingResult bindingResult, Model model, Locale locale) {
		
		GsonBuilder bldr = new GsonBuilder();
		Gson gson = bldr.create();
		
		JsonObject result = new JsonObject();
		result.addProperty("success", !bindingResult.hasErrors());
		result.addProperty("errorCount", bindingResult.getErrorCount());
	
		if (bindingResult.hasErrors()) {
			result.add("errors", getJsonErrorList(bindingResult, locale));
		} else {
			MappingCell mc = mappingCellService.createOrLoadCell(mappingId, mappingCell.getId());

			mc.setName(mappingCell.getName());
			mc.setFunction(mappingCell.getFunction());
			mc.setScore(mappingCell.getScore());
			mc.setOutput(mappingCell.getOutput());
			
			Set<Integer> mciIds = new HashSet<Integer>();
			for (String proxyMci : proxyMcis) {
				mciIds.add(Integer.parseInt(proxyMci));
			}
			
			mappingCellService.mergeAndSave(mc, mciIds);

			JsonArray updatedMappingCells = new JsonArray();
			Set<MappingCellInput> inputs = mc.getMappingCellInputs();			
			for (MappingCellInput input : mc.getMappingCellInputs()) {
				JsonObject o = new JsonObject();
				o.addProperty("id", mc.getId());
				o.addProperty("isSplit", inputs.size() > 1);
				o.addProperty("inputId", input.getElementID());
				o.addProperty("outputId", mc.getOutput());
				o.addProperty("score", mc.getScore());

				updatedMappingCells.add(o);
			}
			
			result.addProperty("userActionLogEntry", mc.getUserAnnotationsInSession().getFirst().getId());
			result.add("updatedMappingCells", updatedMappingCells);
		}
		
		return gson.toJson(result);
	}
}
