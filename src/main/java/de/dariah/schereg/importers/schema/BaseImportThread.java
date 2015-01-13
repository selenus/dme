package de.dariah.schereg.importers.schema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.xml.sax.InputSource;

import de.dariah.base.model.base.SchemaElement;
import de.dariah.schereg.base.model.Domain;
import de.dariah.schereg.base.model.Namespace;
import de.dariah.schereg.base.model.Schema;
import de.dariah.schereg.base.service.SchemaService;
import de.dariah.schereg.util.ContextService;
import de.dariah.schereg.util.ScheRegConstants;
import de.dariah.schereg.util.StopWatch;

public abstract class BaseImportThread extends Thread {

	protected Logger logger = LoggerFactory.getLogger(this.getClass());
	private SchemaService schemaService;
	
	private Schema schema;
	private int schemaId;
	private InputSource importSource;
	private List<Domain> globalDomains;
	
	protected SecurityContext securityContext;
	
	private boolean isSetUp = false;
	
	public List<Domain> getGlobalDomains() {return globalDomains; }
	public void setGlobalDomains(List<Domain> globalDomains) { this.globalDomains = globalDomains; }
	
	protected HashMap<String, String> nsMap; 
	
	
	public BaseImportThread(int schemaId, InputSource importSource, SecurityContext securityContext) {
		this.schemaId = schemaId;
		this.importSource = importSource;
		this.schemaService = (SchemaService)ContextService.getInstance().getBean(SchemaService.class);
		this.securityContext = securityContext;
	}
	
	protected abstract Collection<SchemaElement> parseExternalSchema(InputSource importSource) throws Exception;
	
	public void setUp() {
		
		schema = schemaService.getSchema(schemaId);
		schema.setState(ScheRegConstants.STATE_IN_PROGRESS);
		schemaService.saveSchema(schema);
		
		isSetUp = true;
	}
	
	@Override
	public void run() { 
		if (!isSetUp) {
			logger.error("Call the setup method of the importer before starting the import thread");
			return;
		}
		
		try {
			StopWatch sw = new StopWatch();
			
			SecurityContextHolder.setContext(this.securityContext);

			logger.info(String.format("Import thread started on schema [%s]", schemaId));
			setGlobalDomains(schemaService.getGlobalDomains());
						
			Collection<SchemaElement> schemaElements;
		
			synchronized(importSource) {
				logger.info("Parsing external schema...");
				sw.start();
				schemaElements = parseExternalSchema(importSource);
				logger.info(String.format("Parsing external schema...done, [%d] schema elements identified in %dms", schemaElements.size(), sw.getElapsedTime()));
			}
			
			synchronized(schema) {
				DateTime currentDate = DateTime.now();
				logger.info("Assigning schema elements to schema...");
				if (schemaElements != null && schemaElements.size() > 0) {
					for (SchemaElement element : schemaElements) {
						element.setSchema(schema);
						element.setModified(currentDate);
						if (element.getCreated() == null) {
							element.setCreated(currentDate);
						}				
					}
				}
				
				if (nsMap != null) {
					Collection<Namespace> nsCollection = new ArrayList<Namespace>();
					Namespace ns;
					for (String key : nsMap.keySet()) {
						ns = new Namespace();
						ns.setPrefix(nsMap.get(key));
						ns.setUri(key);
						ns.setSchema(schema);
						
						nsCollection.add(ns);
					}
					schema.setNamespaces(nsCollection);
				}
				
				logger.info(String.format("Saving schema along with %d schema elements...", schemaElements.size()));
				sw.reset();
				
				schemaService.saveImportedSchema(schema, schemaElements);
				
				logger.info(String.format("Saving schema along with schema elements...done in %dms", sw.getElapsedTime()));
				sw.stop();
			}
			
		} catch (Exception ex) {
			String errorMessage = "Failed to save Schema.";
			logger.error(errorMessage, ex);
			try {
				schema.setState(ScheRegConstants.STATE_ERROR);
				schema.setMessage(ex.toString());
				schemaService.saveSchema(schema);
				// Try to save the schema with error, on another exception -> rollback
			} catch (Exception ex2) {
				logger.error("Failed to set schema on state: error", ex2);
				throw new RuntimeException("Exception while saving Schema (with Elements)", ex2);
			}
		}
	}
}
