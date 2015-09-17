package eu.dariah.de.minfba.schereg.pojo;

import eu.dariah.de.minfba.core.metamodel.interfaces.Identifiable;

public class AuthWrappedPojo<T extends Identifiable> implements Identifiable {
	private static final long serialVersionUID = 8187017367480384695L;

	private T pojo;
	
	private boolean read;
	private boolean write;
	private boolean share;
	
	
	public AuthWrappedPojo() {}
	public AuthWrappedPojo(T pojo, boolean read, boolean write, boolean share) {
		this.pojo = pojo;
		this.read = read;
		this.write = write;
		this.share = share;
	}
	
	public T getElement() { return pojo; }
	public void setElement(T element) { this.pojo = element; }
	
	public boolean isRead() { return read; }
	public void setRead(boolean read) { this.read = read; }
	
	public boolean isWrite() { return write; }
	public void setWrite(boolean write) { this.write = write; }
	
	public boolean isShare() { return share; }
	public void setShare(boolean share) { this.share = share; }
	
	
	@Override
	public String getId() {
		return pojo.getId();
	}
	@Override
	public void setId(String id) {
		pojo.setId(id);
	}
}