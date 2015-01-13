package de.dariah.base.model.impl;

import de.dariah.aai.javasp.base.User;
import de.dariah.base.model.base.Identifiable;
import de.dariah.samlsp.orm.model.UserImpl;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

@Entity
@Table(name="user_annotation")
public class UserAnnotation implements Identifiable {
	private static final long serialVersionUID = -2590960050996427670L;
	
	public static enum ACTION_TYPES {
		CREATE,
		UPDATE,
		DELETE,
		COMMENT_ONLY;
	}
	
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_annotation_id_seq")
    @SequenceGenerator(name = "user_annotation_id_seq", sequenceName = "user_annotation_id_seq")
	@Column(name="id")
	private int id;

	@ManyToOne
	@JoinColumn(name="parent_id")
	private UserAnnotation parent;
	
	@ManyToOne
    @JoinColumn(name = "user_id")
	private UserImpl user;
	
	@Column(name="annotated_object_type")
	private String annotatedObjectType;
	
	@Column(name="annotated_object_id")
	private int annotatedObjectId;
	
	@Column(name="aggregator_object_type")
	private String aggregatorObjectType;
	
	@Column(name="aggregator_object_id")
	private int aggregatorObjectId;
	
	@Column(name="action_type")
	private ACTION_TYPES actionType;
		
	@Column(name="object_snapshot")
	private Identifiable objectSnapshot;
	
	@Column(name="comment")
	private String comment;
	
	@Column(name="created")
	@Type(type="org.jadira.usertype.dateandtime.joda.PersistentDateTime")
	private DateTime created;
	
	@Override public int getId() { return this.id; }
	@Override public void setId(int id) { this.id = id;}
	
	public UserAnnotation getParent() { return parent; }
	public void setParent(UserAnnotation parent) { this.parent = parent; }
	
	public UserImpl getUser() { return user; }
	public void setUser(UserImpl user) { this.user = user; }
	
	public String getAnnotatedObjectType() { return annotatedObjectType; }
	public void setAnnotatedObjectType(String annotatedObjectType) { this.annotatedObjectType = annotatedObjectType; }
	
	public int getAnnotatedObjectId() { return annotatedObjectId; }
	public void setAnnotatedObjectId(int annotatedObjectId) { this.annotatedObjectId = annotatedObjectId; }
	
	public String getAggregatorObjectType() { return aggregatorObjectType; }
	public void setAggregatorObjectType(String aggregatorObjectType) { this.aggregatorObjectType = aggregatorObjectType; }
	
	public int getAggregatorObjectId() { return aggregatorObjectId; }
	public void setAggregatorObjectId(int aggregatorObjectId) { this.aggregatorObjectId = aggregatorObjectId; }
	
	public ACTION_TYPES getActionType() { return actionType; }
	public void setActionType(ACTION_TYPES actionType) { this.actionType = actionType; }
	
	public Identifiable getObjectSnapshot() { return objectSnapshot; }
	public void setObjectSnapshot(Identifiable objectSnapshot) { this.objectSnapshot = objectSnapshot; }
	
	public String getComment() { return comment; }
	public void setComment(String comment) { this.comment = comment; }
	
	public DateTime getCreated() { return created; }
	public void setCreated(DateTime created) { this.created = created; }
}
