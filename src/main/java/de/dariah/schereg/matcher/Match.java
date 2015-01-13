package de.dariah.schereg.matcher;

public class Match
{
	/** Stores the positive evidence */
	private Double positiveEvidence;

	/** Stores the total evidence */
	private Double totalEvidence;
	
	/** Constructs the matcher score */
	public Match(Double score)
		{ this.positiveEvidence = score; this.totalEvidence = 1.0; }	
	
	/** Constructs the matcher score */
	public Match(Double positiveEvidence, Double totalEvidence)
		{ this.positiveEvidence = positiveEvidence; this.totalEvidence = totalEvidence; }
	
	/** Returns the positive evidence */
	public Double getPositiveEvidence()
		{ return positiveEvidence; }

	/** Returns the total evidence */
	public Double getTotalEvidence()
		{ return totalEvidence; }
}