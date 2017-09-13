package eu.dariah.de.minfba.schereg.controller.editors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
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

import de.unibamberg.minf.dme.model.base.Element;
import de.unibamberg.minf.dme.model.base.Function;
import de.unibamberg.minf.dme.model.base.Grammar;
import de.unibamberg.minf.dme.model.base.Identifiable;
import de.unibamberg.minf.dme.model.base.Label;
import de.unibamberg.minf.dme.model.datamodel.base.Datamodel;
import de.unibamberg.minf.dme.model.function.FunctionImpl;
import de.unibamberg.minf.dme.model.mapping.base.MappedConcept;
import de.unibamberg.minf.dme.model.mapping.base.Mapping;
import de.unibamberg.minf.gtf.MainEngine;
import de.unibamberg.minf.gtf.exceptions.GrammarProcessingException;
import de.unibamberg.minf.gtf.result.FunctionExecutionResult;
import de.unibamberg.minf.gtf.syntaxtree.SyntaxTreeNode;
import de.unibamberg.minf.gtf.syntaxtree.TerminalSyntaxTreeNode;
import de.unibamberg.minf.gtf.transformation.CompiledTransformationFunction;
import de.unibamberg.minf.gtf.transformation.CompiledTransformationFunctionImpl;
import de.unibamberg.minf.gtf.transformation.processing.params.OutputParam;
import eu.dariah.de.dariahsp.model.web.AuthPojo;
import eu.dariah.de.minfba.core.web.pojo.ModelActionPojo;
import eu.dariah.de.minfba.schereg.controller.base.BaseFunctionController;
import eu.dariah.de.minfba.schereg.model.PersistedSession;
import eu.dariah.de.minfba.schereg.pojo.TreeElementPojo;
import eu.dariah.de.minfba.schereg.serialization.Reference;
import eu.dariah.de.minfba.schereg.service.interfaces.ElementService;
import eu.dariah.de.minfba.schereg.service.interfaces.FunctionService;
import eu.dariah.de.minfba.schereg.service.interfaces.GrammarService;
import eu.dariah.de.minfba.schereg.service.interfaces.MappedConceptService;
import eu.dariah.de.minfba.schereg.service.interfaces.ReferenceService;

@Controller
@RequestMapping(value={"/model/editor/{entityId}/function/{functionId}",
		"/mapping/editor/{entityId}/function/{functionId}"})
public class FunctionEditorController extends BaseFunctionController {
	@Autowired private ReferenceService referenceService;
	@Autowired private FunctionService functionService;
	@Autowired private GrammarService grammarService;
	@Autowired private ElementService elementService;
	
	@Autowired private MappedConceptService mappedConceptService;
	
	@Autowired private MainEngine mainEngine;

	
	public FunctionEditorController() {
		super("schemaEditor");
	}	
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method = RequestMethod.GET, value = "/async/remove")
	public @ResponseBody Function removeElement(@PathVariable String entityId, @PathVariable String functionId, HttpServletRequest request, HttpServletResponse response) {
		AuthPojo auth = authInfoHelper.getAuth(request);
		if(!schemaService.getUserCanWriteEntity(entityId, auth.getUserId())) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return null;
		}
		return functionService.deleteFunctionById(entityId, functionId, authInfoHelper.getAuth(request));
	}
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method = RequestMethod.GET, value = "/async/disable")
	public @ResponseBody ModelActionPojo disableElement(@PathVariable String entityId, @PathVariable String functionId, @RequestParam boolean disabled, HttpServletRequest request, HttpServletResponse response) {
		if (!schemaService.getUserCanWriteEntity(entityId, authInfoHelper.getAuth(request).getUserId())) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return new ModelActionPojo(false);
		}
		
		FunctionImpl f = (FunctionImpl)functionService.findById(functionId);
		f.setDisabled(disabled);
		
		functionService.saveFunction(f, authInfoHelper.getAuth(request));
		return new ModelActionPojo(true);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/get")
	public @ResponseBody Function getElement(@PathVariable String entityId, @PathVariable String functionId) {
		return functionService.findById(functionId);
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/form/editWdata")
	public String getEditFormWithData(@PathVariable String entityId, @PathVariable String functionId, @RequestBody JsonNode jsonNode, HttpServletRequest request, HttpServletResponse response, Model model, Locale locale) {
		AuthPojo auth = authInfoHelper.getAuth(request);
		Identifiable entity = this.getEntity(entityId);
		
		PersistedSession s = sessionService.access(entityId, request.getSession().getId(), authInfoHelper.getUserId(request));
		if (s==null) {
			response.setStatus(HttpServletResponse.SC_RESET_CONTENT);
			return null;
		}
		
		Map<String, String> providedSamples = new HashMap<String, String>();
		if (jsonNode!=null) {
			ArrayNode samples = (ArrayNode)jsonNode.path("samples");
			for (JsonNode n : samples) {
				String text = n.path("text").textValue();
				if (text!=null) {
					providedSamples.put(n.path("elementId").textValue(), text);
				}
			}
		}
		
		Map<Element, String> sampleInputs = new LinkedHashMap<Element, String>();
		List<Object> inputElementIds = new ArrayList<Object>();
		List<Object> inputGrammarIds = new ArrayList<Object>();
		
		if (Datamodel.class.isAssignableFrom(entity.getClass())) {
			String grammarId = referenceService.findReferenceByChildId(entityId, functionId).getId();
			model.addAttribute("grammar", grammarService.findById(grammarId));
			
			String elementId = referenceService.findReferenceByChildId(entityId, grammarId).getId();
			Element e = elementService.findById(elementId);
			
			inputGrammarIds.add(grammarId);
			if (providedSamples.containsKey(e.getId())) {
				sampleInputs.put(e, providedSamples.get(e.getId()));
			} else {
				sampleInputs.put(e, sessionService.getSampleInputValue(s, elementId));
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
					sampleInputs.put(e, sessionService.getSampleInputValue(s, e.getId()));
				}
			}
		}
		
		List<Grammar> grammars = grammarService.findByIds(inputGrammarIds);
		List<String> availableRules = new ArrayList<String>();
		List<String> availablePassthroughGrammars = new ArrayList<String>();
		for (Grammar g : grammars) {
			if (g.isPassthrough()) {
				availablePassthroughGrammars.add("@" + g.getName());
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
		model.addAttribute("actionPath", "/model/editor/" + entityId + "/function/" + functionId + "/async/save");
		return "functionEditor/form/edit";
		
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/form/edit")
	public String getEditForm(@PathVariable String entityId, @PathVariable String functionId, HttpServletRequest request, HttpServletResponse response, Model model, Locale locale) {
		return this.getEditFormWithData(entityId, functionId, null, request, response, model, locale);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/process")
	public String validateFunction(Model model, Locale locale) {		
		return "functionEditor/form/process";
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/async/validate")
	public @ResponseBody ModelActionPojo validateFunction(@PathVariable String entityId, @PathVariable String functionId, @RequestParam String func) {
		ModelActionPojo result = new ModelActionPojo();
		
		try {
			FunctionImpl f = new FunctionImpl(entityId, functionId);
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
	
	@PreAuthorize("isAuthenticated()")
	@RequestMapping(method = RequestMethod.POST, value = "/async/save")
	public @ResponseBody ModelActionPojo saveFunction(@PathVariable String entityId, @PathVariable String functionId, @Valid FunctionImpl function, BindingResult bindingResult, Locale locale, HttpServletRequest request, HttpServletResponse response) {
		AuthPojo auth = authInfoHelper.getAuth(request);
		if(!schemaService.getUserCanWriteEntity(entityId, auth.getUserId())) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
			return null;
		}
		
		ModelActionPojo result = this.getActionResult(bindingResult, locale);
		if (!result.isSuccess()) {
			return result;
		}
		if (function.getId().isEmpty()) {
			function.setId(null);
		}
		
		Function fSave = null;
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
		
		functionService.saveFunction((FunctionImpl)fSave, authInfoHelper.getAuth(request));
		return result;
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

		FunctionImpl f;
		ModelActionPojo result = new ModelActionPojo();

		List<SyntaxTreeNode> values = new ArrayList<SyntaxTreeNode>();
		List<Grammar> grammars = new ArrayList<Grammar>();
				
		if (Datamodel.class.isAssignableFrom(entity.getClass())) {
			String grammarId = referenceService.findReferenceBySchemaAndChildId(entityId, functionId).getId();
			Grammar g = grammarService.findById(grammarId);
			
			String elementId = referenceService.findReferenceByChildId(entityId, grammarId).getId();
			//Element e = elementService.findById(elementId);
			
			values.add(new TerminalSyntaxTreeNode(providedSamples.containsKey(elementId) ? providedSamples.get(elementId) : null, null));
			grammars.add(g);
			
			f = (FunctionImpl)elementService.getElementSubtree(entityId, functionId);
		} else { // Mappings
			Mapping m = (Mapping)entity;
			Datamodel target = schemaService.findSchemaById(m.getTargetId());
			
			Reference parentConceptReference = referenceService.findReferenceByChildId(entity.getId(), functionId);
			MappedConcept mc = mappedConceptService.findById(parentConceptReference.getId());
			List<Identifiable> targetElements = elementService.getElementTrees(target.getId(), mc.getTargetElementIds());
			
			for (String elementId : mc.getElementGrammarIdsMap().keySet()) {
				grammars.add(grammarService.findById(mc.getElementGrammarIdsMap().get(elementId)));
				
				//Element e = elementService.findById(elementId);
				
				
				values.add(new TerminalSyntaxTreeNode(providedSamples.containsKey(elementId) ? providedSamples.get(elementId) : null, null));
			}

			f = (FunctionImpl)functionService.findById(functionId);
			
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
