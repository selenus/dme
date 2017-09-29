package de.unibamberg.minf.dme.importer;

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

import de.unibamberg.minf.dme.exception.SchemaImportException;
import de.unibamberg.minf.dme.importer.datamodel.DatamodelImportListener;
import de.unibamberg.minf.dme.importer.datamodel.DatamodelImporter;
import de.unibamberg.minf.dme.model.base.ModelElement;
import de.unibamberg.minf.dme.model.base.Nonterminal;
import de.unibamberg.minf.dme.model.datamodel.base.Datamodel;
import de.unibamberg.minf.dme.model.datamodel.base.DatamodelNature;
import de.unibamberg.minf.dme.model.exception.MetamodelConsistencyException;
import de.unibamberg.minf.dme.serialization.Reference;
import de.unibamberg.minf.dme.service.interfaces.ElementService;
import de.unibamberg.minf.dme.service.interfaces.IdentifiableService;
import de.unibamberg.minf.dme.service.interfaces.ReferenceService;
import de.unibamberg.minf.dme.service.interfaces.SchemaService;
import eu.dariah.de.dariahsp.model.web.AuthPojo;

@Component
public class DatamodelImportWorker implements ApplicationContextAware, DatamodelImportListener {
	protected static final Logger logger = LoggerFactory.getLogger(DatamodelImportWorker.class);	
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
	
	public DatamodelImporter getSupportingImporter(String filePath) {
		Map<String, DatamodelImporter> importers = appContext.getBeansOfType(DatamodelImporter.class);
		for (DatamodelImporter importer : importers.values()) {
			importer.setImportFilePath(filePath);
			if (importer.getIsSupported()) {
				return importer;
			}
		}
		return null;
	}
	
	public boolean isBeingProcessed(String schemaId) {
		return schemaId!=null && this.processingSchemaIds.contains(schemaId);
	}
	
	public void importSchema(String filePath, String schemaId, String schemaRoot, boolean keepImportedIds, AuthPojo auth) throws SchemaImportException {
		this.importSubtree(filePath, schemaId, null, schemaRoot, null, keepImportedIds, auth);
	}

	public void importSubtree(String filePath, String entityId, String elementId, String schemaRoot, String schemaRootType, boolean keepImportedIds, AuthPojo auth) throws SchemaImportException {
		if (entityId==null || entityId.trim().isEmpty()) {
			logger.error("Schema id must exist (schema must be saved) before import");
			throw new SchemaImportException("Schema id must exist (schema must be saved) before import");
		}
		
		Datamodel s = schemaService.findSchemaById(entityId);
		if (!this.processingSchemaIds.contains(entityId)) {
			this.processingSchemaIds.add(entityId);
		}
		if (filePath==null || !(new File(filePath).exists())) {
			logger.error("Schema import file not set or accessible [{}]", filePath);
			throw new SchemaImportException("Schema import file not set or accessible [{}]");
		}

		Map<String, DatamodelImporter> importers = appContext.getBeansOfType(DatamodelImporter.class);
		for (DatamodelImporter importer : importers.values()) {
			importer.setImportFilePath(filePath);
			if (importer.getIsSupported()) {
				importer.setKeepImportedIds(keepImportedIds);
				importer.setListener(this);
				importer.setDatamodel(s);
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
	public void registerImportFinished(Datamodel importedSchema, String parentElementId, List<ModelElement> rootElements, List<ModelElement> additionalRootElements, AuthPojo auth) {
		if (parentElementId==null) {
			this.importSchema(importedSchema, (Nonterminal)rootElements.get(0), additionalRootElements, auth);
		} else {
			this.importSubtree(importedSchema, parentElementId, rootElements, additionalRootElements, auth);
		}
	}
	
	private synchronized void importSubtree(Datamodel importedSchema, String parentElementId, List<ModelElement> rootElements, List<ModelElement> additionalRootElements, AuthPojo auth) {
		Datamodel s = schemaService.findSchemaById(importedSchema.getId());
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
		
		if (importedSchema.getNatures()!=null) {
			for (DatamodelNature n : importedSchema.getNatures()) {
				DatamodelNature existN = s.getNature(n.getClass());
				if (existN!=null) {			
					try {
						existN.merge(n);
					} catch (MetamodelConsistencyException e) {
						logger.error("Failed to merge schema natures");
					}
				}
			}
		}
		
		schemaService.saveSchema(s, auth);
		
		if (this.processingSchemaIds.contains(importedSchema.getId())) {
			this.processingSchemaIds.remove(importedSchema.getId());
		}
	}
	
	private synchronized void importSchema(Datamodel importedSchema, Nonterminal root, List<ModelElement> additionalRootElements, AuthPojo auth) {
		if (root!=null) {
			elementService.clearElementTree(importedSchema.getId(), auth);
		}
		
		List<ModelElement> rootElements = new ArrayList<ModelElement>();
		rootElements.add(root);

		if (additionalRootElements!=null && additionalRootElements.size()>0) {
			rootElements.addAll(additionalRootElements);
		}
		
		List<Reference> rootRefs = identifiableService.saveHierarchies(rootElements, auth);
		
		//Reference rootNonterminal = elementService.saveElementHierarchy(root, auth);
		for (Reference rootRef : rootRefs) {
			rootRef.setRoot(false);
			if (rootRef.getId().equals(root.getId())) {
				rootRef.setRoot(true);
			}
		}
		
		Datamodel s = schemaService.findSchemaById(importedSchema.getId());
		for (DatamodelNature n : importedSchema.getNatures()) {
			s.addOrReplaceNature(n);
		}
		schemaService.saveSchema(s, rootRefs, auth);
		
		
		
		if (this.processingSchemaIds.contains(importedSchema.getId())) {
			this.processingSchemaIds.remove(importedSchema.getId());
		}
	}
	

	@Override 
	public synchronized void registerImportFailed(Datamodel schema) { 
		if (this.processingSchemaIds.contains(schema.getId())) {
			this.processingSchemaIds.remove(schema.getId());
		}
		logger.warn("Schema import failed");
	}	
}
