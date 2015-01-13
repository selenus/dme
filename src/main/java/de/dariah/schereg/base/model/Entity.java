package de.dariah.schereg.base.model;

import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import de.dariah.base.model.base.SchemaElementImpl;

@javax.persistence.Entity
@Table(name="entity")
public class Entity extends SchemaElementImpl {}