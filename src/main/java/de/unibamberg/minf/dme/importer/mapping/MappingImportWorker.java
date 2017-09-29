package de.unibamberg.minf.dme.importer.mapping;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import de.unibamberg.minf.dme.exception.MappingImportException;
import de.unibamberg.minf.dme.exception.SchemaImportException;
import de.unibamberg.minf.dme.importer.MappingImporter;
import de.unibamberg.minf.dme.importer.SchemaImporter;
import de.unibamberg.minf.dme.model.LogEntry;
import de.unibamberg.minf.dme.model.RightsContainer;
import de.unibamberg.minf.dme.model.datamodel.base.Datamodel;
import de.unibamberg.minf.dme.model.mapping.base.Mapping;
import de.unibamberg.minf.dme.service.interfaces.MappingService;
import eu.dariah.de.dariahsp.model.web.AuthPojo;

public class MappingImportWorker implements ApplicationContextAware, MappingImportListener {
	private static final String LogMessageNoEntityId = "Mapping id must exist (mapping must be saved) before import";
	
	protected static final Logger logger = LoggerFactory.getLogger(MappingImportWorker.class);
	
	private final ExecutorService executor = Executors.newCachedThreadPool();

	private ApplicationContext appContext;
	
	private List<String> processingMappingIds = new ArrayList<String>();
	
	
	@Autowired private MappingService mappingService;
	
	
	@Override
	public void setApplicationContext(ApplicationContext appContext) throws BeansException {
		this.appContext = appContext;
	}
	
	public MappingImporter getSupportingImporter(String filePath) {
		Map<String, MappingImporter> importers = appContext.getBeansOfType(MappingImporter.class);
		for (MappingImporter importer : importers.values()) {
			importer.setMappingFilePath(filePath);
			if (importer.getIsSupported()) {
				return importer;
			}
		}
		return null;
	}
	
	public boolean isBeingProcessed(String mappingId) {
		return mappingId!=null && this.processingMappingIds.contains(mappingId);
	}
	
	public List<LogEntry> importMapping(String filePath, String entityId, boolean keepImportedIds, AuthPojo auth) throws MappingImportException {
		List<LogEntry> result = new ArrayList<LogEntry>();
		
		if (entityId==null || entityId.trim().isEmpty()) {
			logger.error(LogMessageNoEntityId);
			result.add(new LogEntry());
		}
		
		RightsContainer<Mapping> rcM = mappingService.findByIdAndAuth(entityId, auth);
		if (rcM==null) {
			return result;
		}
		if (this.processingMappingIds.contains(entityId)) {
			
		}
		
		Mapping m = rcM.getElement();
		
		return result;
		
		/*Datamodel s = schemaService.findSchemaById(entityId);
		if (!this.processingSchemaIds.contains(entityId)) {
			this.processingSchemaIds.add(entityId);
		}
		if (filePath==null || !(new File(filePath).exists())) {
			logger.error("Schema import file not set or accessible [{}]", filePath);
			throw new SchemaImportException("Schema import file not set or accessible [{}]");
		}

		Map<String, SchemaImporter> importers = appContext.getBeansOfType(SchemaImporter.class);
		for (SchemaImporter importer : importers.values()) {
			importer.setSchemaFilePath(filePath);
			if (importer.getIsSupported()) {
				importer.setKeepImportedIds(keepImportedIds);
				importer.setListener(this);
				importer.setSchema(s);
				importer.setRootElementName(schemaRoot); 
				importer.setAuth(auth);
				importer.setRootElementType(schemaRootType);
				importer.setElementId(elementId);
				
				this.executor.execute(importer);
				return;
			}
		}*/
		
		//throw new SchemaImportException("Failed to import schema due to no matching importer being available");
		
	}
	
	
	@Override
	public void registerImportFailed(Mapping mapping) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void registerImportFinished(Mapping importedMapping, AuthPojo auth) {
		// TODO Auto-generated method stub
		
	}

	
	
}
