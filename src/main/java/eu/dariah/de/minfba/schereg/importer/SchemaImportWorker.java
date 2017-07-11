package eu.dariah.de.minfba.schereg.importer;

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
import org.springframework.stereotype.Component;

import eu.dariah.de.dariahsp.model.web.AuthPojo;
import eu.dariah.de.minfba.core.metamodel.interfaces.Identifiable;
import eu.dariah.de.minfba.core.metamodel.interfaces.Nonterminal;
import eu.dariah.de.minfba.core.metamodel.interfaces.Schema;
import eu.dariah.de.minfba.core.metamodel.interfaces.SchemaNature;
import eu.dariah.de.minfba.core.metamodel.interfaces.Terminal;
import eu.dariah.de.minfba.schereg.exception.SchemaImportException;
import eu.dariah.de.minfba.schereg.serialization.Reference;
import eu.dariah.de.minfba.schereg.service.interfaces.ElementService;
import eu.dariah.de.minfba.schereg.service.interfaces.SchemaService;

@Component
public class SchemaImportWorker implements ApplicationContextAware, SchemaImportListener {
	protected static final Logger logger = LoggerFactory.getLogger(SchemaImportWorker.class);	
	private final ExecutorService executor = Executors.newCachedThreadPool();
	
	@Autowired private SchemaService schemaService;
	@Autowired private ElementService elementService;
	
	private ApplicationContext appContext;
	
	private List<String> processingSchemaIds = new ArrayList<String>();
	
	@Override
	public void setApplicationContext(ApplicationContext appContext) throws BeansException {
		this.appContext = appContext;
	}
	
	public boolean isSupported(String filePath) {
		Map<String, SchemaImporter> importers = appContext.getBeansOfType(SchemaImporter.class);
		for (SchemaImporter importer : importers.values()) {
			importer.setSchemaFilePath(filePath);
			if (importer.getIsSupported()) {
				return true;
			}
		}
		return false;
	}
	
	public List<? extends Identifiable> getPossibleRootElements(String filePath) {
		Map<String, SchemaImporter> importers = appContext.getBeansOfType(SchemaImporter.class);
		for (SchemaImporter importer : importers.values()) {
			importer.setSchemaFilePath(filePath);
			if (importer.getIsSupported()) {
				return importer.getPossibleRootElements();
			}
		}
		return null;
	}
	
	public boolean isBeingProcessed(String schemaId) {
		return schemaId!=null && this.processingSchemaIds.contains(schemaId);
	}
	
	public void importSchema(String filePath, String schemaId, String schemaRoot, AuthPojo auth) throws SchemaImportException {
		if (schemaId==null || schemaId.trim().isEmpty()) {
			logger.error("Schema id must exist (schema must be saved) before import");
			throw new SchemaImportException("Schema id must exist (schema must be saved) before import");
		}
		
		Schema s = schemaService.findSchemaById(schemaId);
		if (!this.processingSchemaIds.contains(schemaId)) {
			this.processingSchemaIds.add(schemaId);
		}
		if (filePath==null || !(new File(filePath).exists())) {
			logger.error("Schema import file not set or accessible [{}]", filePath);
			throw new SchemaImportException("Schema import file not set or accessible [{}]");
		}

		Map<String, SchemaImporter> importers = appContext.getBeansOfType(SchemaImporter.class);
		for (SchemaImporter importer : importers.values()) {
			importer.setSchemaFilePath(filePath);
			if (importer.getIsSupported()) {
				importer.setListener(this);
				importer.setSchema(s);
				importer.setRootElementName(schemaRoot); 
				importer.setAuth(auth);
				
				this.executor.execute(importer);
				return;
			}
		}
		
		throw new SchemaImportException("Failed to import schema due to no matching importer being available");
	}
	
	@Override
	public synchronized void registerImportFinished(Schema importedSchema, Nonterminal root, List<Nonterminal> additionalRootElements, AuthPojo auth) {
		if (root!=null) {
			elementService.clearElementTree(importedSchema.getId(), auth);
		}
		
		
		List<Reference> rootNonterminals = new ArrayList<Reference>();
		Reference rootNonterminal = elementService.saveElementHierarchy(root, auth);
		rootNonterminal.setRoot(true);
		
		rootNonterminals.add(rootNonterminal);
		/*if (additionalRootElements!=null && additionalRootElements.size()>0) {
			for (Nonterminal addRoot : additionalRootElements) {
				rootNonterminals.add(elementService.saveElementHierarchy(addRoot, auth));
			}
		}*/
		
		Schema s = schemaService.findSchemaById(importedSchema.getEntityId());
		for (SchemaNature n : importedSchema.getNatures()) {
			s.addOrReplaceSchemaNature(n);
		}
		schemaService.saveSchema(s, rootNonterminals, auth);
		
		if (this.processingSchemaIds.contains(importedSchema.getId())) {
			this.processingSchemaIds.remove(importedSchema.getId());
		}
	}

	@Override 
	public synchronized void registerImportFailed(Schema schema) { 
		if (this.processingSchemaIds.contains(schema.getId())) {
			this.processingSchemaIds.remove(schema.getId());
		}
		logger.warn("Schema import failed");
	}
}
