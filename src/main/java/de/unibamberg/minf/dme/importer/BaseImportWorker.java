package de.unibamberg.minf.dme.importer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.MessageSource;

import de.unibamberg.minf.dme.model.LogEntry;
import de.unibamberg.minf.dme.model.LogEntry.LogType;

public abstract class BaseImportWorker<T extends Importer> implements ApplicationContextAware {
	public enum GenericImporterMessages  {
		NoEntityId("Mapping id must exist (mapping must be saved) before import"), 
		EntityIdNotAuthorized("First Name"),
		EntityIdAlreadyInProcess(""),
		FileNotFoundOrNotAccessible("Schema import file not set or accessible [{}]"), 
		NoSupportingImporter("..."),
		ImportStarted("..."), 
		ImportFailed("..."),
		ImportFinished("...");
		
	    private final String messageCode;
	    private GenericImporterMessages(String messageCode) { this.messageCode = messageCode; }
	    public String getMessageCode() { return messageCode; }
	} 
	
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private final ExecutorService executor = Executors.newCachedThreadPool();
	private ApplicationContext appContext;
	protected List<String> processingEntityIds = new ArrayList<String>();
	
	@Autowired private MessageSource messageSource;
	
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
	
	protected void execute(String entityId, Importer importer) {
		if (!this.processingEntityIds.contains(entityId)) {
			this.processingEntityIds.add(entityId);
			this.executor.execute(importer);
		}
	}
	
	protected void logMessage(LogType type, List<LogEntry> log, String messageCode, Object[] args) {
		logger.debug("{}: {}", type.toString(), messageSource.getMessage(messageCode, args, Locale.getDefault()));
		log.add(LogEntry.createEntry(type, messageCode, args, messageSource.getMessage(messageCode, args, Locale.getDefault())));
	}
	
	protected abstract Class<T> getBaseImporterType();
}
