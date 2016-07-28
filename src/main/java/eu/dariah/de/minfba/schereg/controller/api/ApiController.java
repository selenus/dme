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
import eu.dariah.de.minfba.core.metamodel.interfaces.Element;
import eu.dariah.de.minfba.core.metamodel.interfaces.Mapping;
import eu.dariah.de.minfba.core.metamodel.interfaces.Schema;
import eu.dariah.de.minfba.core.metamodel.serialization.SerializableMappingContainer;
import eu.dariah.de.minfba.core.metamodel.serialization.SerializableSchemaContainer;
import eu.dariah.de.minfba.core.metamodel.tracking.ChangeSet;
import eu.dariah.de.minfba.core.metamodel.xml.XmlSchema;
import eu.dariah.de.minfba.schereg.model.RightsContainer;
import eu.dariah.de.minfba.schereg.service.interfaces.ElementService;
import eu.dariah.de.minfba.schereg.service.interfaces.MappedConceptService;
import eu.dariah.de.minfba.schereg.service.interfaces.MappingService;
import eu.dariah.de.minfba.schereg.service.interfaces.SchemaService;

@Controller
@RequestMapping(value="/api/")
public class ApiController {
	protected static final Logger logger = LoggerFactory.getLogger(ApiController.class);
	
	@Autowired protected AuthInfoHelper authInfoHelper;
	
	@Autowired private SchemaService schemaService;
	@Autowired protected ElementService elementService;
	
	@Autowired private MappingService mappingService;
	@Autowired private MappedConceptService mappedConceptService;
	
	
	@RequestMapping(method = RequestMethod.GET, value = "/mappings")
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
	
	@RequestMapping(method = RequestMethod.GET, value = "/mappings/{entityId}")
	public @ResponseBody SerializableMappingContainer getMapping(@PathVariable String entityId, HttpServletRequest request) {
		AuthPojo auth = authInfoHelper.getAuth(request);
		RightsContainer<Mapping> mapping = mappingService.findByIdAndAuth(entityId, auth);
		if (mapping!=null) {
			SerializableMappingContainer result = new SerializableMappingContainer();
			result.setMapping(mapping.getElement()); 
			mapping.getElement().setConcepts(mappedConceptService.findAllByMappingId(entityId));
			mapping.getElement().flush();
			// TODO: Include grammars
			return result;
		}
		return null;
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/schemas")
	public @ResponseBody List<SerializableSchemaContainer> getSchemas(HttpServletRequest request) {
		AuthPojo auth = authInfoHelper.getAuth(request);
		List<RightsContainer<Schema>> schemas = schemaService.findAllByAuth(auth);
		List<SerializableSchemaContainer> result = new ArrayList<SerializableSchemaContainer>();
		ChangeSet ch;
		SerializableSchemaContainer sp;
		if (schemas!=null) {
			for (RightsContainer<Schema> s : schemas) {
				if (s.getElement() instanceof XmlSchema) {
					XmlSchema xmlS = ((XmlSchema)s.getElement());
					xmlS.setTerminals(null);
					xmlS.setNamespaces(null);
					xmlS.setRootElementName(null);
					xmlS.setRootElementNamespace(null);
					xmlS.setRecordPath(null);
				}
												
				ch = schemaService.getLatestChangeSetForEntity(s.getId());
				if (ch!=null) {
					s.getElement().setVersionId(ch.getId());
				}
				
				s.getElement().flush();
				
				sp = new SerializableSchemaContainer();
				sp.setSchema(s.getElement());
				
				result.add(sp);
			}
		}
		return result;
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/schemas/{entityId}")
	public @ResponseBody SerializableSchemaContainer exportSchema(@PathVariable String entityId, HttpServletRequest request) {
		AuthPojo auth = authInfoHelper.getAuth(request);
		RightsContainer<Schema> s = schemaService.findByIdAndAuth(entityId, auth);
		Element r = elementService.findRootBySchemaId(entityId, true);
		
		SerializableSchemaContainer sp = new SerializableSchemaContainer();
		sp.setSchema(s.getElement());
		
		ChangeSet ch = schemaService.getLatestChangeSetForEntity(s.getId());
		if (ch!=null) {
			s.getElement().setVersionId(ch.getId());
		}
		
		sp.setRoot(r);
		
		return sp;
	}
}
