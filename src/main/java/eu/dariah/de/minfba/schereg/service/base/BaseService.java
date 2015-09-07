package eu.dariah.de.minfba.schereg.service.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseService {
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	protected static String getNormalizedName(String label) {
		return label.substring(0,1).toUpperCase() + label.substring(1);
	}
}