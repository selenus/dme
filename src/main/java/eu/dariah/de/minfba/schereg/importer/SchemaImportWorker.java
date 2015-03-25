package eu.dariah.de.minfba.schereg.importer;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import eu.dariah.de.minfba.core.metamodel.Nonterminal;
import eu.dariah.de.minfba.core.metamodel.interfaces.Schema;
import eu.dariah.de.minfba.core.metamodel.interfaces.Terminal;
import eu.dariah.de.minfba.core.metamodel.xml.XmlSchema;
import eu.dariah.de.minfba.schereg.exception.SchemaImportException;

@Component
public class SchemaImportWorker implements ApplicationContextAware, SchemaImportListener {
	protected static final Logger logger = LoggerFactory.getLogger(SchemaImportWorker.class);	
	private final ExecutorService executor = Executors.newCachedThreadPool();
	
	private ApplicationContext appContext;
	
	
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
	
	public void importSchema(String filePath, Schema schema) throws SchemaImportException {
		/*
		 * Currently only XML Schemata are supported for import;
		 * 	TODO: Extend for (configurable) support of CSV, JSON etc. schemata
		 */
		if (filePath==null || !(new File(filePath).exists())) {
			logger.error("Schema import file not set or accessible [{}]", filePath);
			throw new SchemaImportException("Schema import file not set or accessible [{}]");
		}
		if (schema==null || schema.getId()==null || schema.getId().trim().isEmpty()) {
			logger.error("Schema id must exist (schema must be saved) before import");
			throw new SchemaImportException("Schema id must exist (schema must be saved) before import");
		}
		
		SchemaImporter importer = appContext.getBean(XmlSchemaImporter.class);
		XmlSchema s = (XmlSchema)schema;
		importer.setListener(this);
		importer.setSchemaId(s.getId());
		importer.setSchemaFilePath(filePath);
		importer.setRootElementNs(s.getRootElementNamespace());
		importer.setRootElementName(s.getRootElementName());
		
		this.executor.execute(importer);
	}
	
	@Override
	public synchronized void registerImportFinished(String schemaId, Nonterminal root) {
		logger.info("Schema successfully imported");
	}

	@Override 
	public synchronized void registerImportFailed(String schemaId) { 
		logger.warn("Schema import failed");
	}
}
