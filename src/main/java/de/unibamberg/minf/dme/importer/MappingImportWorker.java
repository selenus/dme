package de.unibamberg.minf.dme.importer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.unibamberg.minf.dme.exception.MappingImportException;
import de.unibamberg.minf.dme.importer.mapping.MappingImportListener;
import de.unibamberg.minf.dme.importer.mapping.MappingImporter;
import de.unibamberg.minf.dme.model.LogEntry;
import de.unibamberg.minf.dme.model.RightsContainer;
import de.unibamberg.minf.dme.model.LogEntry.LogType;
import de.unibamberg.minf.dme.model.grammar.GrammarContainer;
import de.unibamberg.minf.dme.model.mapping.base.MappedConcept;
import de.unibamberg.minf.dme.model.mapping.base.Mapping;
import de.unibamberg.minf.dme.service.interfaces.MappingService;
import eu.dariah.de.dariahsp.model.web.AuthPojo;

@Component
public class MappingImportWorker extends BaseImportWorker<MappingImporter> implements MappingImportListener {
	
	@Autowired private MappingService mappingService;
	
	@Override protected Class<MappingImporter> getBaseImporterType() { return MappingImporter.class; }
	
	public List<LogEntry> importMapping(String filePath, String entityId, boolean keepImportedIds, AuthPojo auth) throws MappingImportException {
		List<LogEntry> importLog = new ArrayList<LogEntry>();
		if (entityId==null || entityId.trim().isEmpty()) {
			this.logMessage(LogType.ERROR, importLog, GenericImporterMessages.NoEntityId.getMessageCode(), null);
			return importLog;
		}
		if (this.processingEntityIds.contains(entityId)) {
			this.logMessage(LogType.WARNING, importLog, GenericImporterMessages.EntityIdAlreadyInProcess.getMessageCode(), null);
			return importLog;
		}
		if (filePath==null || !(new File(filePath).exists())) {
			this.logMessage(LogType.ERROR, importLog, GenericImporterMessages.FileNotFoundOrNotAccessible.getMessageCode(), new Object[] { filePath });
			return importLog;
		}

		RightsContainer<Mapping> rcM = mappingService.findByIdAndAuth(entityId, auth);
		if (rcM==null) {
			this.logMessage(LogType.ERROR, importLog, GenericImporterMessages.EntityIdNotAuthorized.getMessageCode(), null);
			return importLog;
		}
		
		MappingImporter importer = this.getSupportingImporter(filePath);
		if (importer==null) {
			this.logMessage(LogType.ERROR, importLog, GenericImporterMessages.NoSupportingImporter.getMessageCode(), null);
			return importLog;
		}
		
		importer.setAuth(auth);
		importer.setImportFilePath(filePath);
		importer.setImportListener(this);
		importer.setKeepImportedIds(keepImportedIds);
		importer.setMapping(rcM.getElement());
		
		this.logMessage(LogType.INFO, importLog, GenericImporterMessages.ImportStarted.getMessageCode(), null);
		this.execute(entityId, importer);
		
		return importLog;
	}
	
	
	@Override
	public void registerImportFailed(Mapping mapping) {
		logger.error(GenericImporterMessages.ImportFailed.getMessageCode());
		this.processingEntityIds.remove(mapping.getId());
	} 

	@Override
	public void registerImportFinished(Mapping mapping, List<MappedConcept> importedConcepts, Map<String, String> importedFunctions, Map<String, GrammarContainer> importedGrammars, AuthPojo auth) {
		logger.error(GenericImporterMessages.ImportFinished.getMessageCode());
		this.processingEntityIds.remove(mapping.getId());
	}
}
