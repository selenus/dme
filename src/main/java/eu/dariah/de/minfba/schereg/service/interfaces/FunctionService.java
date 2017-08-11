package eu.dariah.de.minfba.schereg.service.interfaces;

import java.util.List;

import de.unibamberg.minf.dme.model.base.Element;
import de.unibamberg.minf.dme.model.base.Function;
import de.unibamberg.minf.dme.model.function.FunctionImpl;
import eu.dariah.de.dariahsp.model.web.AuthPojo;
import eu.dariah.de.minfba.schereg.service.base.BaseService;

public interface FunctionService extends BaseService {
	public Function createAndAppendFunction(String schemaId, String grammarId, String label, AuthPojo auth);
	
	public void deleteFunctionsBySchemaId(String schemaId);

	public Function findById(String functionId);

	public void saveFunction(FunctionImpl function, AuthPojo auth);

	public Function deleteFunctionById(String schemaId, String id, AuthPojo auth);

	public List<Function> findByEntityId(String entityId);
}
