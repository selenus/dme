package de.unibamberg.minf.dme.importer.mapping;

import de.unibamberg.minf.core.util.Stopwatch;
import de.unibamberg.minf.dme.importer.BaseImporter;
import de.unibamberg.minf.dme.model.mapping.base.Mapping;

public abstract class BaseMappingImporter extends BaseImporter implements MappingImporter {
		
	private Mapping mapping;
	private MappingImportListener importListener;
	
	public Mapping getMapping() { return mapping; }
	@Override public void setMapping(Mapping mapping) { this.mapping = mapping; }

	public MappingImportListener getImportListener() { return importListener; }
	@Override public void setImportListener(MappingImportListener importListener) { this.importListener = importListener; }
	
	
	@Override
	public void run() {
		Stopwatch sw = new Stopwatch().start();
		logger.debug(String.format("Started importing mapping %s", this.getMapping().getId()));
		try {
			this.importJson();
			if (this.getImportListener()!=null) {
				logger.info(String.format("Finished importing mapping %s in %sms", this.getMapping().getId(), sw.getElapsedTime()));
				this.getImportListener().registerImportFinished(this.getMapping(), this.auth);
			}
		} catch (Exception e) {
			logger.error("Error while importing JSON Mapping", e);
			if (this.getImportListener()!=null) {
				this.getImportListener().registerImportFailed(this.getMapping());
			}
		}
	}
	
	protected abstract void importJson();
}
