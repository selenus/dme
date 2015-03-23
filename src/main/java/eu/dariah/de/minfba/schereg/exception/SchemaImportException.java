package eu.dariah.de.minfba.schereg.exception;

public class SchemaImportException extends Exception {
	private static final long serialVersionUID = 931911851708936456L;

	public SchemaImportException(String message) {
		super(message);
	}
	
	public SchemaImportException(Throwable cause) {
		super(cause);
	}
	
	public SchemaImportException(String message, Throwable cause) {
		super(message, cause);
	}
}
