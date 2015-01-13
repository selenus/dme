package de.dariah.schereg.base.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import de.dariah.base.model.base.NamedEntityImpl;

@Entity
@Table(name="data_type")
public class DataType extends NamedEntityImpl {
	
	@Id
	@TableGenerator(name = "common_gen", table="universal_id", valueColumnName="id", allocationSize=1, pkColumnName="sequence_name", pkColumnValue="all")
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "common_gen")
	private int id;
	
	@Override public int getId() { return id; }
	@Override public void setId(int id) { this.id = id; }
	
	/** Constructs a default data type */
	public DataType() {}
	
	/** Constructs a data type */
	public DataType(Integer id, String name, String description) { 
		this.setId(id); 
		this.setName(name); 
		this.setDescription(description); 
	}
	
	
}
