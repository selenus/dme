package de.dariah.schereg.base.model;

import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import de.dariah.base.model.base.Identifiable;
import de.dariah.base.model.base.NamedEntityImpl;
import de.dariah.base.model.base.UserAnnotateable;
import de.dariah.base.model.impl.UserAnnotation;

@Entity
@Table(name="mapping_cell")
public class MappingCell extends NamedEntityImpl implements UserAnnotateable {
	private static final long serialVersionUID = 5659140481136145563L;

	@Id
	@TableGenerator(name = "common_gen", table="universal_id", valueColumnName="id", allocationSize=1, pkColumnName="sequence_name", pkColumnValue="all")
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "common_gen")
	private int id;
	
	@Override public int getId() { return id; }
	@Override public void setId(int id) { this.id = id; }
	
	/** Defines the identify function */
	private static final Integer IDENTIFY_FUNCTION = 450;

	@ManyToOne
    @JoinColumn(name="mapping_id")
	private Mapping mapping;
	
	/** Stores the second mapping cell element */
	@Column(name="output_id")
	private Integer output;

	/** Stores the mapping cell score */
	@Column(name="score")
	private Double score;

	/** Stores the reference to the function used by this mapping cell */
	@Column(name="function_id")
    private Integer functionID;

	/**
	 * Either this or function_id to reference an external function
	 *  Required e. g. once we move towards other expressions than XML-based ones
	 */
	@Column(name="function")
	private String function;
	
	/** Stores the mapping cell author */
	@Column(name="author")
	private String author;

	/** Stores notes about the mapping cell */
	@Column(name="notes")
	private String notes;
	
	@Transient
	private final Deque<UserAnnotation> userAnnotationsInSession = new LinkedList<UserAnnotation>();
	

	public Deque<UserAnnotation> getUserAnnotationsInSession() { return userAnnotationsInSession; } 
	
	@OneToMany(mappedBy="mappingCell", fetch = FetchType.LAZY, orphanRemoval=true)
	@Cascade({CascadeType.SAVE_UPDATE, CascadeType.DELETE})
	private Set<MappingCellInput> mappingCellInputs;
	
	public Integer getOutput() { return output; }
	public void setOutput(Integer output) { this.output = output; }

	public Double getScore() { return score; }
	public void setScore(Double score) { this.score = score; }

	public Integer getFunctionID() { return functionID; }
	public void setFunctionID(Integer functionID) { this.functionID = functionID; }

	public String getAuthor() { return author; } 
	public void setAuthor(String author) { this.author = author; }

	public String getNotes() { return notes; }
	public void setNotes(String notes) { this.notes = notes; }
		
	public Set<MappingCellInput> getMappingCellInputs() { return mappingCellInputs; }
	public void setMappingCellInputs(Set<MappingCellInput> mappingCellInputs) { this.mappingCellInputs = mappingCellInputs; }
	
	public Mapping getMapping() { return mapping; }
	public void setMapping(Mapping mapping) { this.mapping = mapping; }
	
	public String getFunction() { return function; }
	public void setFunction(String function) { this.function = function; }
	
	@Override
	public Identifiable getAggregatorObject() { return this.getMapping(); }
	
	public Integer getMappingId() {
		if (mapping == null) {
			return null;
		}
		return mapping.getId();
	}
	
	public void addMappingCellInput(MappingCellInput mappingCellInput) {
		if (this.mappingCellInputs==null) {
			this.mappingCellInputs = new HashSet<MappingCellInput>();
		}
		this.mappingCellInputs.add(mappingCellInput);
		mappingCellInput.setMappingCell(this);
	}

	/** Indicates if the mapping cell has been validated */
	public Boolean isValidated() { 
		return functionID!=null; 
	}
	
	/** Indicates if the identity function is being used */
	public Boolean isIdentityFunction() { 
		return functionID==null || functionID.equals(IDENTIFY_FUNCTION); 
	}
}
