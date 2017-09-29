package de.unibamberg.minf.dme.importer.mapping;

import de.unibamberg.minf.dme.importer.Importer;
import de.unibamberg.minf.dme.model.mapping.base.Mapping;

public interface MappingImporter extends Importer {
	public void setMapping(Mapping mapping);
	public void setImportListener(MappingImportListener importListener);
}