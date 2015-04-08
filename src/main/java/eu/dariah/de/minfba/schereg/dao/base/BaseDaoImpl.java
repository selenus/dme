package eu.dariah.de.minfba.schereg.dao.base;

import java.util.List;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import eu.dariah.de.minfba.core.metamodel.interfaces.Identifiable;


public class BaseDaoImpl<T extends Identifiable> implements BaseDao<T> {
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	protected final Class<T> clazz;
	
	@Autowired private MongoTemplate mongoTemplate;
	
	public MongoTemplate getMongoTemplate() {
		return mongoTemplate;
	}

	public BaseDaoImpl(Class<T> clazz) {
		this.clazz = clazz;
	}

	/*@Override
	public Page<T> findAll(Pageable arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Iterable<T> findAll(Iterable<String> ids) {		
		// TODO Auto-generated method stub
		return null;
	}*/

	@Override
	public List<T> findAll() {
		return mongoTemplate.findAll(clazz, this.getCollectionName());
	}

	@Override
	public List<T> findAll(Sort sort) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public T findById(String id) {
		return mongoTemplate.findById(id, clazz, this.getCollectionName());
	}

	@Override
	public List<T> find(Query q) {
		return mongoTemplate.find(q, this.clazz, this.getCollectionName());
	}
	
	@Override
	public T findOne(Query q) {
		return mongoTemplate.findOne(q, this.clazz, this.getCollectionName());
	}
	
	@Override
	public void findAndModify(Query query, Update update) {
		mongoTemplate.findAndModify(query, update, this.clazz, this.getCollectionName());
	}
	
	@Override
	public void updateMulti(Query query, Update update) {
		mongoTemplate.updateMulti(query, update, this.clazz, this.getCollectionName());
	}
	
	@Override
	public void upsert(Query query, Update update) {
		mongoTemplate.upsert(query, update, this.clazz, this.getCollectionName());
	}
	
	/*@Override
	public long count() {
		// TODO Auto-generated method stub
		return 0;
	}*/

	@Override
	public void delete(String id) {
		mongoTemplate.findAllAndRemove(new Query(Criteria.where("_id").is(id)), clazz, this.getCollectionName());
	}

	@Override
	public void delete(T entity) {
		mongoTemplate.remove(entity);
	}

	@Override
	public void delete(Iterable<? extends T> entities) {
		mongoTemplate.remove(entities);
	}

	/*@Override
	public void deleteAll() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean exists(String arg0) {
		// TODO Auto-generated method stub
		return false;
	}*/

	@Override
	public <S extends T> S save(S entity) {
		mongoTemplate.save(entity, clazz.getSimpleName());
		return entity;
	}

	@Override
	public <S extends T> List<S> save(Iterable<S> entites) {
		// TODO Auto-generated method stub
		return null;
	}

	
	
	@Override
	public String getCollectionName() {
		return clazz.getSimpleName();
	}

	public static boolean isValidObjectId(String id) {
		return (id!=null && ObjectId.isValid(id)); 
	}
}
