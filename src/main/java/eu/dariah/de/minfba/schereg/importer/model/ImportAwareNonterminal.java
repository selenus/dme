package eu.dariah.de.minfba.schereg.importer.model;

import java.util.List;
import java.util.Map;

import eu.dariah.de.minfba.core.metamodel.NonterminalImpl;

public class ImportAwareNonterminal extends NonterminalImpl {
	private static final long serialVersionUID = 908834748940289151L;
	
	private boolean isabstract; 
	
	private List<ImportAwareNonterminal> abstractChildNonterminals;	
	private Map<String, List<ImportAwareNonterminal>> abstractChildNonterminalExtensionMap;
	private Map<String, List<ImportAwareNonterminal>> childNonterminalExtensionMap;
	
	
	public boolean isAbstract() { return isabstract; }
	public void setAbstract(boolean isabstract) { this.isabstract = isabstract; }
	
	public List<ImportAwareNonterminal> getAbstractChildNonterminals() { return abstractChildNonterminals; }
	public void setAbstractChildNonterminals(List<ImportAwareNonterminal> abstractChildNonterminals) { this.abstractChildNonterminals = abstractChildNonterminals; }
	
	public Map<String, List<ImportAwareNonterminal>> getAbstractChildNonterminalExtensionMap() { return abstractChildNonterminalExtensionMap; }
	public void setAbstractChildNonterminalExtensionMap(Map<String, List<ImportAwareNonterminal>> abstractChildNonterminalExtensionMap) { this.abstractChildNonterminalExtensionMap = abstractChildNonterminalExtensionMap; }
	
	public Map<String, List<ImportAwareNonterminal>> getChildNonterminalExtensionMap() { return childNonterminalExtensionMap; }
	public void setChildNonterminalExtensionMap(Map<String, List<ImportAwareNonterminal>> childNonterminalExtensionMap) { this.childNonterminalExtensionMap = childNonterminalExtensionMap; }
}