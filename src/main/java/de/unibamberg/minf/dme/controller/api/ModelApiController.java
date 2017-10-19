package de.unibamberg.minf.dme.controller.api;

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

import de.unibamberg.minf.dme.exporter.DatamodelExporter;
import de.unibamberg.minf.dme.model.serialization.DatamodelReferenceContainer;
import eu.dariah.de.dariahsp.model.web.AuthPojo;
import eu.dariah.de.dariahsp.web.AuthInfoHelper;

@Controller
@RequestMapping(value="/api/models")
public class ModelApiController extends BaseApiController {
	protected static final Logger logger = LoggerFactory.getLogger(ModelApiController.class);
	
	@Autowired protected AuthInfoHelper authInfoHelper;
	
	@Autowired private DatamodelExporter exporter;
		
	@RequestMapping(method = RequestMethod.GET, value = "")
	public @ResponseBody List<DatamodelReferenceContainer> getDatamodels(HttpServletRequest request) {
		try {
			return exporter.exportDatamodels(authInfoHelper.getAuth(request), true);
		} catch (Exception e) {
			logger.error("Failed to export datamodels", e);
			return null;
		}
	}
	
	@RequestMapping(method = RequestMethod.GET, value = "/{entityId}")
	public @ResponseBody DatamodelReferenceContainer exportDatamodel(@PathVariable String entityId, HttpServletRequest request) {
		return exporter.exportDatamodel(entityId, authInfoHelper.getAuth(request));
	}
}
