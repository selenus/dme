package eu.dariah.de.minfba.schereg.controller.editors;

import java.util.ArrayList;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;

import de.dariah.aai.javasp.web.helper.AuthInfoHelper;
import de.dariah.samlsp.model.pojo.AuthPojo;
import de.unibamberg.minf.gtf.TransformationEngine;
import de.unibamberg.minf.gtf.exception.DataTransformationException;
import de.unibamberg.minf.gtf.exception.GrammarProcessingException;
import de.unibamberg.minf.gtf.transformation.CompiledTransformationFunction;
import de.unibamberg.minf.gtf.transformation.CompiledTransformationFunctionImpl;
import de.unibamberg.minf.gtf.transformation.processing.params.OutputParam;
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
import eu.dariah.de.minfba.core.web.controller.BaseTranslationController;
import eu.dariah.de.minfba.core.web.pojo.ModelActionPojo;
import eu.dariah.de.minfba.schereg.controller.base.BaseFunctionController;
import eu.dariah.de.minfba.schereg.controller.base.BaseScheregController;
import eu.dariah.de.minfba.schereg.model.PersistedSession;
import eu.dariah.de.minfba.schereg.pojo.TreeElementPojo;
import eu.dariah.de.minfba.schereg.serialization.Reference;
import eu.dariah.de.minfba.schereg.service.interfaces.ElementService;
import eu.dariah.de.minfba.schereg.service.interfaces.FunctionService;
import eu.dariah.de.minfba.schereg.service.interfaces.GrammarService;
import eu.dariah.de.minfba.schereg.service.interfaces.MappedConceptService;
import eu.dariah.de.minfba.schereg.service.interfaces.MappingService;
import eu.dariah.de.minfba.schereg.service.interfaces.PersistedSessionService;
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
	
	@Autowired private TransformationEngine engine;
	@Autowired private PersistedSessionService sessionService;
	
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
	
	@RequestMapping(method = RequestMethod.GET, value = "/form/edit")
	public String getEditForm(@PathVariable String entityId, @PathVariable String functionId, HttpServletRequest request, Model model, Locale locale) {
		AuthPojo auth = authInfoHelper.getAuth(request);
		Identifiable entity = this.getEntity(entityId);
		
		List<String> grammarClasses = new ArrayList<String>();
		grammarClasses.add(DescriptionGrammarImpl.class.getName());
		grammarClasses.add(DescriptionGrammar.class.getName());
		
		String grammarId = referenceService.findReferenceByChildId(entityId, functionId, grammarClasses).getId();
		if (!grammarId.equals(entityId)) { // Happens for mappings
			model.addAttribute("grammar", grammarService.findById(grammarId));
		}
		
		model.addAttribute("elementSample", this.getSampleInputValue(entity, functionId, request.getSession().getId(), auth.getUserId()));
		model.addAttribute("function", functionService.findById(functionId));
		model.addAttribute("readonly", this.getIsReadOnly(entity, auth.getUserId()));
		model.addAttribute("actionPath", "/schema/editor/" + entityId + "/function/" + functionId + "/async/save");
		return "schemaEditor/form/function/edit";
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
			
			CompiledTransformationFunction fCompiled = engine.compileOutputFunction(f, true);
			
			if (fCompiled instanceof CompiledTransformationFunctionImpl) {
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
	
	@RequestMapping(method = RequestMethod.POST, value = "/async/parseSample")
	public @ResponseBody ModelActionPojo parseSampleInput(@PathVariable String entityId, @PathVariable String functionId, @RequestParam String func, @RequestParam String sample, Locale locale) {
		String grammarId = referenceService.findReferenceBySchemaAndChildId(entityId, functionId).getId();
		DescriptionGrammar g = grammarService.findById(grammarId);
		
		Identifiable entity = this.getEntity(entityId);

		TransformationFunctionImpl f = new TransformationFunctionImpl(entityId, functionId);
		f.setFunction(func);
		f.setId(functionId);
		
		ModelActionPojo result = new ModelActionPojo();
		
		TransformationFunctionImpl fLoaded;
		if (entity instanceof Schema) {
			fLoaded = (TransformationFunctionImpl)elementService.getElementSubtree(entityId, functionId);
		} else {
			fLoaded = f;
			
			Mapping m = (Mapping)entity;
			Schema target = schemaService.findSchemaById(m.getTargetId());
			
			Reference rGrammar = referenceService.findReferenceByChildId(m.getId(), functionId);
			Reference rMappedConcept = referenceService.findReferenceByChildId(m.getId(), rGrammar.getId());
			
			MappedConcept c = mappedConceptService.findById(rMappedConcept.getId()); 
			
			List<Identifiable> targetElements = elementService.getElementTrees(target.getId(), c.getTargetElementIds());
			
			f.setOutputElements(elementService.convertToLabels(targetElements));
		}
		
		try {
			engine.checkGrammar(g);
			List<OutputParam> pResult = engine.process(sample, g, f);
			result.setSuccess(true);
			
			boolean allMatched = true;
			if (pResult!=null && pResult.size()>0) {
				List<TreeElementPojo> resultPojos = new ArrayList<TreeElementPojo>();
				MutableBoolean pMatch = new MutableBoolean(true);
				for(OutputParam p : pResult) {
					resultPojos.add(this.convertOutputParamToPojo(p, fLoaded.getOutputElements(), pMatch));
					allMatched = allMatched && pMatch.booleanValue();
				}
				result.setPojo(resultPojos);
				if (!allMatched) {
					result.addObjectWarning(messageSource.getMessage("~eu.dariah.de.minfba.schereg.model.function.validation.labels_not_found", null, locale));
				}
			}
		} catch (GrammarProcessingException | DataTransformationException e) {
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
