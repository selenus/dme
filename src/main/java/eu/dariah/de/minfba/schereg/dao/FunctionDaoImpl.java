package eu.dariah.de.minfba.schereg.dao;

import java.util.List;

import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import eu.dariah.de.minfba.core.metamodel.function.interfaces.TransformationFunction;
import eu.dariah.de.minfba.schereg.dao.base.BaseDaoImpl;
import eu.dariah.de.minfba.schereg.dao.interfaces.FunctionDao;

@Repository
public class FunctionDaoImpl extends BaseDaoImpl<TransformationFunction> implements FunctionDao {
	public FunctionDaoImpl() {
		super(TransformationFunction.class);
	}
	
	@Override
	public List<TransformationFunction> findBySchemaId(String schemaId) {		
		return this.find(Query.query(Criteria.where("schemaId").is(schemaId)));
	}
}
