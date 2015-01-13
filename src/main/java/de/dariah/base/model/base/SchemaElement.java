package de.dariah.base.model.base;

import java.util.Deque;

import de.dariah.base.model.impl.UserAnnotation;
import de.dariah.schereg.base.model.Schema;

public interface SchemaElement extends NamedEntity {
	
	public Deque<UserAnnotation> getUserAnnotationsInSession();
	
	public BaseEntity getBaseElement();
	public void setBaseElement(BaseEntity baseElement);

	public Schema getSchema();
	public void setSchema(Schema schema);
}
