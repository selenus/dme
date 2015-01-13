package de.dariah.base.model.base;

import java.util.Deque;
import java.util.LinkedList;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;



import de.dariah.base.model.impl.UserAnnotation;
import de.dariah.schereg.base.model.Schema;

@MappedSuperclass
public abstract class SchemaElementImpl extends NamedEntityImpl implements SchemaElement, UserAnnotateable {
	private static final long serialVersionUID = -5714562340364078091L;

	@Id
	@TableGenerator(name = "common_gen", table="universal_id", valueColumnName="id", allocationSize=1, pkColumnName="sequence_name", pkColumnValue="all")
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "common_gen")
	private int id;
	
	@ManyToOne
    @JoinColumn(name="schema_id")
	private Schema schema;
	
	@Transient
	private BaseEntity baseElement;
	
	@Transient
	private final Deque<UserAnnotation> userAnnotationsInSession = new LinkedList<UserAnnotation>();
	

	public Deque<UserAnnotation> getUserAnnotationsInSession() { return userAnnotationsInSession; } 
	
	@Override 
	public int getId() { return id; }
	@Override 
	public void setId(int id) { this.id = id; }
	
	@Override
	public BaseEntity getBaseElement() { return baseElement; }
	@Override
	public void setBaseElement(BaseEntity baseElement) { this.baseElement = baseElement; }

	@Override
	public Schema getSchema() { return schema; }
	@Override
	public void setSchema(Schema schema) { this.schema = schema; }
	
	@Override
	public Identifiable getAggregatorObject() {
		return getSchema();
	}
}
	