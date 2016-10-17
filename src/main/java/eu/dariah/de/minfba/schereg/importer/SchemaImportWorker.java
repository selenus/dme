package eu.dariah.de.minfba.schereg.importer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import com.mongodb.DBObject;

import de.dariah.samlsp.model.pojo.AuthPojo;
import eu.dariah.de.minfba.core.metamodel.Nonterminal;
import eu.dariah.de.minfba.core.metamodel.interfaces.Element;
import eu.dariah.de.minfba.core.metamodel.interfaces.Schema;
import eu.dariah.de.minfba.core.metamodel.interfaces.Terminal;
import eu.dariah.de.minfba.core.metamodel.xml.XmlSchema;
import eu.dariah.de.minfba.core.metamodel.xml.XmlTerminal;
import eu.dariah.de.minfba.schereg.exception.SchemaImportException;
import eu.dariah.de.minfba.schereg.serialization.Reference;
import eu.dariah.de.minfba.schereg.service.SchemaServiceImpl;
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
		SchemaImporter importer = appContext.getBean(XmlSchemaImporter.class);
		importer.setSchemaFilePath(filePath);
		return importer.getIsSupported();
	}
	
	public List<? extends Terminal> getPossibleRootTerminals(String filePath) {
		SchemaImporter importer = appContext.getBean(XmlSchemaImporter.class);
		importer.setSchemaFilePath(filePath);
		return importer.getPossibleRootTerminals();
	}
	
	public boolean isBeingProcessed(String schemaId) {
		return schemaId!=null && this.processingSchemaIds.contains(schemaId);
	}
	
	public void importSchema(String filePath, String schemaId, Integer rootTerminalIndex, AuthPojo auth) throws SchemaImportException {
		/*
		 * Currently only XML Schemata are supported for import;
		 * 	TODO: Extend for (configurable) support of CSV, JSON etc. schemata
		 */
		if (schemaId==null || schemaId.trim().isEmpty()) {
			logger.error("Schema id must exist (schema must be saved) before import");
			throw new SchemaImportException("Schema id must exist (schema must be saved) before import");
		}
		Schema tmpS = schemaService.findSchemaById(schemaId);
		
		XmlSchema s;
		if (tmpS instanceof XmlSchema) {
			s = (XmlSchema)tmpS;
		} else {
			s = new XmlSchema();
			s.setId(tmpS.getId());
			s.setLabel(tmpS.getLabel());
			s.setDescription(tmpS.getDescription());
		}
		
		if (!this.processingSchemaIds.contains(schemaId)) {
			this.processingSchemaIds.add(schemaId);
		}
		
		if (filePath==null || !(new File(filePath).exists())) {
			logger.error("Schema import file not set or accessible [{}]", filePath);
			throw new SchemaImportException("Schema import file not set or accessible [{}]");
		}
		
		
		XmlTerminal rootTerminal = (XmlTerminal)getPossibleRootTerminals(filePath).get(rootTerminalIndex);
		s.setRootElementNamespace(rootTerminal.getNamespace());
		s.setRootElementName(rootTerminal.getName());
		
		SchemaImporter importer = appContext.getBean(XmlSchemaImporter.class);
		importer.setListener(this);
		importer.setSchema(s);
		importer.setSchemaFilePath(filePath);
		importer.setRootElementNs(s.getRootElementNamespace());
		importer.setRootElementName(s.getRootElementName()); 
		importer.setAuth(auth);
		
		this.executor.execute(importer);
	}
	
	@Override
	public synchronized void registerImportFinished(Schema schema, Nonterminal root, AuthPojo auth) {
		if (root!=null) {
			elementService.clearElementTree(schema.getId(), auth);
		}
		
		Reference rootNonterminal = elementService.saveElementHierarchy(root, auth);
		
		schemaService.saveSchema(schema, rootNonterminal, auth);
		
		if (this.processingSchemaIds.contains(schema.getId())) {
			this.processingSchemaIds.remove(schema.getId());
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
