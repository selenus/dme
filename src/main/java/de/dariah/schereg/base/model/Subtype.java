package de.dariah.schereg.base.model;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import de.dariah.base.model.base.BaseEntity;
import de.dariah.base.model.base.SchemaElementImpl;

@javax.persistence.Entity
@Table(name="subtype")
public class Subtype extends SchemaElementImpl {
		
	@Id
    @Column(name="parent_id")
	private Integer parentId;
	
	@Id
    @Column(name="child_id")
	private Integer childId;
    	    
    @Transient
	private BaseEntity parent;
	
    @Transient
	private BaseEntity child;

	public Integer getParentId() { return parentId; }
	public void setParentId(Integer parentId) { this.parentId = parentId; }
	
	public Integer getChildId() { return childId; }
	public void setChildId(Integer childId) { this.childId = childId; }
	
	public BaseEntity getParent() { return parent; }
	public void setParent(BaseEntity parent) { this.parent = parent; }
	
	public BaseEntity getChild() { return child; }
	public void setChild(BaseEntity child) { this.child = child; }
	
	@Override public int getId() { return hashCode(); }
	@Override public void setId(int id) { }
}
