package eu.dariah.de.minfba.schereg.model;

import java.util.List;

import eu.dariah.de.minfba.core.metamodel.interfaces.Identifiable;

public class RightsContainer<T extends Identifiable> implements Identifiable {
	private static final long serialVersionUID = -1272567909444423065L;
	
	private String id;
	private T element;
	
	private boolean draft;
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
	public void setDraft(boolean draft) { this.draft = draft; }

	public String getOwnerId() { return ownerId; }
 	public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

	public List<String> getReadIds() { return readIds; }
	public void setReadIds(List<String> readIds) { this.readIds = readIds; }

	public List<String> getWriteIds() { return writeIds; }
	public void setWriteIds(List<String> writeIds) { this.writeIds = writeIds; }

	public List<String> getShareIds() { return shareIds; }
 	public void setShareIds(List<String> shareIds) { this.shareIds = shareIds; }
}
