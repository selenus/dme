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
@Table(name="functions")
public class Function extends NamedEntityImpl {
		
	@Id
	@TableGenerator(name = "common_gen", table="universal_id", valueColumnName="id", allocationSize=1, pkColumnName="sequence_name", pkColumnValue="all")
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "common_gen")
	private int id;
	
	@Override public int getId() { return id; }
	@Override public void setId(int id) { this.id = id; }
	
	/** Stores the function expression */
	@Column(name="expression")
	private String expression;
	
	/** Stores the function category */
	@Column(name="category")
	private String category;
	
	/** Stores the input types */
	// bag! @Column(name="domain_id")
	private Integer inputTypes[];
	
	/** Stores the output type */
	@Column(name="outputType")
	private Integer outputType;
	
	public String getExpression() { return expression; }
	public void setExpression(String expression) { this.expression = expression; }

	public String getCategory() { return category; }
	public void setCategory(String category) { this.category = category; }

	public Integer[] getInputTypes() { return inputTypes; }
	public void setInputTypes(Integer[] inputTypes) { this.inputTypes = inputTypes; }

	public Integer getOutputType() { return outputType; }
	public void setOutputType(Integer outputType) { this.outputType = outputType; }

	/** Constructs a default function */
	public Function() {}
	
	/** Constructs a function */
	public Function(Integer id, String name, String description, String expression, String category, Integer inputTypes[], Integer outputType) { 
		this.setId(id); 
		this.setName(name);
		this.setDescription(description); 
		this.expression=expression; 
		this.category=category; 
		this.inputTypes=inputTypes; 
		this.outputType=outputType; 
	}
	
}
