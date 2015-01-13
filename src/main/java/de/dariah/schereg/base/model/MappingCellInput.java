package de.dariah.schereg.base.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import de.dariah.base.model.base.Identifiable;

@Entity
@Table(name="mapping_cell_input")
public class MappingCellInput implements Identifiable, Serializable {
	private static final long serialVersionUID = 458806944320760908L;

	@Id
	@TableGenerator(name = "entry_gen", table="universal_id", valueColumnName="id", allocationSize=1, pkColumnName="sequence_name", pkColumnValue="mapping_cell")
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "entry_gen")
	private Integer id;
		
	@Column(name="entity_id")
	private Integer elementID = null;

	@Column(name="constant_value")
	private String constant = null;

	@Column(name="constant_data_type")
	private String constantType = null;
	
	@ManyToOne
    @JoinColumn(name="mapping_cell_id")
	private MappingCell mappingCell;
		
	public int getId() { return id; }
	public void setId(int id) { this.id = id; }
	
	public Integer getElementID() { return elementID; }
	public void setElementID(Integer elementID) { this.elementID = elementID; }

	public String getConstant() { return constant; }
	public void setConstant(String constant) { this.constant = constant; }

	public String getConstantType() { return constantType; }
	public void setConstantType(String constantType) { this.constantType = constantType; }
	
	public MappingCell getMappingCell() { return mappingCell; }
	public void setMappingCell(MappingCell mappingCell) { this.mappingCell = mappingCell; }
	
	public MappingCellInput() {}	
	
	/** Constructs the mapping cell input for an element ID */
	public MappingCellInput(Integer elementID)
		{ this.elementID = elementID; }

	/** Constructs the mapping cell input for a constant */
	public MappingCellInput(String constant)
		{ this.constant = constant; }
	
	/** Indicates if the input is a constant */
	public boolean isConstant()
		{ return constant!=null; }

	/** Retrieves the mapping cell input from the string */
	static public MappingCellInput parse(String value)
	{ 
    	if(value.matches("\".*\"")) return new MappingCellInput(value.substring(1,value.length()-2));
    	else try {return new MappingCellInput(Integer.parseInt(value)); }
    	catch(Exception e) { return null; }
	}
}
