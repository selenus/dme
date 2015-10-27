package eu.dariah.de.minfba.schereg.model;

import eu.dariah.de.minfba.core.metamodel.mapping.MappingImpl;

public class MappingWithSchemasImpl extends MappingImpl {
	private static final long serialVersionUID = 6683075539087257860L;
	
	private String sourceLabel;
	private String targetLabel;
	
	public String getSourceLabel() { return sourceLabel; }
	public void setSourceLabel(String sourceLabel) { this.sourceLabel = sourceLabel; }
	
	public String getTargetLabel() { return targetLabel; }
	public void setTargetLabel(String targetLabel) { this.targetLabel = targetLabel; }
}