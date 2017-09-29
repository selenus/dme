package de.unibamberg.minf.dme.exception;

public class MappingImportException extends Exception {
	private static final long serialVersionUID = 8129590711314854889L;

	public MappingImportException(String message) {
		super(message);
	}
	
	public MappingImportException(Throwable cause) {
		super(cause);
	}
	
	public MappingImportException(String message, Throwable cause) {
		super(message, cause);
	}
}
