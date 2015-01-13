package de.dariah.schereg.matcher.algorithms;

import java.util.ArrayList;

import de.dariah.base.model.base.BaseEntity;
import de.dariah.base.model.base.SchemaElement;
import de.dariah.schereg.base.model.Alias;
import de.dariah.schereg.base.model.Containment;
import de.dariah.schereg.base.model.Schema;
import de.dariah.schereg.matcher.Matcher;
import de.dariah.schereg.util.SchemaElementContainer;

public class ExactLabelsMatcher // extends Matcher
{	
//	class MatcherCheckboxParameter
//	{
//		private String name;
//		private String text;
//		private String value;
//		
//		public String getName() { return name; }
//		public String getText() { return text; }
//		public String getValue() { return value; }
//		public void setValue(String value) { this.value = value; }
//		
//		/** Constructs the matcher checkbox parameter */
//		public MatcherCheckboxParameter(String name, Boolean value) { 
//			this.name=name; this.value=value.toString();
//			this.text=name.replaceAll("([a-z])([A-Z])","$1 $2");
//		}
//		
//		/** Constructs the matcher checkbox parameter */
//		public MatcherCheckboxParameter(String name, String text, Boolean value) { 
//			this.name=name; this.text=text; this.value=value.toString(); 
//		}
//		
//		/** Indicates if the parameter is selected */
//		public Boolean isSelected()
//			{ return getValue().equals("true"); }
//
//		/** Marks the parameter as selected */
//		public void setSelected(Boolean selected)
//			{ setValue(selected.toString()); }
//	}
//	
//	// Stores the matcher parameters
//	private MatcherCheckboxParameter name = new MatcherCheckboxParameter("UseName",true);
//	private MatcherCheckboxParameter description = new MatcherCheckboxParameter("UseDescription",false);
//	private MatcherCheckboxParameter hierarchy = new MatcherCheckboxParameter("UseHierarchy",true);
//	
//	public String getName() { return "Exact Matcher"; }
//
//	/** Returns the list of parameters associated with the bag matcher */
//	public ArrayList<MatcherCheckboxParameter> getMatcherParameters()
//	{
//		ArrayList<MatcherCheckboxParameter> parameters = new ArrayList<MatcherCheckboxParameter>();
//		parameters.add(name);
//		parameters.add(description);
//		parameters.add(hierarchy);
//		return parameters;
//	}
//
//	/** Returns the element name and/or description */
//	private String getName(Schema schema, Integer elementID)
//	{
//		StringBuffer value = new StringBuffer();
//
//		// Retrieve name if the "name" option is set
//		if(name.isSelected())
//		{
//			String name;
//			
//			// Get the name, trimming the edges and collapsing spaces to be one space long
//			if (getSourceElementContainer().getIdLookupTable().containsKey(elementID)) {
//				name = getDisplayName(getSourceElementContainer().getIdLookupTable().get(elementID)) + " -> ";
//			} else {
//				name = getDisplayName(getTargetElementContainer().getIdLookupTable().get(elementID)) + " -> ";
//			}
//
//			name = name.replaceAll("\\b\\s{2,}\\b", " ").trim();
//			value.append(name);
//		}
//
//		// Retrieve description if the "description" option is set
//		if(description.isSelected())
//		{
//			// Get the description, trimming the edges and collapsing spaces to be one space long
//			String description;
//			if (getSourceElementContainer().getIdLookupTable().containsKey(elementID)) {
//				description = getSourceElementContainer().getIdLookupTable().get(elementID).getDescription();
//			} else {
//				description = getTargetElementContainer().getIdLookupTable().get(elementID).getDescription();
//			}
//			description = description.replaceAll("\\b\\s{2,}\\b", " ").trim();
//			if(description.length() > 0) { value.append(description); }
//		}
//		
//		return value.toString();
//	}
//	
//	public String getDisplayName(SchemaElement element)
//	{
//		SchemaElementContainer container;
//		
//		if (getSourceElementContainer().getIdLookupTable().containsKey(element.getId())) {
//			container = getSourceElementContainer();
//		} else {
//			container = getTargetElementContainer();
//		}
//		
//		Alias alias = null;
//		if (container.getClassLookupTable().contains(Alias.class) && container.getClassLookupTable().get(Alias.class).size() > 0) {
//			for (SchemaElement e : container.getClassLookupTable().get(Alias.class)) {
//				Alias a = (Alias)e;
//				if (a.getElement().getId() == element.getId()) {
//					alias = a;
//				}
//			}
//		}
//		
//		String name = alias!=null ? alias.getName() : element.getName();
//		if(name.length()>0) return name;
//
//		// Otherwise, returns the name of the parent containment element
//		if(element instanceof Containment)
//		{
//			BaseEntity child = ((Containment)element).getChild();
//			return "[" + getDisplayName((SchemaElement)child) + "]";
//		}
//
//		// Otherwise, find name of containment associated with element
//		for(Containment containment : getContainments(elementID))
//			if(containment.getChildID().equals(elementID) && containment.getName().length()>0)
//				return "[" + getDisplayName(containment.getId()) + "]";
//
//		// Otherwise, return nothing
//		return "";
//	}
//	
//	/** Generate scores for the exact matches */
//	private MatcherScores getExactMatches()
//	{
//		// Get the source and target elements
//		ArrayList<SchemaElement> sourceElements = schema1.getFilteredElements();
//		ArrayList<SchemaElement> targetElements = schema2.getFilteredElements();
//
//		// Sets the current and total comparisons
//		completedComparisons = 0;
//		totalComparisons = sourceElements.size() + targetElements.size();
//		
//		// Generate a hash of all target elements
//		HashMap<String,ArrayList<Integer>> targetMap = new HashMap<String,ArrayList<Integer>>();
//		for(SchemaElement element : schema2.getFilteredElements())
//		{
//			String key = getName(schema2, element.getId());
//			if(key.length()==0) continue;
//			ArrayList<Integer> targetIDs = targetMap.get(key);
//			if(targetIDs == null)
//				targetMap.put(key, targetIDs = new ArrayList<Integer>());
//			targetIDs.add(element.getId());
//			completedComparisons++;
//		}
//		
//		// Find all exact matches
//		MatcherScores scores = new MatcherScores(100.0);
//		for(SchemaElement sourceElement : sourceElements)
//		{
//			String key = getName(schema1,sourceElement.getId());
//			ArrayList<Integer> targetIDs = targetMap.get(key);
//			if(targetIDs != null)
//				for(Integer targetID : targetIDs)
//					scores.setScore(sourceElement.getId(), targetID, new MatcherScore(100.0,100.0));
//			completedComparisons++;
//		}
//		return scores;
//	}
//
//	/** Generate scores for the exact structure matches */
//	private MatcherScores getExactHierarchicalMatches()
//	{
//		// Get the source and target elements
//		ArrayList<SchemaElement> sourceElements = schema1.getFilteredElements();
//
//		// Sets the current and total comparisons
//		completedComparisons = 0;
//		totalComparisons = sourceElements.size();
//
//		// Search for matching hierarchical matches
//		MatcherScores scores = new MatcherScores(100.0);
//		for(SchemaElement sourceElement : sourceElements)
//		{
//			// Retrieve all matching target elements
//			HashSet<Integer> targetIDs = new HashSet<Integer>();
//			for(ArrayList<SchemaElement> sourcePath : schema1.getPaths(sourceElement.getId()))
//			{
//				// Retrieve the source path
//				ArrayList<String> path = new ArrayList<String>();
//				for(SchemaElement element : sourcePath)
//					path.add(schema1.getDisplayName(element.getId()));
//
//				// Identify all target paths
//				for(Integer targetID : schema2.getPathIDs(path))
//					if(schema2.isVisible(targetID))
//						targetIDs.add(targetID);
//			}
//
//			// Set scores for the matching target elements
//			for(Integer targetID : targetIDs)
//			{
//				String name1 = getName(schema1, sourceElement.getId());
//				String name2 = getName(schema2, targetID);
//				if(name1.length()>0 && name1.equals(name2))
//					scores.setScore(sourceElement.getId(), targetID, new MatcherScore(100.0,100.0));
//			}
//
//			// Update the completed comparison count
//			completedComparisons++;
//		}
//		return scores;
//	}
//	
//	/** Generates scores for the specified elements */
//	public MatcherScores match() {
//		// Don't proceed if neither "name" nor "description" option selected
//		if (!name.isSelected() && !description.isSelected()) {
//			return new MatcherScores(100.0);
//		}
//
//		// Generate the matches
//		if (hierarchy.isSelected()) {
//			return getExactHierarchicalMatches();
//		} else {
//			return getExactMatches();
//		}
//	}
}
