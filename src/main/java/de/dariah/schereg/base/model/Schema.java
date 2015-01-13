package de.dariah.schereg.base.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.GenericGenerator;

import de.dariah.base.model.base.NamedEntityImpl;
import de.dariah.base.model.base.SchemaElement;
import de.dariah.federation.model.SchemaPojo;

/**
 * @author Tobias Gradl
 * @created 12.12.2011 17:19:53
 * @version 1
 *
 */
@javax.persistence.Entity
@Table(name="schema")
public class Schema extends NamedEntityImpl  {

	@Id
	@TableGenerator(name = "common_gen", table="universal_id", valueColumnName="id", allocationSize=1, pkColumnName="sequence_name", pkColumnValue="all")
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "common_gen")
	private int id;
	
	@Override public int getId() { return id; }
	@Override public void setId(int id) { this.id = id; }
	
	@Size(min=3, max=50)
	@NotNull
	@Override public String getName() { return super.getName(); };
		
	@Column(name="SOURCE")
	//@Pattern(regexp="\\b(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]", 
	//			message="Please provide a valid URL as the schema source.")
	private String source;
	
	@Column(name="TYPE")
	private String type;
		
	@Column(name="is_locked")
	private Boolean isLocked;
	
	@Column(name="state")
	private int state;
	
	@Column(name="message")
	private String message;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "file_id", nullable = true)
	private File file;
	
	@Column(name = "uuid", unique = true)
	private String uuid;

	@Transient
	private byte[] bytes;
	
	@Transient
	private String prevSource;
		
	@ManyToMany(targetEntity= Project.class)
	@Cascade({CascadeType.SAVE_UPDATE})
	@JoinTable(name="project_schema", joinColumns = @JoinColumn(name = "schema_id"), inverseJoinColumns = @JoinColumn(name = "project_id"))
	private Collection<Project> projects;

	@OneToMany(mappedBy="schema", fetch = FetchType.LAZY)
	@Cascade(value=CascadeType.ALL)
	private Collection<Namespace> namespaces;
	
	@OneToMany(mappedBy="schema", fetch = FetchType.LAZY)
	@Cascade(value=CascadeType.ALL)
	private Collection<Attribute> attributes;
	
	@OneToMany(mappedBy="schema", fetch = FetchType.LAZY)
	@Cascade(value=CascadeType.ALL)
	private Collection<Alias> aliases;
	
	@OneToMany(mappedBy="schema", fetch = FetchType.LAZY)
	@Cascade(value=CascadeType.ALL)
	private Collection<Domain> domains;
	
	@OneToMany(mappedBy="schema", fetch = FetchType.LAZY)
	@Cascade(value=CascadeType.ALL)
	private Collection<DomainValue> domainValues;
		
	@OneToMany(mappedBy="schema", fetch = FetchType.LAZY)
	@Cascade(value=CascadeType.ALL)
	private Collection<Entity> entities;
	
	@OneToMany(mappedBy="schema", fetch = FetchType.LAZY)
	@Cascade(value=CascadeType.ALL)
	private Collection<Containment> containments;
	
	@OneToMany(mappedBy="schema", fetch = FetchType.LAZY)
	@Cascade(value=CascadeType.ALL)
	private Collection<Relationship> relationships;
	
	@OneToMany(mappedBy="schema", fetch = FetchType.LAZY)
	@Cascade(value=CascadeType.ALL)
	private Collection<Subtype> subtypes;
	
	@OneToMany(mappedBy="schema", fetch = FetchType.LAZY)
	@Cascade(value=CascadeType.ALL)
	private Collection<Synonym> synonym;
	
	
	public Collection<Attribute> getAttributes() {
		return attributes;
	}

	public void setAttributes(Collection<Attribute> attributes) {
		this.attributes = attributes;
	}

	

	
	public String getUuid() {
		return uuid;
	}
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	public Schema() {
		this.setIsLocked(false);
	}
		
	public Boolean getIsLocked() {
		return isLocked;
	}

	public void setIsLocked(Boolean isLocked) {
		this.isLocked = isLocked;
	}
			

	/**
	 * @return the source
	 */
	public String getSource() {
		return source;
	}
	
	public String getSourceShort() {
		
		if (source == null){
			return "";
		}
		else if (!source.contains("/")) {
			return source;
		}		
		return source.substring(source.lastIndexOf("/")+1);
	}
	
	/**
	 * @param source the source to set
	 */
	public void setSource(String source) {
		this.source = source;
	}
	
	public void resetSource() {
		this.source = this.prevSource;
		this.prevSource = "";
	}
	
	public void rememberSource() {
		this.prevSource = this.source;
	}
	
	public String getPrevSource() {
		return prevSource;
	}

	public void setPrevSource(String prevSource) {
		this.prevSource = prevSource;
	}
	
	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	
	
	public Collection<Project> getProjects() {
		return projects;
	}

	public void setProjects(Collection<Project> projects) {
		this.projects = projects;
	}
	
	
	

	public Collection<Namespace> getNamespaces() {
		return namespaces;
	}
	public void setNamespaces(Collection<Namespace> namespaces) {
		this.namespaces = namespaces;
	}
	public Collection<Alias> getAliases() {
		return aliases;
	}

	public void setAliases(Collection<Alias> aliases) {
		this.aliases = aliases;
	}

	public Collection<Domain> getDomains() {
		return domains;
	}

	public void setDomains(Collection<Domain> domains) {
		this.domains = domains;
	}

	public Collection<DomainValue> getDomainValues() {
		return domainValues;
	}

	public void setDomainValues(Collection<DomainValue> domainValues) {
		this.domainValues = domainValues;
	}

	public Collection<Entity> getEntities() {
		return entities;
	}

	public void setEntities(Collection<Entity> entities) {
		this.entities = entities;
	}

	public Collection<Containment> getContainments() {
		return containments;
	}

	public void setContainments(Collection<Containment> containments) {
		this.containments = containments;
	}

	public Collection<Relationship> getRelationships() {
		return relationships;
	}

	public void setRelationships(Collection<Relationship> relationships) {
		this.relationships = relationships;
	}

	public Collection<Subtype> getSubtypes() {
		return subtypes;
	}

	public void setSubtypes(Collection<Subtype> subtypes) {
		this.subtypes = subtypes;
	}

	public Collection<Synonym> getSynonym() {
		return synonym;
	}

	public void setSynonym(Collection<Synonym> synonym) {
		this.synonym = synonym;
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

	public byte[] getBytes() {
		return bytes;
	}

	public void setBytes(byte[] bytes) {
		this.bytes = bytes;
	}

	public File getFile() { return file; }
	public void setFile(File file) { this.file = file; }
	
	
	/** Constructs a schema */
	public Schema(Integer id, String name, String source, String type, String description, boolean locked)
		{ this.setId(id); this.setName(name); this.source = source; this.type = type; this.setDescription(description); this.setIsLocked(locked); }

	
	/** Copies the schema */
	//public Schema copy()
	//	{ return new Schema(getId(),getName(),getAuthor(),getSource(),getType(),getDescription(),getIsLocked()); }
	
	public SchemaPojo toPojo() {
		SchemaPojo pojo = new SchemaPojo();
		pojo.setId(getId());
		pojo.setName(getName());
		pojo.setDescription(getDescription());
		pojo.setUuid(getUuid());
		pojo.setNamespaces(new HashMap<String, String>());
		
		if (namespaces != null) {
			for (Namespace ns : namespaces) {
				pojo.getNamespaces().put(ns.getPrefix(), ns.getUri());
			}
		}
		
		return pojo;
	}
	
}
