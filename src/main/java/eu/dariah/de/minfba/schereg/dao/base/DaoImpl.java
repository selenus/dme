package eu.dariah.de.minfba.schereg.dao.base;

import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

import eu.dariah.de.minfba.core.metamodel.interfaces.Identifiable;

public abstract class DaoImpl<T extends Identifiable> implements Dao {
	protected static final String ID_FIELD = "_id";
	protected static final String ENTITY_ID_FIELD = "entityId";
	
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	protected final Class<T> clazz;
	protected final String collectionName;
	
	@Autowired protected MongoTemplate mongoTemplate;
	
	@Override public Class<?> getClazz() { return clazz; }
	@Override public String getCollectionName() { return collectionName; }
	
	public static boolean isNewId(String id) {
		return id==null || id.equals("") || id.equals("undefined");
	}
	
	public static String createNewObjectId() {
		return new ObjectId().toString();
	}
	
	public DaoImpl(Class<?> clazz) {
		this.clazz = (Class<T>)clazz;
		this.collectionName = clazz.getSimpleName().substring(0,1).toLowerCase() + clazz.getSimpleName().substring(1);
	}
	
	public DaoImpl(Class<?> clazz, String collectionName) {
		this.clazz = (Class<T>)clazz;
		this.collectionName = collectionName;
	}
}
