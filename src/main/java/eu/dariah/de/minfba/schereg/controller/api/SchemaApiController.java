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

import de.unibamberg.minf.dme.model.base.Element;
import de.unibamberg.minf.dme.model.datamodel.DatamodelImpl;
import de.unibamberg.minf.dme.model.datamodel.base.Datamodel;
import de.unibamberg.minf.dme.model.datamodel.base.DatamodelNature;
import de.unibamberg.minf.dme.model.datamodel.natures.XmlDatamodelNature;
import de.unibamberg.minf.dme.model.serialization.DatamodelContainer;
import de.unibamberg.minf.dme.model.tracking.ChangeSet;
import eu.dariah.de.dariahsp.model.web.AuthPojo;
import eu.dariah.de.dariahsp.web.AuthInfoHelper;
import eu.dariah.de.minfba.schereg.model.RightsContainer;
import eu.dariah.de.minfba.schereg.service.interfaces.ElementService;
import eu.dariah.de.minfba.schereg.service.interfaces.SchemaService;

@Controller
@RequestMapping(value="/api/schemas")
public class SchemaApiController extends BaseApiController {
	protected static final Logger logger = LoggerFactory.getLogger(SchemaApiController.class);
	
	@Autowired protected AuthInfoHelper authInfoHelper;
	
	@Autowired private SchemaService schemaService;
	@Autowired protected ElementService elementService;

	
	@RequestMapping(method = RequestMethod.GET, value = "")
	public @ResponseBody List<DatamodelContainer> getSchemas(HttpServletRequest request) {
		AuthPojo auth = authInfoHelper.getAuth(request);
		List<RightsContainer<Datamodel>> schemas = schemaService.findAllByAuth(auth);
		List<DatamodelContainer> result = new ArrayList<DatamodelContainer>();
		ChangeSet ch;
		DatamodelContainer sp;
		if (schemas!=null) {
			for (RightsContainer<Datamodel> s : schemas) {
				if (s.getElement() instanceof XmlDatamodelNature) {
					XmlDatamodelNature xmlS = ((XmlDatamodelNature)s.getElement());
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
				
				sp = new DatamodelContainer();
				sp.setModel((DatamodelImpl)s.getElement());
				
				result.add(sp);
			}
		}
		return result;
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/{entityId}")
	public @ResponseBody DatamodelContainer exportSchema(@PathVariable String entityId, HttpServletRequest request) {
		AuthPojo auth = authInfoHelper.getAuth(request);
		RightsContainer<Datamodel> s = schemaService.findByIdAndAuth(entityId, auth);
		Element r = elementService.findRootBySchemaId(entityId, true);
		
		DatamodelContainer sp = new DatamodelContainer();
		sp.setModel((DatamodelImpl)s.getElement());
		
		ChangeSet ch = schemaService.getLatestChangeSetForEntity(s.getId());
		if (ch!=null) {
			s.getElement().setVersionId(ch.getId());
		}
		
		sp.setRoot(r);
		sp.setGrammars(this.serializeGrammarSources(entityId));
		return sp;
	}
}
