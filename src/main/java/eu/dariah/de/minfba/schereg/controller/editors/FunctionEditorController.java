package eu.dariah.de.minfba.schereg.controller.editors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import de.dariah.samlsp.model.pojo.AuthPojo;
import de.unibamberg.minf.gtf.DescriptionEngine;
import de.unibamberg.minf.gtf.MainEngine;
import de.unibamberg.minf.gtf.TransformationEngine;
import de.unibamberg.minf.gtf.exception.DataTransformationException;
import de.unibamberg.minf.gtf.exception.GrammarProcessingException;
import de.unibamberg.minf.gtf.result.FunctionExecutionResult;
import de.unibamberg.minf.gtf.transformation.CompiledTransformationFunction;
import de.unibamberg.minf.gtf.transformation.CompiledTransformationFunctionImpl;
import de.unibamberg.minf.gtf.transformation.processing.params.OutputParam;
import edu.stanford.nlp.ling.CoreAnnotations.ForcedSentenceEndAnnotation;
import eu.dariah.de.minfba.core.metamodel.Label;
import eu.dariah.de.minfba.core.metamodel.function.DescriptionGrammarImpl;
import eu.dariah.de.minfba.core.metamodel.function.TransformationFunctionImpl;
import eu.dariah.de.minfba.core.metamodel.function.interfaces.DescriptionGrammar;
import eu.dariah.de.minfba.core.metamodel.function.interfaces.TransformationFunction;
import eu.dariah.de.minfba.core.metamodel.interfaces.Element;
import eu.dariah.de.minfba.core.metamodel.interfaces.Identifiable;
import eu.dariah.de.minfba.core.metamodel.interfaces.MappedConcept;
import eu.dariah.de.minfba.core.metamodel.interfaces.Mapping;
import eu.dariah.de.minfba.core.metamodel.interfaces.Schema;
import eu.dariah.de.minfba.core.metamodel.mapping.MappedConceptImpl;
import eu.dariah.de.minfba.core.web.pojo.ModelActionPojo;
import eu.dariah.de.minfba.processing.ElementProcessor;
import eu.dariah.de.minfba.schereg.controller.base.BaseFunctionController;
import eu.dariah.de.minfba.schereg.pojo.TreeElementPojo;
import eu.dariah.de.minfba.schereg.serialization.Reference;
import eu.dariah.de.minfba.schereg.service.interfaces.ElementService;
import eu.dariah.de.minfba.schereg.service.interfaces.FunctionService;
import eu.dariah.de.minfba.schereg.service.interfaces.GrammarService;
import eu.dariah.de.minfba.schereg.service.interfaces.MappedConceptService;
import eu.dariah.de.minfba.schereg.service.interfaces.ReferenceService;
import eu.dariah.de.minfba.schereg.service.interfaces.SchemaService;

@Controller
@RequestMapping(value={"/schema/editor/{entityId}/function/{functionId}",
		"/mapping/editor/{entityId}/function/{functionId}"})
public class FunctionEditorController extends BaseFunctionController {
	@Autowired private ReferenceService referenceService;
	@Autowired private FunctionService functionService;
	@Autowired private GrammarService grammarService;
	@Autowired private ElementService elementService;
	
	@Autowired private SchemaService schemaService;
	@Autowired private MappedConceptService mappedConceptService;
	
	@Autowired private MainEngine mainEngine;

	
	public FunctionEditorController() {
		super("schemaEditor");
	}	
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/remove")
	public @ResponseBody TransformationFunction removeElement(@PathVariable String entityId, @PathVariable String functionId, HttpServletRequest request) {
		return functionService.deleteFunctionById(entityId, functionId, authInfoHelper.getAuth(request));
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/get")
	public @ResponseBody TransformationFunction getElement(@PathVariable String entityId, @PathVariable String functionId) {
		return functionService.findById(functionId);
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/form/editWdata")
	public String getEditFormWithData(@PathVariable String entityId, @PathVariable String functionId, @RequestBody JsonNode jsonNode, HttpServletRequest request, Model model, Locale locale) {
		AuthPojo auth = authInfoHelper.getAuth(request);
		Identifiable entity = this.getEntity(entityId);
		
		Map<String, String> providedSamples = new HashMap<String, String>();
		ArrayNode samples = (ArrayNode)jsonNode.path("samples");
		for (JsonNode n : samples) {
			String text = n.path("text").textValue();
			if (text!=null) {
				providedSamples.put(n.path("elementId").textValue(), text);
			}
		}
		
		Map<Element, String> sampleInputs = new LinkedHashMap<Element, String>();
		List<Object> inputElementIds = new ArrayList<Object>();
		List<Object> inputGrammarIds = new ArrayList<Object>();
		
		if (Schema.class.isAssignableFrom(entity.getClass())) {
			String grammarId = referenceService.findReferenceByChildId(entityId, functionId).getId();
			model.addAttribute("grammar", grammarService.findById(grammarId));
			
			String elementId = referenceService.findReferenceByChildId(entityId, grammarId).getId();
			Element e = elementService.findById(elementId);
			
			inputGrammarIds.add(grammarId);
			if (providedSamples.containsKey(e.getId())) {
				sampleInputs.put(e, providedSamples.get(e.getId()));
			} else {
				sampleInputs.put(e, sessionService.getSampleInputValue(e.getId(), entityId, request.getSession().getId(), auth.getUserId()));
			}
			
		} else { // Mapping
			Reference parentConceptReference = referenceService.findReferenceByChildId(entity.getId(), functionId);
			MappedConcept mc = mappedConceptService.findById(parentConceptReference.getId());
			
			for (String elementId : mc.getElementGrammarIdsMap().keySet()) {
				inputElementIds.add(elementId);
				inputGrammarIds.add(mc.getElementGrammarIdsMap().get(elementId));
			}
			
			for (Element e : elementService.findByIds(inputElementIds) ){
				if (providedSamples.containsKey(e.getId())) {
					sampleInputs.put(e, providedSamples.get(e.getId()));
				} else {
					sampleInputs.put(e, sessionService.getSampleInputValue(e.getId(), entityId, request.getSession().getId(), auth.getUserId()));
				}
			}
		}
		
		List<DescriptionGrammar> grammars = grammarService.findByIds(inputGrammarIds);
		List<String> availableRules = new ArrayList<String>();
		List<String> availablePassthroughGrammars = new ArrayList<String>();
		for (DescriptionGrammar g : grammars) {
			if (g.isPassthrough()) {
				availablePassthroughGrammars.add("@" + g.getGrammarName());
			} else {
				try {
					for (String rule : mainEngine.getDescriptionEngine().getParserRuleNames(g)) {
						if (!availableRules.contains("@" + rule)) {
							availableRules.add("@" + rule);
						}
					}
				} catch (GrammarProcessingException e) {
					logger.error(String.format("Failed to retrieve parser rules for grammar %s", g.getIdentifier()), e);
				}
			}
		}
		
		Collections.sort(availableRules);
		
		model.addAttribute("availableRules", availableRules);
		model.addAttribute("availablePassthroughGrammars", availablePassthroughGrammars);
		
		model.addAttribute("sampleInputMap", sampleInputs);		
		model.addAttribute("function", functionService.findById(functionId));
		model.addAttribute("readonly", this.getIsReadOnly(entity, auth.getUserId()));
		model.addAttribute("actionPath", "/schema/editor/" + entityId + "/function/" + functionId + "/async/save");
		return "schemaEditor/form/function/edit";
		
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/form/edit")
	public String getEditForm(@PathVariable String entityId, @PathVariable String functionId, HttpServletRequest request, Model model, Locale locale) {
		return this.getEditFormWithData(entityId, functionId, null, request, model, locale);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/process")
	public String validateFunction(Model model, Locale locale) {		
		return "schemaEditor/form/function/process";
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/async/validate")
	public @ResponseBody ModelActionPojo validateFunction(@PathVariable String entityId, @PathVariable String functionId, @RequestParam String func) {
		ModelActionPojo result = new ModelActionPojo();
		
		try {
			TransformationFunctionImpl f = new TransformationFunctionImpl(entityId, functionId);
			f.setFunction(func);
			
			CompiledTransformationFunction fCompiled = mainEngine.getTransformationEngine().compileOutputFunction(f, true);
			if (!fCompiled.isEmpty()) {
				result.setPojo(((CompiledTransformationFunctionImpl)fCompiled).getSvg());
				result.setObjectErrors(((CompiledTransformationFunctionImpl)fCompiled).getErrors());
			}
			
			result.setSuccess(true);
		} catch (Exception e) {	}
		return result;
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/async/save")
	public @ResponseBody ModelActionPojo saveFunction(@PathVariable String entityId, @PathVariable String functionId, @Valid TransformationFunctionImpl function, BindingResult bindingResult, Locale locale, HttpServletRequest request) {
		ModelActionPojo result = this.getActionResult(bindingResult, locale);
		if (!result.isSuccess()) {
			return result;
		}
		if (function.getId().isEmpty()) {
			function.setId(null);
		}
		
		TransformationFunction fSave = null;
		if (function.getId()!=null) {
			fSave = functionService.findById(function.getId());
			if (fSave!=null) {
				fSave.setError(function.isError());
				fSave.setFunction(function.getFunction());
				fSave.setName(function.getName());
				fSave.setEntityId(function.getEntityId());
			}
		}
		if (fSave==null) {
			fSave = function;
		}
		
		
		if (!fSave.getFunction().trim().isEmpty()) {
			ModelActionPojo validationResult = this.validateFunction(entityId, functionId, fSave.getFunction());
			if (validationResult.isSuccess() && !validationResult.hasErrors()) {
				fSave.setError(false);
			} else {
				fSave.setError(true);
			}		
		}
		
		functionService.saveFunction((TransformationFunctionImpl)fSave, authInfoHelper.getAuth(request));
		return result;
	}
	
	private String getSampleByElementId(String elementId, ArrayNode elementIds, ArrayNode samples) {
		for (int i=0; i<elementIds.size(); i++) {
			if (elementIds.get(i).textValue().equals(elementId) && i<samples.size()) {
				return samples.get(i).textValue();
			}
		}
		return "";
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/async/parseSample")
	public @ResponseBody ModelActionPojo parseSampleInput(@PathVariable String entityId, @PathVariable String functionId, @RequestBody JsonNode jsonNode, Locale locale) {
	
		Map<String, String> providedSamples = new HashMap<String, String>();
		
		String func = jsonNode.path("func").textValue();
		
		ArrayNode samples = (ArrayNode)jsonNode.path("samples");
		for (JsonNode n : samples) {
			String text = n.path("text").textValue();
			if (text!=null && !text.isEmpty()) {
				providedSamples.put(n.path("elementId").textValue(), text);
			}
		}
		
		Identifiable entity = this.getEntity(entityId);

		TransformationFunctionImpl f;
		ModelActionPojo result = new ModelActionPojo();

		List<String> values = new ArrayList<String>();
		List<DescriptionGrammar> grammars = new ArrayList<DescriptionGrammar>();
				
		if (Schema.class.isAssignableFrom(entity.getClass())) {
			String grammarId = referenceService.findReferenceBySchemaAndChildId(entityId, functionId).getId();
			DescriptionGrammar g = grammarService.findById(grammarId);
			
			String elementId = referenceService.findReferenceByChildId(entityId, grammarId).getId();
			
			values.add(providedSamples.containsKey(elementId) ? providedSamples.get(elementId) : null);
			grammars.add(g);
			
			f = (TransformationFunctionImpl)elementService.getElementSubtree(entityId, functionId);
		} else { // Mappings
			Mapping m = (Mapping)entity;
			Schema target = schemaService.findSchemaById(m.getTargetId());
			
			Reference parentConceptReference = referenceService.findReferenceByChildId(entity.getId(), functionId);
			MappedConcept mc = mappedConceptService.findById(parentConceptReference.getId());
			List<Identifiable> targetElements = elementService.getElementTrees(target.getId(), mc.getTargetElementIds());
			
			for (String elementId : mc.getElementGrammarIdsMap().keySet()) {
				grammars.add(grammarService.findById(mc.getElementGrammarIdsMap().get(elementId)));
				values.add(providedSamples.containsKey(elementId) ? providedSamples.get(elementId) : null);
			}

			f = (TransformationFunctionImpl)functionService.findById(functionId);
			
			//f = new TransformationFunctionImpl(entityId, functionId);
			f.setOutputElements(elementService.convertToLabels(targetElements));
		}
				
		if (func != null) {
			f.setFunction(func);
		}
		
		try {
			FunctionExecutionResult pResult = mainEngine.processValues(values, grammars, f);
			result.setSuccess(true);
			
			boolean allMatched = true;
			if (pResult!=null && pResult.getOutputParams()!=null && pResult.getOutputParams().size()>0) {
				List<TreeElementPojo> resultPojos = new ArrayList<TreeElementPojo>();
				MutableBoolean pMatch = new MutableBoolean(true);
				for(OutputParam p : pResult.getOutputParams()) {
					resultPojos.add(this.convertOutputParamToPojo(p, f.getOutputElements(), pMatch));
					allMatched = allMatched && pMatch.booleanValue();
				}
				result.setPojo(resultPojos);
				if (!allMatched) {
					result.addObjectWarning(messageSource.getMessage("~eu.dariah.de.minfba.schereg.model.function.validation.labels_not_found", null, locale));
				}
			}
		} catch (Exception e) {
			logger.error("Error performing sample transformation", e);
		}
		
		
		return result;
	}
	
	private TreeElementPojo convertOutputParamToPojo(OutputParam param, List<Label> outputElements, MutableBoolean allMatched) {
		if (param==null) {
			return null;
		}
		TreeElementPojo pojo = new TreeElementPojo();
		pojo.setLabel(param.getLabel());
		pojo.setValue(param.getValue());
		
		List<Label> sublabels = null;
		if (outputElements==null) {
			allMatched.setFalse();
		} else {
			boolean match = false;
			for (Label l : outputElements) {
				if (l.getName().equals(param.getLabel())) {
					sublabels = l.getSubLabels();
					match = true;
					break;
				}
			}
			if (match == false) {
				allMatched.setFalse();
			}
		}
		
		if (param.getChildParameters()!=null) {
			pojo.setChildren(new ArrayList<TreeElementPojo>());
			for (OutputParam childParam : param.getChildParameters()) {
				pojo.getChildren().add(this.convertOutputParamToPojo(childParam, sublabels, allMatched));
			}
		}		
		return pojo;
	}
}
