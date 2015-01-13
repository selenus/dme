package de.dariah.schereg.base.model;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@javax.persistence.Entity
@Table(name="namespace")
public class Namespace {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "namespace_id_seq")
    @SequenceGenerator(name = "namespace_id_seq", sequenceName = "namespace_id_seq")
	@Column(name="id")
	private int id;

	@Column
	private String prefix;
	
	@Column
	private String uri;
	
	@ManyToOne
    @JoinColumn(name="schema_id")
	private Schema schema;

	public int getId() { return id; }
	public void setId(int id) { this.id = id; }

	public String getPrefix() { return prefix; }
	public void setPrefix(String prefix) { this.prefix = prefix; }

	public String getUri() { return uri; }
	public void setUri(String uri) { this.uri = uri; }
	
	public Schema getSchema() { return schema; }
	public void setSchema(Schema schema) { this.schema = schema; }		
}
