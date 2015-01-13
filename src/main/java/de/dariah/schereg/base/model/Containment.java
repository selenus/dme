package de.dariah.schereg.base.model;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;

import de.dariah.base.model.base.BaseEntity;
import de.dariah.base.model.base.ConfigurableSchemaElementImpl;
import de.dariah.base.model.base.SchemaElementImpl;

@javax.persistence.Entity
@Table(name="containment")
public class Containment extends ConfigurableSchemaElementImpl {
	
	@Column(name="parent_id")
	private Integer parentId;
	
	@Column(name="child_id")
	private Integer childId;
	
	@Transient
	private BaseEntity parent;
	
	@Transient
	private BaseEntity child;
	
	@Column(name="min")
	private Integer min;
	
	@Column(name="max")
	private Integer max;

	public Integer getParentId() { return parentId; }
	public void setParentId(Integer parentId) { this.parentId = parentId; }

	public Integer getChildId() { return childId; } 
	public void setChildId(Integer childId) { this.childId = childId; }

	public BaseEntity getParent() { return parent; }
	public void setParent(BaseEntity parent) { this.parent = parent; }
	
	public BaseEntity getChild() { return child; }
	public void setChild(BaseEntity child) { this.child = child; }
	
	public Integer getMin() { return min; }
	public void setMin(Integer min) { this.min = min; }
	
	public Integer getMax() { return max; }
	public void setMax(Integer max) { this.max = max; }

	
}
