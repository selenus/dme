package de.dariah.schereg.base.model;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import de.dariah.base.model.base.NamedEntityImpl;

@Entity
@Table(name="project")
public class Project extends NamedEntityImpl {

	@Id
	@TableGenerator(name = "common_gen", table="universal_id", valueColumnName="id", allocationSize=1, pkColumnName="sequence_name", pkColumnValue="all")
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "common_gen")
	private int id;
	
	@Override public int getId() { return id; }
	@Override public void setId(int id) { this.id = id; }
	
	@Column(name="author")	
	private String author;

	/** Stores the project schemas */
	//private ProjectSchema[] schemas;
	
	@ManyToMany(targetEntity= Schema.class ,cascade = CascadeType.ALL)
	@JoinTable(name="project_schema", joinColumns = @JoinColumn(name = "project_id"), inverseJoinColumns = @JoinColumn(name = "schema_id"))
	private Set<Schema> schemas;
	
	
	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	/*public ProjectSchema[] getSchemas() {
		return schemas;
	}

	public void setSchemas(ProjectSchema[] schemas) {
		this.schemas = schemas;
	}*/

	public Set<Schema> getSchemas() {
		return schemas;
	}

	public void setSchemas(Set<Schema> schemas) {
		this.schemas = schemas;
	}

	/** Constructs a default project */	public Project() {}

	/** Constructs a project */
	/*public Project(Integer id, String name, String description, String author, ProjectSchema[] schemas)
		{ this.setId(id); this.name = name; this.description = description; this.author = author; this.schemas = schemas; }
*/
	/** Copies the project */
	/*public Project copy()
		{ return new Project(getId(),getName(),getDescription(),getAuthor(),getSchemas()); }
	*/
	
	/** Returns the list of project schema IDs */
	/*public Integer[] getSchemaIDs()
	{
		ArrayList<Integer> schemaIDs = new ArrayList<Integer>();
		if(schemas!=null)
			for(ProjectSchema schema : schemas)
				schemaIDs.add(schema.getId());
		return schemaIDs.toArray(new Integer[0]);
	}*/
	
	/** Retrieves the schema model for the specified schema */
	/*public SchemaModel getSchemaModel(Integer schemaID)
	{
		for(ProjectSchema schema : schemas)
			if(schema.getId().equals(schemaID))
				return schema.geetSchemaModel();
		return null;
	}*/
	
}
