package de.dariah.schereg.base.model;

import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import de.dariah.base.model.base.SchemaElementImpl;

@javax.persistence.Entity
@Table(name="alias")
public class Alias extends SchemaElementImpl {
	
	@ManyToOne
    @JoinColumn(name="element_id")
	private Entity element;
	
	public Entity getElement() { return element; }
	public void setElement(Entity element) { this.element = element; }
}
