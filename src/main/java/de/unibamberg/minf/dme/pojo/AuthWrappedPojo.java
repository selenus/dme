package de.unibamberg.minf.dme.pojo;

import de.unibamberg.minf.dme.model.base.Identifiable;

public class AuthWrappedPojo<T extends Identifiable> implements Identifiable {
	private static final long serialVersionUID = -9121158238035864927L;

	private T pojo;
	
	private boolean own;
	private boolean draft;
	private boolean readOnly;
	private boolean write;
	private boolean share;
	
	
	public AuthWrappedPojo() {}
	public AuthWrappedPojo(T pojo, boolean own, boolean write, boolean share, boolean draft, boolean readOnly) {
		this.pojo = pojo;
		this.own = own;
		this.write = write;
		this.readOnly = readOnly;
		this.share = share;
		this.draft = draft;
	}
		
	public boolean isWrite() { return write; }
	public void setWrite(boolean write) { this.write = write; }
	
	public boolean isShare() { return share; }
	public void setShare(boolean share) { this.share = share; }
	
	public T getPojo() { return pojo; }
	public void setPojo(T pojo) { this.pojo = pojo; }
	
	public boolean isOwn() { return own; }
	public void setOwn(boolean own) { this.own = own; }
	
	public boolean isDraft() { return draft; }
	public void setDraft(boolean draft) { this.draft = draft; }
	
	public boolean isReadOnly() { return readOnly; }
	public void setReadOnly(boolean readOnly) { this.readOnly = readOnly; }
	
	@Override public String getId() { return this.pojo.getId(); }
	@Override public void setId(String id) { this.pojo.setId(id); }
}