package eu.dariah.de.minfba.schereg.controller.api;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import de.dariah.aai.javasp.web.helper.AuthInfoHelper;
import de.dariah.samlsp.model.pojo.AuthPojo;
import eu.dariah.de.minfba.core.metamodel.interfaces.Mapping;
import eu.dariah.de.minfba.core.metamodel.serialization.SerializableMappingContainer;
import eu.dariah.de.minfba.schereg.model.RightsContainer;
import eu.dariah.de.minfba.schereg.service.interfaces.MappedConceptService;
import eu.dariah.de.minfba.schereg.service.interfaces.MappingService;

@Controller
@RequestMapping(value="/api/mappings")
public class MappingApiController {
	protected static final Logger logger = LoggerFactory.getLogger(MappingApiController.class);
			
	@Autowired protected AuthInfoHelper authInfoHelper;
	
	@Autowired private MappingService mappingService;
	@Autowired private MappedConceptService mappedConceptService;
	
	
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
		return this.processMapping(mappingService.findByAuthAndSourceAndTargetId(auth, sourceId, targetId));
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/{entityId}")
	public @ResponseBody SerializableMappingContainer getMapping(@PathVariable String entityId, HttpServletRequest request) {
		AuthPojo auth = authInfoHelper.getAuth(request);
		return this.processMapping(mappingService.findByIdAndAuth(entityId, auth));
	}
	
	
	private List<SerializableMappingContainer> processMappings(List<RightsContainer<Mapping>> mappings) {
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
	
	private SerializableMappingContainer processMapping(RightsContainer<Mapping> mapping) {
		if (mapping!=null) {
			SerializableMappingContainer result = new SerializableMappingContainer();
			result.setMapping(mapping.getElement()); 
			mapping.getElement().setConcepts(mappedConceptService.findAllByMappingId(mapping.getId()));
			mapping.getElement().flush();
			// TODO: Include grammars
			return result;
		}
		return null;
	}
}
