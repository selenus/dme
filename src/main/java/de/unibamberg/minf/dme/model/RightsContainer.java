package de.unibamberg.minf.dme.model;

import java.util.List;

import de.unibamberg.minf.dme.model.tracking.Change;
import de.unibamberg.minf.dme.model.tracking.ChangeType;
import de.unibamberg.minf.dme.model.tracking.TrackedEntity;

public class RightsContainer<T extends TrackedEntity> implements TrackedEntity {
	private static final long serialVersionUID = -1272567909444423065L;
	
	private String id;
	private T element;
	
	private boolean draft;
	private boolean readOnly;
	private String ownerId;
	
	private List<String> readIds;
	private List<String> writeIds;
	private List<String> shareIds;
	
	
	@Override public String getId() { return this.id; }
	@Override public void setId(String id) { 
		this.id = id;
		if (this.element!=null) {
			this.element.setId(id); 
		}
	}

	public T getElement() { return element; }
	public void setElement(T element) {	
		this.element = element;
		if (this.id!=null && element!=null) {
			this.element.setId(this.id);
		}
	}
	
	public boolean isDraft() { return draft; }
	public void setDraft(boolean draft) { 
		this.addChange(ChangeType.EDIT_VALUE, "draft", this.draft, draft);
		this.draft = draft; 
	}
	
	public boolean isReadOnly() { return readOnly; }
	public void setReadOnly(boolean readOnly) {
		this.addChange(ChangeType.EDIT_VALUE, "readOnly", this.readOnly, readOnly);
		this.readOnly = readOnly;
	}
	
	public String getOwnerId() { return ownerId; }
 	public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

	public List<String> getReadIds() { return readIds; }
	public void setReadIds(List<String> readIds) { this.readIds = readIds; }

	public List<String> getWriteIds() { return writeIds; }
	public void setWriteIds(List<String> writeIds) { this.writeIds = writeIds; }

	public List<String> getShareIds() { return shareIds; }
 	public void setShareIds(List<String> shareIds) { this.shareIds = shareIds; }
	
 	@Override
	public List<Change> flush() {
 		if (element!=null) {
 			return element.flush();
 		}
 		return null;
	}
 	
	@Override
	public void addChanges(List<Change> changes) {
		if (element!=null) {
			element.addChanges(changes);
		}
	}
	
	@Override
	public void addChange(Change change) {
		if (element!=null) {
			element.addChange(change);
		}
	}
	
	@Override
	public <TVal> void addChange(ChangeType type, String key, TVal oldValue, TVal newValue) {
		if (element!=null) {
			element.addChange(type, key, oldValue, newValue);
		}
	}
	
	@Override
	public boolean isChanged() {
		return element==null ? false : element.isChanged();
	}	
}