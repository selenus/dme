package eu.dariah.de.minfba.schereg.controller.schemaeditor;

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
import de.unibamberg.minf.gtf.TransformationEngine;
import de.unibamberg.minf.gtf.exception.DataTransformationException;
import de.unibamberg.minf.gtf.exception.GrammarProcessingException;
import de.unibamberg.minf.gtf.transformation.CompiledTransformationFunction;
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
@RequestMapping(value={"/schema/editor/{schemaId}/function/{functionId}",
		"/mapping/editor/{schemaId}/function/{functionId}"})
public class FunctionEditorController extends BaseScheregController {
	@Autowired private ReferenceService referenceService;
	@Autowired private FunctionService functionService;
	@Autowired private GrammarService grammarService;
	@Autowired private ElementService elementService;
	
	@Autowired private SchemaService schemaService;
	@Autowired private MappingService mappingService;
	
	@Autowired private MappedConceptService mappedConceptService;
	
	@Autowired private TransformationEngine engine;
	@Autowired private PersistedSessionService sessionService;
	
	public FunctionEditorController() {
		super("schemaEditor");
	}	
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/remove")
	public @ResponseBody TransformationFunction removeElement(@PathVariable String schemaId, @PathVariable String functionId, HttpServletRequest request) {
		return functionService.deleteFunctionById(schemaId, functionId, authInfoHelper.getAuth(request));
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/get")
	public @ResponseBody TransformationFunction getElement(@PathVariable String schemaId, @PathVariable String functionId) {
		return functionService.findById(functionId);
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/form/edit")
	public String getEditForm(@PathVariable String schemaId, @PathVariable String functionId, HttpServletRequest request, Model model, Locale locale) {
		String grammarId = referenceService.findReferenceBySchemaAndChildId(schemaId, functionId).getId();
		
		PersistedSession s = sessionService.access(schemaId, request.getSession().getId(), authInfoHelper.getUserId(request));
		if (s.getSelectedValueMap()!=null) {
			String elementId = referenceService.findReferenceBySchemaAndChildId(schemaId, grammarId).getId();
			if (s.getSelectedValueMap().containsKey(elementId)) {
				model.addAttribute("elementSample", s.getSelectedValueMap().get(elementId));
			}
		}
		
		model.addAttribute("grammar", grammarService.findById(grammarId));
		model.addAttribute("function", functionService.findById(functionId));
		model.addAttribute("actionPath", "/schema/editor/" + schemaId + "/function/" + functionId + "/async/save");
		return "schemaEditor/form/function/edit";
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/async/process")
	public String validateFunction(Model model, Locale locale) {		
		return "schemaEditor/form/function/process";
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/async/validate")
	public @ResponseBody ModelActionPojo validateFunction(@PathVariable String schemaId, @PathVariable String functionId, @RequestParam String func) {
		ModelActionPojo result = new ModelActionPojo();
		
		try {
			TransformationFunctionImpl f = new TransformationFunctionImpl(schemaId, functionId);
			f.setFunction(func);
			
			CompiledTransformationFunction fCompiled = engine.compileOutputFunction(f, true);
			result.setPojo(fCompiled.getSvg());
			result.setObjectErrors(fCompiled.getErrors());
			
			result.setSuccess(true);
		} catch (Exception e) {	}
		return result;
	}
	
	@RequestMapping(method = RequestMethod.POST, value = "/async/save")
	public @ResponseBody ModelActionPojo saveFunction(@PathVariable String schemaId, @PathVariable String functionId, @Valid TransformationFunctionImpl function, BindingResult bindingResult, Locale locale, HttpServletRequest request) {
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
			ModelActionPojo validationResult = this.validateFunction(schemaId, functionId, fSave.getFunction());
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
	public @ResponseBody ModelActionPojo parseSampleInput(@PathVariable String schemaId, @PathVariable String functionId, @RequestParam String func, @RequestParam String sample) {
		String grammarId = referenceService.findReferenceBySchemaAndChildId(schemaId, functionId).getId();
		DescriptionGrammar g = grammarService.findById(grammarId);
		
		Identifiable entity = schemaService.findSchemaById(schemaId);
		if (entity==null) {
			entity=mappingService.findMappingById(schemaId);
		}

		TransformationFunctionImpl f = new TransformationFunctionImpl(schemaId, functionId);
		f.setFunction(func);
		f.setId(functionId);
		
		ModelActionPojo result = new ModelActionPojo();
		
		TransformationFunctionImpl fLoaded;
		if (entity instanceof Schema) {
			fLoaded = (TransformationFunctionImpl)elementService.getElementSubtree(schemaId, functionId);
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
					result.addObjectWarning("~ Not all produced labels are found in the schema definition");
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
