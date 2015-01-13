package de.dariah.schereg.base.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang.builder.HashCodeBuilder;


@javax.persistence.Entity
@Table(name="annotation")
public class Annotation implements Serializable {
				
	@Id
	@ManyToOne
    @JoinColumn(name="element_id")
	private Entity element;

	@Id
	@ManyToOne
    @JoinColumn(name="group_id")
	private Entity group;
	
	/** Stores the annotated attribute name */
	@Column(name="attribute")
	private String attribute;
	
	/** Stores the element value */
	@Column(name="value")
	private String value;

	public Entity getElement() { return element; }
	public void setElement(Entity element) { this.element = element; }

	public Entity getGroup() { return group; }
	public void setGroup(Entity group) { this.group = group; }

	public String getAttribute() { return attribute; }
	public void setAttribute(String attribute) { this.attribute = attribute; }

	public String getValue() { return value; }
	public void setValue(String value) { this.value = value; }
	
	@Override
	public int hashCode() {
		//if ((element != null && element.getId() > 0) || (group != null && group.getId() > 0)){
			int hashCode = 19;
			if (element != null) {
				hashCode = 12 * hashCode + element.hashCode();
			}
			if (group != null) {
				hashCode = 12 * hashCode + group.hashCode();
			}
			return hashCode;
	    // } else {
	    //    return super.hashCode();
	    //}		
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) { 
			return true;
		}
		if ((obj == null) || (obj.getClass() != this.getClass())) {
			return false;
		}
		
		Annotation rhs = (Annotation)obj;		
		if (element != null && (rhs.getElement() == null || !rhs.getElement().equals(element))) {
			return false;
		}
		if (element == null && rhs.getElement() != null) {
			return false;
		}
		if (group != null && (rhs.getGroup() == null || !rhs.getGroup().equals(group))) {
			return false;
		}
		if (group == null && rhs.getGroup() != null) {
			return false;
		}
		
		return true;	
	}
}
