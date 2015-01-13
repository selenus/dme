package de.dariah.schereg.base.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import de.dariah.base.model.base.NamedEntityImpl;

@Entity
@Table(name="data_source")
public class DataSource extends NamedEntityImpl {
	
	@Id
	@TableGenerator(name = "common_gen", table="universal_id", valueColumnName="id", allocationSize=1, pkColumnName="sequence_name", pkColumnValue="all")
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "common_gen")
	private int id;
	
	@Override public int getId() { return id; }
	@Override public void setId(int id) { this.id = id; }
	
	/** Stores the data source URL */
	@Column(name="url")
	private String url;
	
	/** Stores the data source schema ID */
	@Column(name="schema_id")
	private Integer schemaID;
	
	/** Stores the data source element ID */
	@Column(name="element_id")
	private Integer elementID;

	public String getUrl() { return url; }
	public void setUrl(String url) { this.url = url; }

	public Integer getSchemaID() { return schemaID; }
	public void setSchemaID(Integer schemaID) { this.schemaID = schemaID; }

	public Integer getElementID() { return elementID; }
	public void setElementID(Integer elementID) { this.elementID = elementID; }

	/** Constructs a default data source */
	public DataSource() {} 
	
	/** Constructs a data source object */
	public DataSource(Integer id, String name, String url, Integer schemaID, Integer elementID) { 
		this.setId(id); 
		super.setName(name); 
		this.url = url; 
		this.schemaID = schemaID; 
		this.elementID = elementID; 
	}
}
