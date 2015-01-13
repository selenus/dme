package de.dariah.schereg.base.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import de.dariah.base.model.base.BaseEntityImpl;
import de.dariah.base.model.base.NamedEntityImpl;
import de.dariah.base.model.base.SchemaElement;
import de.dariah.base.model.base.SchemaElementImpl;

@Entity
@Table(name="domainvalue")
public class DomainValue extends SchemaElementImpl implements SchemaElement {
	
	@Column(name="value")
	private String value;
		
	@ManyToOne
    @JoinColumn(name="domain_id")
	private Domain domain;
		
	public String getValue() { return value; }
	public void setValue(String value) { this.value = value; }

	public Domain getDomain() { return domain; }
	public void setDomain(Domain domain) { this.domain = domain; }
	
	/** These are required for matching */
	@Override public String getName() { return this.value; }
	@Override public void setName(String name) { this.value = name; }
}
