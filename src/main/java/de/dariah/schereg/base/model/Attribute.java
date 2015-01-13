package de.dariah.schereg.base.model;

import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import de.dariah.base.model.base.ConfigurableSchemaElementImpl;
import de.dariah.base.model.base.SchemaElementImpl;

@javax.persistence.Entity
@Table(name="attribute")
public class Attribute extends ConfigurableSchemaElementImpl {
	private static final long serialVersionUID = 8772177259631295004L;

	@ManyToOne
    @JoinColumn(name="entity_id")
	private Entity entity;
	
	@ManyToOne
    @JoinColumn(name="domain_id")
	private Domain domain;
	
	@Column(name="min")
	private Integer min;
	
	@Column(name="max")
	private Integer max;
	
	@Column(name="key")
	private Character key;

	public Entity getEntity() { return entity; }
	public void setEntity(Entity entity) { this.entity = entity; }

	public Domain getDomain() { return domain; }
	public void setDomain(Domain domain) { this.domain = domain; }

	public Integer getMin() { return min; }
	public void setMin(Integer min) { this.min = min; }

	public Integer getMax() { return max; }
	public void setMax(Integer max) { this.max = max; }

	public Character isKey() { return key; }
	public void setKey(Character key) { this.key = key; }
	
	public boolean getIsKey() { return key=='1'; }
	public void setIsKey(boolean key) { this.key = '1'; }
	
}