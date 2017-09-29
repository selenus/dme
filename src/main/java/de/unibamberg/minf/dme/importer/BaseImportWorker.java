package de.unibamberg.minf.dme.importer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;


public abstract class BaseImportWorker<T extends Importer> implements ApplicationContextAware {
	
	public static final String LogMessageNoEntityId = "Mapping id must exist (mapping must be saved) before import";
	
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private final ExecutorService executor = Executors.newCachedThreadPool();
	private ApplicationContext appContext;
	protected List<String> processingEntityIds = new ArrayList<String>();
	

	

	@Override
	public void setApplicationContext(ApplicationContext appContext) throws BeansException {
		this.appContext = appContext;
	}
	
	public boolean isBeingProcessed(String entityId) {
		return entityId!=null && this.processingEntityIds.contains(entityId);
	}
	
	public T getSupportingImporter(String filePath) {
		Map<String, T> importers = appContext.getBeansOfType(this.getBaseImporterType());
		for (T importer : importers.values()) {
			importer.setImportFilePath(filePath);
			if (importer.getIsSupported()) {
				return importer;
			}
		}
		return null;
	}
	
	protected abstract Class<T> getBaseImporterType();
}
