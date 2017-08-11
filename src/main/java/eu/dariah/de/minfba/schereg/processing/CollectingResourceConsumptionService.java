package eu.dariah.de.minfba.schereg.processing;

import java.util.ArrayList;
import java.util.List;

import de.unibamberg.minf.dme.model.datamodel.base.DatamodelNature;
import eu.dariah.de.minfba.processing.consumption.ResourceConsumptionService;
import eu.dariah.de.minfba.processing.model.base.Resource;

public class CollectingResourceConsumptionService implements ResourceConsumptionService {

	private List<Resource> resources;

	public List<Resource> getResources() { return resources; }
	public void setResources(List<Resource> resources) { this.resources = resources; }
	

	@Override
	public void consume(Resource res) {
		if (resources==null) {
			resources = new ArrayList<Resource>();
		}
		resources.add(res);
	}
	
	@Override
	public int commit() {
		return 0;
	}
	@Override
	public void init(String schemaId) {
	}
}