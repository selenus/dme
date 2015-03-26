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
import org.springframework.stereotype.Component;

import eu.dariah.de.minfba.core.metamodel.Nonterminal;
import eu.dariah.de.minfba.core.metamodel.interfaces.Element;
import eu.dariah.de.minfba.core.metamodel.interfaces.Schema;
import eu.dariah.de.minfba.core.metamodel.interfaces.Terminal;
import eu.dariah.de.minfba.core.metamodel.xml.XmlSchema;
import eu.dariah.de.minfba.core.metamodel.xml.XmlTerminal;
import eu.dariah.de.minfba.schereg.exception.SchemaImportException;
import eu.dariah.de.minfba.schereg.service.ElementService;
import eu.dariah.de.minfba.schereg.service.SchemaService;

@Component
public class SchemaImportWorker implements ApplicationContextAware, SchemaImportListener {
	protected static final Logger logger = LoggerFactory.getLogger(SchemaImportWorker.class);	
	private final ExecutorService executor = Executors.newCachedThreadPool();
	
	@Autowired private SchemaService schemaService;
	@Autowired private ElementService elementService;
	
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
	
	public void importSchema(String filePath, String schemaId, Integer rootTerminalIndex) throws SchemaImportException {
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
		
		
		if (filePath==null || !(new File(filePath).exists())) {
			logger.error("Schema import file not set or accessible [{}]", filePath);
			throw new SchemaImportException("Schema import file not set or accessible [{}]");
		}
		
		
		XmlTerminal rootTerminal = (XmlTerminal)getPossibleRootTerminals(filePath).get(rootTerminalIndex);
		s.setRootElementNamespace(rootTerminal.getNamespace());
		s.setRootElementName(rootTerminal.getName());
		
		SchemaImporter importer = appContext.getBean(XmlSchemaImporter.class);
		importer.setListener(this);
		importer.setSchemaId(s.getId());
		importer.setSchemaFilePath(filePath);
		importer.setRootElementNs(s.getRootElementNamespace());
		importer.setRootElementName(s.getRootElementName());                                                                                                                                                                                                                                                                                                                                                                                                                                                  
		
		this.executor.execute(importer);
	}
	
	@Override
	public synchronized void registerImportFinished(String schemaId, Nonterminal root, List<? extends Terminal> terminals) {
		Schema s = schemaService.findSchemaById(schemaId);
		if (s != null && root!=null) {
			elementService.deleteBySchemaId(schemaId);
		}
		elementService.saveElement(root);
		s.setRootNonterminalId(root.getId());
		
		if (terminals.size()>0 && terminals.get(0) instanceof XmlTerminal) {
			XmlSchema sXml;
			if (s instanceof XmlSchema) {
				sXml = (XmlSchema)s;
			} else {
				sXml = schemaService.convertSchema(new XmlSchema(), s);
			}
			sXml.setTerminals(new ArrayList<XmlTerminal>(terminals.size()));
			for (Terminal t : terminals) {
				sXml.getTerminals().add((XmlTerminal)t);
			}
			schemaService.saveSchema(sXml);
		} else {
			schemaService.saveSchema(s);
		}
		
		
		Element e = elementService.findRootByElementId(root.getId());

		logger.info("Schema successfully imported");
	}

	@Override 
	public synchronized void registerImportFailed(String schemaId) { 
		logger.warn("Schema import failed");
	}
}
