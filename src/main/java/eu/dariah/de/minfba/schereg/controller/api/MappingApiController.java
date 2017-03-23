package eu.dariah.de.minfba.schereg.controller.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import eu.dariah.de.dariahsp.web.helper.AuthInfoHelper;
import eu.dariah.de.dariahsp.web.model.AuthPojo;
import eu.dariah.de.minfba.core.metamodel.function.interfaces.TransformationFunction;
import eu.dariah.de.minfba.core.metamodel.interfaces.Mapping;
import eu.dariah.de.minfba.core.metamodel.serialization.SerializableMappingContainer;
import eu.dariah.de.minfba.core.metamodel.tracking.ChangeSet;
import eu.dariah.de.minfba.schereg.model.RightsContainer;
import eu.dariah.de.minfba.schereg.service.interfaces.FunctionService;
import eu.dariah.de.minfba.schereg.service.interfaces.MappedConceptService;
import eu.dariah.de.minfba.schereg.service.interfaces.MappingService;

@Controller
@RequestMapping(value="/api/mappings")
public class MappingApiController extends BaseApiController {
	protected static final Logger logger = LoggerFactory.getLogger(MappingApiController.class);
			
	@Autowired protected AuthInfoHelper authInfoHelper;
	
	@Autowired private MappingService mappingService;
	@Autowired private MappedConceptService mappedConceptService;
	@Autowired private FunctionService functionService;
	
	@RequestMapping(method = RequestMethod.GET, value = "")
	public @ResponseBody List<SerializableMappingContainer> getMappings(HttpServletRequest request) {
		AuthPojo auth = authInfoHelper.getAuth(request);
		List<RightsContainer<Mapping>> mappings = mappingService.findAllByAuth(auth, false);
		List<SerializableMappingContainer> result = new ArrayList<SerializableMappingContainer>();
		
		if (mappings!=null) {
			for (RightsContainer<Mapping> m : mappings) {
				SerializableMappingContainer mc = new SerializableMappingContainer();
				mc.setMapping(m.getElement());
				result.add(mc);
			}
		}
		
		return result;
	}
		
	@RequestMapping(method = RequestMethod.GET, value = "/by-source/{sourceId}")
	public @ResponseBody List<SerializableMappingContainer> getMappingsBySource(@PathVariable String sourceId, HttpServletRequest request) {
		AuthPojo auth = authInfoHelper.getAuth(request);
		return this.processMappings(mappingService.findAllByAuthAndSourceId(auth, sourceId));
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/by-target/{targetId}")
	public @ResponseBody List<SerializableMappingContainer> getMappingsByTarget(@PathVariable String targetId, HttpServletRequest request) {
		AuthPojo auth = authInfoHelper.getAuth(request);
		return this.processMappings(mappingService.findAllByAuthAndTargetId(auth, targetId));
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/by-source-and-target/{sourceId}/{targetId}")
	public @ResponseBody SerializableMappingContainer getMappingBySourceAndTarget(@PathVariable String sourceId, @PathVariable String targetId, HttpServletRequest request) {
		AuthPojo auth = authInfoHelper.getAuth(request);
		return this.processMappingWithDetails(mappingService.findByAuthAndSourceAndTargetId(auth, sourceId, targetId));
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/{entityId}")
	public @ResponseBody SerializableMappingContainer getMapping(@PathVariable String entityId, HttpServletRequest request) {
		AuthPojo auth = authInfoHelper.getAuth(request);
		return this.processMappingWithDetails(mappingService.findByIdAndAuth(entityId, auth));
	}
	
	
	private List<SerializableMappingContainer> processMappings(List<RightsContainer<Mapping>> mappings) {
		List<SerializableMappingContainer> result = new ArrayList<SerializableMappingContainer>();
		if (mappings!=null) {
			ChangeSet ch;
			for (RightsContainer<Mapping> m : mappings) {
				SerializableMappingContainer mc = new SerializableMappingContainer();
				
				ch = mappingService.getLatestChangeSetForEntity(m.getId());
				if (ch!=null) {
					m.getElement().setVersionId(ch.getId());
				}
				
				m.getElement().flush();
				
				mc.setMapping(m.getElement());
				result.add(mc);
			}
		}
		return result;
	}
	
	private SerializableMappingContainer processMappingWithDetails(RightsContainer<Mapping> mapping) {
		if (mapping!=null) {
			SerializableMappingContainer result = new SerializableMappingContainer();
			result.setMapping(mapping.getElement()); 
			mapping.getElement().setConcepts(mappedConceptService.findAllByMappingId(mapping.getId(), true));
			mapping.getElement().flush();
			
			ChangeSet ch = mappingService.getLatestChangeSetForEntity(mapping.getId());
			if (ch!=null) {
				mapping.getElement().setVersionId(ch.getId());
			}
			
			mapping.getElement().flush();
			
			result.setGrammars(this.serializeGrammarSources(mapping.getId()));		
			
			Map<String, String> serializedFunctions = new HashMap<String, String>();
			
			List<TransformationFunction> functions = functionService.findByEntityId(mapping.getId());
			for (TransformationFunction f : functions) {
				serializedFunctions.put(f.getId(), f.getFunction());
			}
			
			result.setFunctions(serializedFunctions);
			
			return result;
		}
		return null;
	}
}
