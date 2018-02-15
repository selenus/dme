package de.unibamberg.minf.dme.controller.api;

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

import de.unibamberg.minf.dme.model.RightsContainer;
import de.unibamberg.minf.dme.model.base.Function;
import de.unibamberg.minf.dme.model.base.Grammar;
import de.unibamberg.minf.dme.model.mapping.MappingImpl;
import de.unibamberg.minf.dme.model.mapping.base.Mapping;
import de.unibamberg.minf.dme.model.serialization.MappingContainer;
import de.unibamberg.minf.dme.model.tracking.ChangeSet;
import de.unibamberg.minf.dme.service.interfaces.FunctionService;
import de.unibamberg.minf.dme.service.interfaces.MappedConceptService;
import de.unibamberg.minf.dme.service.interfaces.MappingService;
import eu.dariah.de.dariahsp.model.web.AuthPojo;
import eu.dariah.de.dariahsp.web.AuthInfoHelper;

@Controller
@RequestMapping(value="/api/mappings")
public class MappingApiController extends BaseApiController {
	protected static final Logger logger = LoggerFactory.getLogger(MappingApiController.class);
			
	@Autowired protected AuthInfoHelper authInfoHelper;
	
	@Autowired private MappingService mappingService;
	@Autowired private MappedConceptService mappedConceptService;
	@Autowired private FunctionService functionService;
	
	@RequestMapping(method = RequestMethod.GET, value = "")
	public @ResponseBody List<MappingContainer> getMappings(HttpServletRequest request) {
		AuthPojo auth = authInfoHelper.getAuth(request);
		List<RightsContainer<Mapping>> mappings = mappingService.findAllByAuth(auth, false);
		List<MappingContainer> result = new ArrayList<MappingContainer>();
		
		if (mappings!=null) {
			for (RightsContainer<Mapping> m : mappings) {
				MappingContainer mc = new MappingContainer();
				mc.setMapping((MappingImpl)m.getElement());
				result.add(mc);
			}
		}
		
		return result;
	}
		
	@RequestMapping(method = RequestMethod.GET, value = "/by-source/{sourceId}")
	public @ResponseBody List<MappingContainer> getMappingsBySource(@PathVariable String sourceId, HttpServletRequest request) {
		AuthPojo auth = authInfoHelper.getAuth(request);
		return this.processMappings(mappingService.findAllByAuthAndSourceId(auth, sourceId));
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/by-target/{targetId}")
	public @ResponseBody List<MappingContainer> getMappingsByTarget(@PathVariable String targetId, HttpServletRequest request) {
		AuthPojo auth = authInfoHelper.getAuth(request);
		return this.processMappings(mappingService.findAllByAuthAndTargetId(auth, targetId));
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/by-source-and-target/{sourceId}/{targetId}")
	public @ResponseBody MappingContainer getMappingBySourceAndTarget(@PathVariable String sourceId, @PathVariable String targetId, HttpServletRequest request) {
		AuthPojo auth = authInfoHelper.getAuth(request);
		return this.processMappingWithDetails(mappingService.findByAuthAndSourceAndTargetId(auth, sourceId, targetId));
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/{entityId}")
	public @ResponseBody MappingContainer getMapping(@PathVariable String entityId, HttpServletRequest request) {
		AuthPojo auth = authInfoHelper.getAuth(request);
		return this.processMappingWithDetails(mappingService.findByIdAndAuth(entityId, auth));
	}
	
	
	private List<MappingContainer> processMappings(List<RightsContainer<Mapping>> mappings) {
		List<MappingContainer> result = new ArrayList<MappingContainer>();
		if (mappings!=null) {
			ChangeSet ch;
			for (RightsContainer<Mapping> m : mappings) {
				MappingContainer mc = new MappingContainer();
				
				ch = mappingService.getLatestChangeSetForEntity(m.getId());
				if (ch!=null) {
					m.getElement().setVersionId(ch.getId());
				}
				
				m.getElement().flush();
				
				mc.setMapping((MappingImpl)m.getElement());
				result.add(mc);
			}
		}
		return result;
	}
	
	private MappingContainer processMappingWithDetails(RightsContainer<Mapping> mapping) {
		if (mapping!=null) {
			MappingContainer result = new MappingContainer();
			result.setMapping((MappingImpl)mapping.getElement()); 
			mapping.getElement().setConcepts(mappedConceptService.findAllByMappingId(mapping.getId(), true));
			mapping.getElement().flush();
			
			ChangeSet ch = mappingService.getLatestChangeSetForEntity(mapping.getId());
			if (ch!=null) {
				mapping.getElement().setVersionId(ch.getId());
			}
			
			mapping.getElement().flush();
			
			List<Grammar> grammars = grammarService.getNonPassthroughGrammars(mapping.getId());
			if (grammars!=null && grammars.size()>0) {
				result.setGrammars(new HashMap<String, Grammar>());
				for (Grammar g : grammars) {
					result.getGrammars().put(g.getId(), g);
				}
			}
			
			Map<String, String> serializedFunctions = new HashMap<String, String>();
			
			List<Function> functions = functionService.findByEntityId(mapping.getId());
			for (Function f : functions) {
				serializedFunctions.put(f.getId(), f.getFunction());
			}
			
			result.setFunctions(serializedFunctions);
			
			return result;
		}
		return null;
	}
}
