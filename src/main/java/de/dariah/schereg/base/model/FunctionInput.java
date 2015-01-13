package de.dariah.schereg.base.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import de.dariah.base.model.base.BaseEntityImpl;

@Entity
@Table(name="function_input")
public class FunctionInput extends BaseEntityImpl {
	
	@Id
	@TableGenerator(name = "common_gen", table="universal_id", valueColumnName="id", allocationSize=1, pkColumnName="sequence_name", pkColumnValue="all")
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "common_gen")
	private int id;
	
	@Override public int getId() { return id; }
	@Override public void setId(int id) { this.id = id; }
	
	@Column(name="function_id")
	private Integer functionId;
	
	@Column(name="input_type")
	private Integer inputType;
	
	@Column(name="input_loc")
	private Integer inputLoc;
	
	public Integer getFunctionId() { return functionId; }
	public void setFunctionId(Integer functionId) { this.functionId = functionId; }
	
	public Integer getInputType() { return inputType; }
	public void setInputType(Integer inputType) { this.inputType = inputType; }
	
	public Integer getInputLoc() { return inputLoc; }
	public void setInputLoc(Integer inputLoc) { this.inputLoc = inputLoc; }
}
