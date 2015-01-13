package de.dariah.schereg.base.model;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Set;

import javax.persistence.CascadeType;
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

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import de.dariah.base.model.base.BaseEntityImpl;
import de.dariah.base.model.base.Identifiable;
import de.dariah.base.model.base.UserAnnotateable;
import de.dariah.base.model.impl.UserAnnotation;

@Entity
@Table(name="mapping")
public class Mapping extends BaseEntityImpl implements UserAnnotateable {
	private static final long serialVersionUID = -4482287728519996869L;

	@Id
	@TableGenerator(name = "common_gen", table="universal_id", valueColumnName="id", allocationSize=1, pkColumnName="sequence_name", pkColumnValue="all")
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "common_gen")
	private int id;
	
	@Override public int getId() { return id; }
	@Override public void setId(int id) { this.id = id; }
		
	@Column(name="is_locked")
	private Boolean isLocked;	
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "project_id", nullable = false)
	private Project project;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "target_id", nullable = false)
	/** Validation that source and target are unequal is performed in MappingValidator */
	private Schema target;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "source_id", nullable = false)
	/** Validation that source and target are unequal is performed in MappingValidator */
	private Schema source;

	@Transient
	private final Deque<UserAnnotation> userAnnotationsInSession = new LinkedList<UserAnnotation>();
	

	public Deque<UserAnnotation> getUserAnnotationsInSession() { return userAnnotationsInSession; } 
	
	@Column(name="state")
	private int state;
			
	@Column(name="message")
	private String message;
	
	@Transient
	private boolean performMatching;
	
	public boolean isPerformMatching() {
		return performMatching;
	}
	public void setPerformMatching(boolean performMatching) {
		this.performMatching = performMatching;
	}

	/* LazyCollectionOption.EXTRA makes sure that Counts are executed on the collection (SQL: Count(id))
	 *	instead of initializing the whole collection */
	@OneToMany(mappedBy="mapping", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@LazyCollection(LazyCollectionOption.EXTRA)
	private Set<MappingCell> mappingCells;
			
	public Integer getProjectId() {
		if (project==null) {
			return null;
		}
		return project.getId();
	}

	public int getSourceId() {
		if (source==null) {
			return -1;
		}
		return source.getId();
	}

	public int getTargetId() {
		if (target==null) {
			return -1;
		}
		return target.getId();
	}

	public Schema getTarget() {
		return target;
	}


	public Schema getSource() {
		return source;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setTarget(Schema target) {
		this.target = target;
	}

	public void setSource(Schema source) {
		this.source = source;
	}

	public Set<MappingCell> getMappingCells() {
		return mappingCells;
	}

	public void setMappingCells(Set<MappingCell> mappingCells) {
		this.mappingCells = mappingCells;
	}

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

	public Boolean getIsLocked() {
		return isLocked;
	}

	public void setIsLocked(Boolean isLocked) {
		this.isLocked = isLocked;
	}
	
	/** Constructs a default mapping */
	public Mapping() {}

	/** Constructs a mapping */
	public Mapping(Project project, Schema source, Schema target)
		{ this.project = project; this.source = source; this.target = target; }

	/** Copies the mapping */
	public Mapping copy()
		{ return new Mapping(getProject(),getSource(),getTarget()); }
		
	@Override
	public Identifiable getAggregatorObject() { return this; }	
}
