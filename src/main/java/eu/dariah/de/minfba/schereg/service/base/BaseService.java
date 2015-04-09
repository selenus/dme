package eu.dariah.de.minfba.schereg.service.base;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseService {
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	protected static String getNormalizedName(String label) {
		if (label==null || label.trim().isEmpty()) {
			label = "g" + new ObjectId().toString().toUpperCase();
		}
		//label = label.replaceAll("\\W", "");
		label = label.substring(0,1).toUpperCase() + label.substring(1);
		return label;
	}
}