package de.dariah.schereg.util.json;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Hashtable;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import de.dariah.base.model.base.SchemaElement;
import de.dariah.schereg.base.model.Attribute;
import de.dariah.schereg.base.model.Containment;
import de.dariah.schereg.base.model.Domain;
import de.dariah.schereg.base.model.Subtype;

public class SchemaElementJsonConverter {

	private Hashtable<Class<? extends SchemaElement>, ArrayList<SchemaElement>> elementLookupTable;
	
	public SchemaElementJsonConverter(Hashtable<Class<? extends SchemaElement>, ArrayList<SchemaElement>> elementLookupTable) {
		this.elementLookupTable = elementLookupTable;
	}
	
	public JsonArray getAsHierarchicalJson() {
		
		JsonArray result = new JsonArray();
		ArrayList<SchemaElement> rootElements = getRootElements(this.elementLookupTable.get(Containment.class));
		if (rootElements != null) {
			for (SchemaElement root : rootElements) {
				JsonObject obj = new JsonObject();
				obj.addProperty("id", root.getId());
				obj.addProperty("name", root.getName());

				obj.add("children", addElements(root.getId(), getChildElements(root, new HashSet<Integer>())));
				result.add(obj);
			}
		}
		
		return result;
	}
	
	private JsonArray addElements(Integer parentId, ArrayList<SchemaElement> elements) {
		
		if (elements == null || elements.size()==0) {
			return null;
		}
		
		JsonArray result = new JsonArray();
		for (SchemaElement elem : elements) {
			JsonObject obj = new JsonObject();
			obj.addProperty("id", elem.getId());
			obj.addProperty("name", elem.getName());
			obj.add("connections", new JsonArray());
			
			obj.add("children", addElements(elem.getId(), getChildElements(elem)));
			
			result.add(obj);
		}
		
		return result;
	}
	
	private static ArrayList<SchemaElement> getRootElements(ArrayList<SchemaElement> containments)
	{
		ArrayList<SchemaElement> rootElements = new ArrayList<SchemaElement>();

		// Find all containments whose roots are null 
		// and were not created by splitting 
		for(SchemaElement element : containments)
			if(((Containment)element).getParentId() == null){
				
				Containment cont = (Containment)element;
				boolean isSplit = false;
				
				for (SchemaElement se : containments) {
					Containment cont2 = (Containment)se;
					if (cont.getId() != cont2.getId() 
							&& cont.getName().equals(cont2.getName()) 
							&& cont.getDescription().equals(cont2.getDescription())
							&& cont.getChildId().equals(cont2.getChildId())
							&& cont2.getParentId() != null
							/*&& !cont.getBase().equals(cont2.getBase())*/)
					{
						isSplit = true;
					}
				}
				if (isSplit == false)
					rootElements.add(element);
				
			}
		return rootElements;
	}
	
	
	public ArrayList<SchemaElement> getChildElements(SchemaElement element, HashSet<Integer> pathIDs) {

		
		ArrayList<SchemaElement> childElements = new ArrayList<SchemaElement>();

		for(SchemaElement childElement : getChildElements(element))
		{
			// Don't add children if element already in branch
			if(pathIDs.contains(childElement.getId())) continue;

			// Don't add children if type already in branch
			SchemaElement type = getType(childElement.getId());
			if(type!=null)
			{
				boolean duplicatedType = false;
				for(Integer pathID : pathIDs)
					if(type.equals(getType(pathID)))
						{ duplicatedType = true; break; }
				if(duplicatedType) continue;
			}

			// Adds the child element to the list of children elements
			childElements.add(childElement);
		}

		// Retrieve child elements from the hierarchical schema info
		return childElements;
	}
		
	public ArrayList<SchemaElement> getChildElements(SchemaElement element) {
		
		ArrayList<SchemaElement> childElements = new ArrayList<SchemaElement>();
		
		// Find all containments one level lower on the schema
		if(element instanceof Containment)
		{
			Integer childId = ((Containment)element).getChildId();

			if (!(getElement(childId) instanceof Domain)){
			
				SchemaElement e = getElement(childId);
			
				// Build list of all IDs for super-type entities
				ArrayList<Integer> superTypeIDs = getSuperTypes(childId);
					
				// Retrieves all containments whose parent is the child ID
				for (Integer id : superTypeIDs)
					for(Containment containment : getContainments(id))
						if(id.equals(containment.getParentId()))
							childElements.add(containment);
	
				// Retrieves all attributes whose element is the child ID
				for (Integer id : superTypeIDs)
					for(Attribute attribute : getAttributes(id))
						childElements.add(attribute);
			}
		}
			
		return childElements;
	}
	


	/** Identify the super types of the specified element */
	private ArrayList<Integer> getSuperTypes(Integer childID)
	{
		ArrayList<Integer> elementIDs = new ArrayList<Integer>(Arrays.asList(new Integer[]{childID}));
		for(int i=0; i<elementIDs.size(); i++)
		{
			Integer parentID = elementIDs.get(i);
			for(Subtype subtype : getSubTypes(parentID))
				if(!elementIDs.contains(subtype.getParentId()))
					elementIDs.add(subtype.getParentId());
		}
		return elementIDs;
	}
	
	public SchemaElement getType(Integer elementID)
	{
		SchemaElement element = getElement(elementID);
		SchemaElement childElement = null;
		
		if(element instanceof Containment)
			childElement = getElement(((Containment)element).getChildId());
				
		else if (element instanceof Attribute)
			childElement = getElement(((Attribute)element).getDomain().getId());
		
		if (childElement != null && childElement.getName() != null && childElement.getName().length() > 0)
			return childElement;

		return null;	
	}
	
	
	public SchemaElement getElement(Integer id) {
		for (ArrayList<SchemaElement> subList : this.elementLookupTable.values()) {
			for (SchemaElement e : subList) {
				if (id != null && e.getId()==id.intValue()) {
					return e;
				}
			}
		}
		return null;
	}

	private ArrayList<Containment> getContainments(Integer id) {
		ArrayList<Containment> result = new ArrayList<Containment>();
				
		if (this.elementLookupTable.get(Containment.class) != null) {
			for (SchemaElement elem : this.elementLookupTable.get(Containment.class)) {
				Containment c = (Containment)elem;
				if ((c.getParentId() != null && c.getParentId().equals(id)) || 
						(c.getChildId() != null && c.getChildId().equals(id))) {
					result.add(c);
				}
			}
		}
		
		return result;
	}

	private ArrayList<Attribute> getAttributes(Integer id) {
		ArrayList<Attribute> result = new ArrayList<Attribute>();
		
		if (this.elementLookupTable.get(Attribute.class) != null) {
			for (SchemaElement elem : this.elementLookupTable.get(Attribute.class)) {
				Attribute a = (Attribute)elem;
				if (id != null && (a.getEntity().getId()==id.intValue() || a.getDomain().getId()==id.intValue())) {
					result.add(a);
				}
			}
		}
		
		return result;
	}

	private ArrayList<Subtype> getSubTypes(Integer id) {
		ArrayList<Subtype> result = new ArrayList<Subtype>();
		
		
		if (this.elementLookupTable.get(Subtype.class) != null) {
			for (SchemaElement elem : this.elementLookupTable.get(Subtype.class)) {
				Subtype s = (Subtype)elem;
				 if ((s.getParentId() != null && s.getParentId().equals(id)) || 
							(s.getChildId() != null && s.getChildId().equals(id))) {
					result.add(s);
				}
			}
		}
		
		return result;
	}
	
}
