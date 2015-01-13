package de.dariah.schereg.base.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Immutable;

import de.dariah.base.model.base.Identifiable;

/**
 * This entity serves as a means to access all sorts of SchemaElements from a UNION view over the respective tables.
 *  In order to access the actual SchemaElement (e.g. Attribute, Containment, Relationship) use their concrete entities.
 *  
 * @author Tobias Gradl, University of Bamberg
 */
@Entity
@Table(name="schema_elements")
@Immutable
public class ReadOnlySchemaElement implements Identifiable, Serializable {
	private static final long serialVersionUID = -9006508698481707299L;

	@Id
	private Integer id;
	
	@Column(name="name")
	private String name;
	
	@Column(name="description")
	private String description;
	
	@Column(name="type")
	private String type;
		
	@Column(name="schema_id")
	private int schemaId;
	
	@Override public int getId() { return id; }
	@Override public void setId(int id) { this.id = id; }
	
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	
	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }
	
	public String getType() { return type; }
	public void setType(String type) { this.type = type; }
	
	public int getSchemaId() { return schemaId; }
	public void setSchemaId(int schemaId) { this.schemaId = schemaId; }
}
