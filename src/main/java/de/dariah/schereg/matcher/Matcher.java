package de.dariah.schereg.matcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;

import de.dariah.base.model.base.SchemaElement;
import de.dariah.schereg.base.model.Schema;
import de.dariah.schereg.util.SchemaElementContainer;


public abstract class Matcher
{
	private Schema sourceSchema;
	private Schema targetSchema;

	private SchemaElementContainer sourceElementContainer;
	private SchemaElementContainer targetElementContainer;
	
	public Schema getSourceSchema() { return sourceSchema; }
	public void setSourceSchema(Schema sourceSchema) { this.sourceSchema = sourceSchema; }

	public Schema getTargetSchema() { return targetSchema; }
	public void setTargetSchema(Schema targetSchema) { this.targetSchema = targetSchema; }
	
	public SchemaElementContainer getSourceElementContainer() {
		return sourceElementContainer;
	}
	public void setSourceElementContainer(SchemaElementContainer sourceElementContainer) {
		this.sourceElementContainer = sourceElementContainer;
	}
	public SchemaElementContainer getTargetElementContainer() {
		return targetElementContainer;
	}
	public void setTargetElementContainer(SchemaElementContainer targetElementContainer) {
		this.targetElementContainer = targetElementContainer;
	}
	
	
	//private MatchTypeMappings types;

	//private HashMap<String,String> defaults = new HashMap<String,String>();
	
	//private boolean isDefaultMatcher = false;

	//private boolean isHiddenMatcher = false;
	
	// Stores the completed and total number of comparisons that need to be performed
	protected int completedComparisons = 0, totalComparisons = 1;
	
	public boolean isMappingAllowedForTypes(Class<? extends SchemaElement> sourceType, Class<? extends SchemaElement> targetTypeHash) {
		return true;
	}
	


	abstract public String getName();

	//public boolean needsClient() { return false; }
	
	//protected ArrayList<MatcherParameter> getMatcherParameters() { return new ArrayList<MatcherParameter>(); }
	
	/*
	// Matcher getters
	final public boolean isDefault() { return isDefaultMatcher; }
	final public boolean isHidden() { return isHiddenMatcher; }

	// Matcher setters
	final public void setDefault(boolean isDefault) { this.isDefaultMatcher = isDefault; }
	final public void setHidden(boolean isHidden) { this.isHiddenMatcher = isHidden; }
	 */
	
	
	
	/** Initializes the matcher */
	/*final public void initialize(FilteredSchemaInfo schema1, FilteredSchemaInfo schema2)
		{ this.schema1 = schema1; this.schema2 = schema2; this.types = null; }


	final public void initialize(FilteredSchemaInfo schema1, FilteredSchemaInfo schema2, MatchTypeMappings types)
		{ this.schema1 = schema1; this.schema2 = schema2; this.types = types; }

	 */
	
	
	/** Sets the parameter default */
	/*
	final public void setDefault(String name, String value)
		{ defaults.put(name,value); }*/

	/** Retrieve the matcher parameters */
	/*final public ArrayList<MatcherParameter> getParameters()
	{
		ArrayList<MatcherParameter> parameters = new ArrayList<MatcherParameter>();
		for(MatcherParameter parameter : getMatcherParameters())
		{
			String value = defaults.get(parameter.getName());
			if(value!=null) parameter.setValue(value);
			parameters.add(parameter);
		}
		return parameters;
	}*/
	
	/** Generates scores for the specified graphs */
	abstract public MatchResult match();

	/** Indicates if the specified elements can validly be mapped together */
	/*final protected boolean isAllowableMatch(SchemaElement element1, SchemaElement element2)
		{ return types == null || types.isMapped(element1, element2); }*/

	/** Indicates the completion percentage of the matcher */
	final public double getPercentComplete()
		{ return 1.0 * completedComparisons / totalComparisons; }
}