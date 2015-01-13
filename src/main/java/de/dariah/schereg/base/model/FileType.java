package de.dariah.schereg.base.model;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import de.dariah.base.model.base.BaseEntityImpl;

@javax.persistence.Entity
@Table(name="file_types")
public class FileType extends BaseEntityImpl {

	@Id
	@TableGenerator(name = "common_gen", table="universal_id", valueColumnName="id", allocationSize=1, pkColumnName="sequence_name", pkColumnValue="all")
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "common_gen")
	private int id;
	
	@Column(name="name")
	private String name;	
	
	@Column(name="description")
	private String description;	
	
	@Override 
	public int getId() { return id; }
	@Override 
	public void setId(int id) { this.id = id; }
	
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	
	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }
}
