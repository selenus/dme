package eu.dariah.de.minfba.schereg.dao.base;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.dariah.de.minfba.core.metamodel.interfaces.Identifiable;


public class BaseDaoImpl<T extends Identifiable> {
	public final Logger logger = LoggerFactory.getLogger(this.getClass());
}
