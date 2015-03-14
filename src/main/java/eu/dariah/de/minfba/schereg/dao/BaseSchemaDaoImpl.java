package eu.dariah.de.minfba.schereg.dao;

import org.springframework.stereotype.Repository;

import de.dariah.base.dao.base.BaseDaoImpl;
import eu.dariah.de.minfba.core.metamodel.interfaces.Schema;

@Repository
public class BaseSchemaDaoImpl extends BaseDaoImpl<Schema> implements BaseSchemaDao {
	public BaseSchemaDaoImpl() {
		super(Schema.class);
	}
}
