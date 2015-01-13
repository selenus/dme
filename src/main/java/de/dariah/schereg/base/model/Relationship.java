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
import de.dariah.base.model.base.NamedEntityImpl;
import de.dariah.base.model.base.SchemaElementImpl;

@javax.persistence.Entity
@Table(name="relationship")
public class Relationship extends SchemaElementImpl {
	
	/*@Id
	@TableGenerator(name = "common_gen", table="universal_id", valueColumnName="id", allocationSize=1, pkColumnName="sequence_name", pkColumnValue="all")
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "common_gen")
	private int id;
	
	@Override public int getId() { return id; }
	@Override public void setId(int id) { this.id = id; }*/
	
    @Column(name="left_id")
	private int leftId;
	
	@Transient
	private BaseEntity left;
	
	@Column(name="left_min")
	private Integer leftMin;
	
	@Column(name="left_max")
	private Integer leftMax;
	
    @Column(name="right_id")
	private int rightId;
	
	@Transient
	private BaseEntity right;
	
	@Column(name="right_min")
	private Integer rightMin;
	
	@Column(name="right_max")
	private Integer rightMax;

	public BaseEntity getLeft() { return left; }
	public void setLeft(BaseEntity left) { this.left = left; }

	public Integer getLeftMin() { return leftMin; }
	public void setLeftMin(Integer leftMin) { this.leftMin = leftMin; }

	public Integer getLeftMax() { return leftMax; }
	public void setLeftMax(Integer leftMax) { this.leftMax = leftMax; }

	public BaseEntity getRight() { return right; }
	public void setRight(BaseEntity right) { this.right = right; }

	public Integer getRightMin() { return rightMin; }
	public void setRightMin(Integer rightMin) { this.rightMin = rightMin; }

	public Integer getRightMax() { return rightMax; }
	public void setRightMax(Integer rightMax) { this.rightMax = rightMax; }
	
	public int getLeftId() { return leftId; }
	public void setLeftId(int leftId) { this.leftId = leftId; }
	
	public int getRightId() { return rightId; }
	public void setRightId(int rightId) { this.rightId = rightId; }
}
