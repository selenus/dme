package eu.dariah.de.minfba.schereg.model;

public class SessionSampleFile {
	public enum FileTypes { XML, JSON, ZIP }
	
	private String path;
	private int fileCount;
	private FileTypes type;
	
	public String getPath() { return path; }
	public void setPath(String path) { this.path = path; }
	
	public int getFileCount() { return fileCount; }
	public void setFileCount(int fileCount) { this.fileCount = fileCount; }
	
	public FileTypes getType() { return type; }
	public void setType(FileTypes type) { this.type = type; }
}
