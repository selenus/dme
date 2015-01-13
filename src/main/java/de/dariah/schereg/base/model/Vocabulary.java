package de.dariah.schereg.base.model;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.TableGenerator;

import de.dariah.base.model.base.BaseEntityImpl;

public class Vocabulary extends BaseEntityImpl {
	@Id
	@TableGenerator(name = "common_gen", table="universal_id", valueColumnName="id", allocationSize=1, pkColumnName="sequence_name", pkColumnValue="all")
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "common_gen")
	private int id;
	
	@Override public int getId() { return id; }
	@Override public void setId(int id) { this.id = id; }
}