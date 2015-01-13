package de.dariah.schereg.util.json;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

import de.dariah.base.model.base.ConfigurableSchemaElementImpl;
import de.dariah.base.model.base.SchemaElement;
import de.dariah.federation.model.SchemaElementPojo;
import de.dariah.federation.util.Constants;
import de.dariah.schereg.base.model.Attribute;
import de.dariah.schereg.base.model.Containment;
import de.dariah.schereg.base.model.Domain;
import de.dariah.schereg.base.model.Subtype;

public class SchemaElementConverter {
	
	private Hashtable<Class<? extends SchemaElement>, ArrayList<SchemaElement>> elementLookupTable;
	
	public SchemaElementConverter(Hashtable<Class<? extends SchemaElement>, ArrayList<SchemaElement>> elementLookupTable) {
		this.elementLookupTable = elementLookupTable;
	}
	
	public List<SchemaElementPojo> getTree() {
		
		List<SchemaElement> rootElements = getRootElements(this.elementLookupTable.get(Containment.class));
		List<SchemaElementPojo> result = new ArrayList<SchemaElementPojo>();
		
		if (rootElements != null) {
			for (SchemaElement root : rootElements) {
				
				SchemaElementPojo pojo = new SchemaElementPojo();
				pojo.setId(root.getId());
				pojo.setName(root.getName());
				pojo.setChildren(addElements(root.getId(), getChildElements(root, new HashSet<Integer>())));
				
				if (root instanceof Attribute || root instanceof Containment) {
					pojo.setNsPrefix(((ConfigurableSchemaElementImpl)root).getNsUri());
					pojo.setElementProperties(getElementProperties((ConfigurableSchemaElementImpl)root));
				} 				
				
				result.add(pojo);
			}
		}
		return result;
	}


	
	private List<SchemaElementPojo> addElements(Integer parentId, ArrayList<SchemaElement> elements) {
		
		if (elements == null || elements.size()==0) {
			return null;
		}
		
		List<SchemaElementPojo> result = new ArrayList<SchemaElementPojo>();
		for (SchemaElement elem : elements) {
			SchemaElementPojo pojo = new SchemaElementPojo();
			pojo.setId(elem.getId());
			pojo.setName(elem.getName());
			pojo.setChildren(addElements(elem.getId(), getChildElements(elem)));
			
			if (elem instanceof Attribute || elem instanceof Containment) {
				pojo.setNsPrefix(((ConfigurableSchemaElementImpl)elem).getNsUri());
				pojo.setElementProperties(getElementProperties((ConfigurableSchemaElementImpl)elem));
				
				if (elem instanceof Attribute) {
					pojo.setAttribute(true);
				}
			} 
			
			
			result.add(pojo);
		}
		
		return result;
	}
	
	private HashMap<String, Object> getElementProperties(ConfigurableSchemaElementImpl elem) {
		HashMap<String, Object> result = new HashMap<String, Object>();
		
		if (elem.isProcessGeoData()) {
			result.put(Constants.PROCESSING_FLAG_PROCESS_GEO, true);
		}
		if (elem.isProcessSourceLinks()) {
			result.put(Constants.PROCESSING_FLAG_PROCESS_SOURCE_LINKS, true);
		}
		if (elem.getAnalyzers()!=null && elem.getAnalyzers().size()>0) {
			result.put(Constants.PROCESSING_FLAG_ANALYZERS, elem.getAnalyzers());
		}
		if (elem.isUseForTitle()) {
			result.put(Constants.PROCESSING_FLAG_USE_TITLE, true);
		}
		if (elem.isUseForTopicModelling()) {
			result.put(Constants.PROCESSING_FLAG_USE_TOPIC, true);
		}
		
		return result;
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
