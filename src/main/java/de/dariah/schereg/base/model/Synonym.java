package de.dariah.schereg.base.model;

import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import de.dariah.base.model.base.SchemaElement;
import de.dariah.base.model.base.SchemaElementImpl;

@javax.persistence.Entity
@Table(name="synonym")
public class Synonym extends SchemaElementImpl implements SchemaElement {
	
	@ManyToOne
    @JoinColumn(name="element_id")
	private Entity entity;

	public Entity getEntity() {
		return entity;
	}

	public void setEntity(Entity entity) {
		this.entity = entity;
	}

}
