package eu.dariah.de.minfba.schereg.importer.model;

import java.util.List;

import eu.dariah.de.minfba.core.metamodel.NonterminalImpl;

public class ImportAwareNonterminal extends NonterminalImpl {
	private static final long serialVersionUID = 908834748940289151L;
	
	private int parentCount;
	private boolean isabstract;
	private String terminalQN;
	private List<ImportAwareNonterminal> extensions;
	
	
	public int getParentCount() { return parentCount; }
	public void setParentCount(int parentCount) { this.parentCount = parentCount; }
	
	public boolean isAbstract() { return isabstract; }
	public void setAbstract(boolean isabstract) { this.isabstract = isabstract; }
	
	public String getTerminalQN() { return terminalQN; }
	public void setTerminalQN(String terminalQN) { this.terminalQN = terminalQN; }
	
	public List<ImportAwareNonterminal> getExtensions() { return extensions; }
	public void setExtensions(List<ImportAwareNonterminal> extensions) { this.extensions = extensions; }
}