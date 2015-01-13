package de.dariah.schereg.base.model;

import java.util.Set;

import javax.persistence.CascadeType;
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

import de.dariah.base.model.base.NamedEntityImpl;
import de.dariah.base.model.base.SchemaElementImpl;

@Entity
@Table(name="domain")
public class Domain extends SchemaElementImpl  {
			
	@OneToMany(mappedBy="domain", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<Attribute> attributes;
	
	@OneToMany(mappedBy="domain", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private Set<DomainValue> domainValues;
	
	public Set<Attribute> getAttributes() { return attributes; }
	public void setAttributes(Set<Attribute> attributes) { this.attributes = attributes; }

	public Set<DomainValue> getDomainValues() { return domainValues; }
	public void setDomainValues(Set<DomainValue> domainValues) { this.domainValues = domainValues; }
}
