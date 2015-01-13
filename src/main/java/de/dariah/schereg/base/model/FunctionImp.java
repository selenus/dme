package de.dariah.schereg.base.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="function_implementation")
public class FunctionImp {

	/** Stores the function id */
	@Column(name="function_id")
	@Id
	private Integer functionID;
	
	/** Stores the implementation language */
	@Column(name="language")
	private String language;
	
	/** Stores the implementation dialect */
	@Column(name="dialect")
	private String dialect;
	
	/** Stores the function implementation */
	@Column(name="implementation")
	private String implementation;
	
	public Integer getFunctionID() {
		return functionID;
	}

	public void setFunctionID(Integer functionID) {
		this.functionID = functionID;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getDialect() {
		return dialect;
	}

	public void setDialect(String dialect) {
		this.dialect = dialect;
	}

	public String getImplementation() {
		return implementation;
	}

	public void setImplementation(String implementation) {
		this.implementation = implementation;
	}

	/** Constructs a default function implementation */
	public FunctionImp() {}
	
	/** Constructs a function implementation */
	public FunctionImp(Integer functionID, String language, String dialect, String implementation)
		{ this.functionID=functionID; this.language=language; this.dialect=dialect; this.implementation=implementation; }
	
}
