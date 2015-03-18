package eu.dariah.de.minfba.schereg.dao;

import org.springframework.stereotype.Repository;

import de.dariah.base.dao.base.BaseDaoImpl;
import eu.dariah.de.minfba.core.metamodel.BaseSchema;
import eu.dariah.de.minfba.core.metamodel.interfaces.Schema;

@Repository
public class SchemaDaoImpl extends BaseDaoImpl<Schema> implements SchemaDao {
	public SchemaDaoImpl() {
		super(BaseSchema.class);
	}
}
