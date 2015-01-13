package de.dariah.schereg.base.model;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import de.dariah.base.model.base.BaseEntityImpl;

@javax.persistence.Entity
@Table(name="files")
public class File extends BaseEntityImpl {

	@Id
	@TableGenerator(name = "files_gen", table="universal_id", valueColumnName="id", allocationSize=1, pkColumnName="sequence_name", pkColumnValue="files")
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "files_gen")
	private int id;
	
	@Column(name="filename")
	private String filename;
	
	@Column(name="filestream")
	private byte[] filestrem; 
		
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "filetype", nullable = false)
	private FileType filetype;
	
	@Column(name="validated")
	private boolean isValidated;
		
	@Override 
	public int getId() { return id; }
	@Override 
	public void setId(int id) { this.id = id; }
	
	public byte[] getFilestrem() { return filestrem; }
	public void setFilestrem(byte[] filestrem) { this.filestrem = filestrem; }
	
	public FileType getFiletype() { return filetype; }
	public void setFiletype(FileType filetype) { this.filetype = filetype; }
	
	public boolean isValidated() { return isValidated; }
	public void setValidated(boolean isValidated) { this.isValidated = isValidated; }
	
	public String getFilename() { return filename; }
	public void setFilename(String filename) { this.filename = filename; }
}
