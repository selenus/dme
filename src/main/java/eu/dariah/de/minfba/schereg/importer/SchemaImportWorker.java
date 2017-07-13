package eu.dariah.de.minfba.schereg.importer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
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
import eu.dariah.de.minfba.core.metamodel.ModelElement;
import eu.dariah.de.minfba.core.metamodel.exception.MetamodelConsistencyException;
import eu.dariah.de.minfba.core.metamodel.interfaces.Identifiable;
import eu.dariah.de.minfba.core.metamodel.interfaces.Nonterminal;
import eu.dariah.de.minfba.core.metamodel.interfaces.Schema;
import eu.dariah.de.minfba.core.metamodel.interfaces.SchemaNature;
import eu.dariah.de.minfba.schereg.exception.SchemaImportException;
import eu.dariah.de.minfba.schereg.serialization.Reference;
import eu.dariah.de.minfba.schereg.service.interfaces.ElementService;
import eu.dariah.de.minfba.schereg.service.interfaces.IdentifiableService;
import eu.dariah.de.minfba.schereg.service.interfaces.ReferenceService;
import eu.dariah.de.minfba.schereg.service.interfaces.SchemaService;

@Component
public class SchemaImportWorker implements ApplicationContextAware, SchemaImportListener {
	protected static final Logger logger = LoggerFactory.getLogger(SchemaImportWorker.class);	
	private final ExecutorService executor = Executors.newCachedThreadPool();
	
	@Autowired private SchemaService schemaService;
	@Autowired private ElementService elementService;
	@Autowired private IdentifiableService identifiableService;
	
	@Autowired private ReferenceService referenceService;
	
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
	
	public List<? extends ModelElement> getElementsByTypes(String filePath, List<Class<? extends ModelElement>> allowedSubtreeRoots) {
		Map<String, SchemaImporter> importers = appContext.getBeansOfType(SchemaImporter.class);
		for (SchemaImporter importer : importers.values()) {
			importer.setSchemaFilePath(filePath);
			if (importer.getIsSupported()) {
				return importer.getElementsByTypes(allowedSubtreeRoots);
			}
		}
		return null;
	}
	
	public boolean isBeingProcessed(String schemaId) {
		return schemaId!=null && this.processingSchemaIds.contains(schemaId);
	}
	
	public void importSchema(String filePath, String schemaId, String schemaRoot, AuthPojo auth) throws SchemaImportException {
		this.importSubtree(filePath, schemaId, null, schemaRoot, null, auth);
	}

	public void importSubtree(String filePath, String entityId, String elementId, String schemaRoot, String schemaRootType, AuthPojo auth) throws SchemaImportException {
		if (entityId==null || entityId.trim().isEmpty()) {
			logger.error("Schema id must exist (schema must be saved) before import");
			throw new SchemaImportException("Schema id must exist (schema must be saved) before import");
		}
		
		Schema s = schemaService.findSchemaById(entityId);
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
				importer.setListener(this);
				importer.setSchema(s);
				importer.setRootElementName(schemaRoot); 
				importer.setAuth(auth);
				importer.setRootElementType(schemaRootType);
				importer.setElementId(elementId);
				
				this.executor.execute(importer);
				return;
			}
		}
		
		throw new SchemaImportException("Failed to import schema due to no matching importer being available");
	}
	
	@Override
	public void registerImportFinished(Schema importedSchema, String parentElementId, List<ModelElement> rootElements, List<ModelElement> additionalRootElements, AuthPojo auth) {
		if (parentElementId==null) {
			this.importSchema(importedSchema, (Nonterminal)rootElements.get(0), additionalRootElements, auth);
		} else {
			this.importSubtree(importedSchema, parentElementId, rootElements, additionalRootElements, auth);
		}
	}
	
	private synchronized void importSubtree(Schema importedSchema, String parentElementId, List<ModelElement> rootElements, List<ModelElement> additionalRootElements, AuthPojo auth) {
		Schema s = schemaService.findSchemaById(importedSchema.getId());
		Reference root = referenceService.findReferenceBySchemaId(s.getId());
		
		Reference parent = referenceService.findReferenceById(root, parentElementId);
		List<Reference> subrefs;
		for (ModelElement me : rootElements) {
			subrefs = new ArrayList<Reference>();
			if (parent.getChildReferences()==null) {
				parent.setChildReferences(new HashMap<String, Reference[]>());
			}
			if (parent.getChildReferences().containsKey(me.getClass().getName())) {
				for (Reference r : parent.getChildReferences().get(me.getClass().getName())) {
					subrefs.add(r);
				}
			}
			subrefs.add(identifiableService.saveHierarchy(me, auth));			
			parent.getChildReferences().put(me.getClass().getName(), subrefs.toArray(new Reference[0]));
		}
		referenceService.saveRoot(root);
		
		for (SchemaNature n : importedSchema.getNatures()) {
			SchemaNature existN = s.getNature(n.getClass());
			if (existN!=null) {			
				try {
					existN.merge(n);
				} catch (MetamodelConsistencyException e) {
					logger.error("Failed to merge schema natures");
				}
			}
		}
		
		schemaService.saveSchema(s, auth);
		
		if (this.processingSchemaIds.contains(importedSchema.getId())) {
			this.processingSchemaIds.remove(importedSchema.getId());
		}
	}
	
	private synchronized void importSchema(Schema importedSchema, Nonterminal root, List<ModelElement> additionalRootElements, AuthPojo auth) {
		if (root!=null) {
			elementService.clearElementTree(importedSchema.getId(), auth);
		}
		List<Reference> rootNonterminals = new ArrayList<Reference>();
		
		Reference rootNonterminal = identifiableService.saveHierarchy(root, auth);
		
		//Reference rootNonterminal = elementService.saveElementHierarchy(root, auth);
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
