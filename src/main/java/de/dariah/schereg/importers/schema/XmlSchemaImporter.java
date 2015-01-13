package de.dariah.schereg.importers.schema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;

import javax.xml.stream.events.Namespace;

import org.exolab.castor.xml.Namespaces;
import org.exolab.castor.xml.schema.Annotated;
import org.exolab.castor.xml.schema.Annotation;
import org.exolab.castor.xml.schema.AnyType;
import org.exolab.castor.xml.schema.AttributeDecl;
import org.exolab.castor.xml.schema.AttributeGroupReference;
import org.exolab.castor.xml.schema.ComplexType;
import org.exolab.castor.xml.schema.Documentation;
import org.exolab.castor.xml.schema.ElementDecl;
import org.exolab.castor.xml.schema.Facet;
import org.exolab.castor.xml.schema.Form;
import org.exolab.castor.xml.schema.Group;
import org.exolab.castor.xml.schema.Schema;
import org.exolab.castor.xml.schema.SimpleType;
import org.exolab.castor.xml.schema.Union;
import org.exolab.castor.xml.schema.Wildcard;
import org.exolab.castor.xml.schema.XMLType;
import org.exolab.castor.xml.schema.reader.SchemaReader;
import org.springframework.security.core.context.SecurityContext;
import org.xml.sax.InputSource;

import de.dariah.base.model.base.NamedEntity;
import de.dariah.base.model.base.SchemaElement;
import de.dariah.schereg.base.model.Attribute;
import de.dariah.schereg.base.model.Containment;
import de.dariah.schereg.base.model.Domain;
import de.dariah.schereg.base.model.DomainValue;
import de.dariah.schereg.base.model.Entity;
import de.dariah.schereg.base.model.Relationship;
import de.dariah.schereg.base.model.Subtype;

public class XmlSchemaImporter extends BaseImportThread {
		
	private Entity anyEntity;
	
	private HashMap<Integer, SchemaElement> schemaElementMap;
	private HashMap<String,Domain> domainMap;
	private HashSet<Integer> attributesInAttributeGroups;
	private HashMap<String,Entity> attributeGroupEntities;
	
	public XmlSchemaImporter(int schemaId, InputSource importSource, SecurityContext securityContext) {
		super(schemaId, importSource, securityContext);
		
		schemaElementMap = new HashMap<Integer, SchemaElement>();
		domainMap = new HashMap<String, Domain>();
		attributeGroupEntities = new HashMap<String,Entity>();
		attributesInAttributeGroups = new HashSet<Integer>();
		nsMap = new HashMap<String, String>();
	}
	
	@Override
	protected Collection<SchemaElement> parseExternalSchema(InputSource importSource) throws Exception {
		//try {						
			for (Domain globalDomain : getGlobalDomains()) {
				domainMap.put(globalDomain.getName().trim().toUpperCase(), globalDomain);
			}
			
			SchemaReader xmlSchemaReader = new SchemaReader(importSource);

			// TODO: This fixes external pointers (redirect) problem for now; Problem?
			xmlSchemaReader.setValidation(false);

			
			
			Schema mainSchema = xmlSchemaReader.read();
			

					
			Namespaces ns = mainSchema.getNamespaces();
			Enumeration nsPrefixEnum = ns.getLocalNamespacePrefixes();
			
			while (nsPrefixEnum.hasMoreElements()) {
				String prefix = nsPrefixEnum.nextElement().toString();
				String nsUri = ns.getNamespaceURI(prefix);
				
				if (prefix.equals("\"\"") || prefix.equals("''")) {
					prefix = "";
				}
				
				nsMap.put(nsUri, prefix);
			}
			

			
			getRootElements(mainSchema);
			 
			return schemaElementMap.values();
		/*}
		catch(Exception e) { 			
			e.printStackTrace();
			throw new Exception(e.getMessage()); 
		}*/
	}
	

	/*************************************************************************
	 * Rest of XSDImporter 
	 * ***********************************************************************
	 */
	
	/** Returns the importer name */
	public String getDisplayName()
		{ return "XSD Importer"; }
	
	/** Returns the importer description */
	public String getDescription()
		{ return "This importer can be used to import schemas from an xsd format"; }
	
	/** Returns the importer URI file types */
	public ArrayList<String> getFileTypes()
	{
		ArrayList<String> fileTypes = new ArrayList<String>();
		fileTypes.add(".xsd");
		return fileTypes;
	}

	/** Returns the schema elements from the specified URI */
	public ArrayList<SchemaElement> generateSchemaElements() throws Exception
		{ return new ArrayList<SchemaElement>(schemaElementMap.values()); }


	/**
	 * getRootElements:  Processes the SimpleTypes, ComplexTypes, and Elements 
	 * defined at the "root" (Schema) level.
	 * 
	 * @param xmlSchema schema to be processed
	 */
	public void getRootElements(Schema xmlSchema) {
		
		// TODO: this has been modified to work with newer version of Schema, might require testing
		
		Collection<SimpleType> simpleTypes = xmlSchema.getSimpleTypes();
		for(SimpleType simpleType : simpleTypes) {
			processSimpleType(simpleType, null);
		}
		
		Collection<ComplexType> complexTypes = xmlSchema.getComplexTypes();
		for(ComplexType complexType : complexTypes) {
			processComplexType(complexType, null);
		}
		
		Collection<ElementDecl> elements = xmlSchema.getElementDecls();
		for(ElementDecl element : elements) {
			processElement(element, null);
		}
		
//		// Each root SimpleType should be translated into a Domain
//		Enumeration<?> simpleTypes = xmlSchema.getSimpleTypes();
//		while (simpleTypes.hasMoreElements())
//			processSimpleType((SimpleType) simpleTypes.nextElement(), null);
//		
//		// Each root ComplexType should be translated into an Entity
//		Enumeration<?> complexTypes = xmlSchema.getComplexTypes();
//		while (complexTypes.hasMoreElements())
//			processComplexType((ComplexType) complexTypes.nextElement(), null);
//		
//		// Each root Element should be translated into a Containment (with schema as parent)
//		Enumeration<?> elements = xmlSchema.getElementDecls();
//		while (elements.hasMoreElements()) 
//			processElement((ElementDecl) elements.nextElement(), null);
	}
	
	/**
	 * processSimpleType: creates M3 Domain for the passed SimpleType 
	 * (or finds references to existing Domain if type seen before)
	 * and adds this domain as child of passed Containment or Attribute
	 * 
	 * @param passedType XML SimpleType which needs to either be processed 
	 * or referenced if already seen
	 * @param parent M3 Containment or Attribute to which domain for passed 
	 * simpleType should be added as child
	 */
	public void processSimpleType (XMLType passedType, NamedEntity parent)
	{		
		// assign the default type of String
		String typeName = "StringDef" + " ";
		if ((passedType != null) && (passedType.getName() != null) && (passedType.getName().length() > 0)) 
			typeName = passedType.getName() + " ";
		
		// handle "Any" type
		if (passedType != null && passedType instanceof AnyType)
			typeName = "Any" + " ";
		
		// handle IDREF / IDREFS -- generate relationship to "Any" entity
		if (parent instanceof Attribute && (typeName.equals("IDREF") || typeName.equals("IDREFS"))){
		
			if (this.anyEntity == null) {
				//this.anyEntity = new Entity(nextAutoInc(),"ANY","ANY ENTITY", null);
				this.anyEntity = new Entity();
				this.anyEntity.setName("ANY");
				this.anyEntity.setDescription("ANY Entity");
			}
			schemaElementMap.put(this.anyEntity.hashCode(),this.anyEntity);
			
			Integer rightMax = ( typeName.equals("IDREFS") ) ? null : 1;   			
			//Relationship rel = new Relationship(nextAutoInc(), parent.getName(), "", ((Attribute)parent).getEntity(), 0, 1, this.anyEntity, 0, rightMax, null);
			Relationship rel = new Relationship();
			rel.setName(parent.getName());
						
			rel.setLeft(((Attribute)parent).getEntity());
			rel.setLeftMin(0);
			rel.setLeftMax(1);
			rel.setRight(this.anyEntity);
			rel.setRightMin(0);
			rel.setRightMax(rightMax);
			
			schemaElementMap.put(rel.hashCode(),rel);
			
			/** remove the attribute if type ANY is involved **/
			schemaElementMap.remove(parent.getId());
			
		}
		else {
	
			// find Domain for SimpleType (generated if required)
			//Domain domain = new Domain(nextAutoInc(), typeName, (passedType == null ? "" : this.getDocumentation(passedType)), null);
			Domain domain = new Domain();
			domain.setName(typeName.trim());
			domain.setDescription((passedType == null ? "" : this.getDocumentation(passedType)));
			
			if (domainMap.containsKey(domain.getName()) == false) {
				domainMap.put(domain.getName(),domain);
				schemaElementMap.put(domain.hashCode(), domain);
				
				if (passedType != null && passedType instanceof SimpleType && !(passedType instanceof Union)){
					// create DomainValues (if specified for SimpleType)
					Enumeration<?> facets = ((SimpleType)passedType).getFacets("enumeration");
					while (facets.hasMoreElements()) {
						Facet facet = (Facet) facets.nextElement();
						//DomainValue domainValue = new DomainValue(nextAutoInc(), facet.getValue(), this.getDocumentation(facet), domain, null);
						DomainValue domainValue = new DomainValue();
						domainValue.setValue(facet.getValue());
						domainValue.setDescription(this.getDocumentation(facet));
						domainValue.setDomain(domain);
						
						schemaElementMap.put(domainValue.hashCode(), domainValue);
					}
				}
				
				// TODO: process Union Types
				else if (passedType != null && passedType instanceof Union){
					Union passedUnion = (Union)passedType;
					Enumeration<?> memberTypes = passedUnion.getMemberTypes();
					while (memberTypes.hasMoreElements()){
						SimpleType childType = (SimpleType)memberTypes.nextElement();
						
						// create a subtype to capture union
						//Subtype subtype = new Subtype(nextAutoInc(), domain, null, null);
						Subtype subtype = new Subtype();
						subtype.setParent(domain);
						
						schemaElementMap.put(subtype.hashCode(), subtype);
						processSimpleType(childType,subtype);
					}
				}
			} else {
				Domain existingDomain = domainMap.get(domain.getName());
				if (!schemaElementMap.containsKey(existingDomain.hashCode())) {
					schemaElementMap.put(existingDomain.hashCode(), existingDomain);
				}
			}
	 
			// attached Domain as child to passed Attribute / Containment / Subtype
			domain = domainMap.get(domain.getName()); 
			if (parent instanceof Attribute)
				((Attribute)parent).setDomain(domain);
			else if (parent instanceof Containment)
				((Containment)parent).setChild(domain);
			else if (parent instanceof Subtype)
				((Subtype)parent).setChild(domain);
		}
	} // end method processSimpleType


	
	
	/**
	 * processComplexType: creates M3 Entity for the passed ComplexType 
	 * (or finds references to existing Entity if type seen before)
	 * and adds this entity as child of passed Containment or Subtype
	 * 
	 * NOTE:  This method can support handling Attributes which are 
	 * simpleContent by creating an additional simpleContent attribute.
	 * The necessary code for handling simpleContent is currently commented
	 * out.
	 * 
	 * @param passedType XML ComplexType which needs to either be processed 
	 * or referenced if already seen
	 * 
	 * @param parent M3 Containment or Subtype to which entity for passed 
	 * complexType should be added as child
	 */
	public void processComplexType (ComplexType passedType, SchemaElement parent)
	{
		
		// check to see if entity has been created for passed complex type
		// create new Entity if none has been created 
		//Entity entity = new Entity(nextAutoInc(), passedType.getName(), this.getDocumentation(passedType), null);
		Entity entity = new Entity();
		entity.setName(passedType.getName() == null ? "" : passedType.getName());
		entity.setDescription(this.getDocumentation(passedType));
		
		if (schemaElementMap.containsKey(passedType.hashCode()) == false) 
		{
			schemaElementMap.put(passedType.hashCode(), entity);
				
			try 
			{
				// get Attributes for current complexType
				Enumeration<?> attrGroupReferences = passedType.getAttributeGroupReferences();
				
				while (attrGroupReferences.hasMoreElements())
				{					
					AttributeGroupReference attrGroupRef = (AttributeGroupReference)attrGroupReferences.nextElement();
					//Entity attrGroupEntity = new Entity(nextAutoInc(),attrGroupRef.getReference(),"attr group",null);
					Entity attrGroupEntity = new Entity();
					attrGroupEntity.setName(attrGroupRef.getReference() == null ? "" : attrGroupRef.getReference());
					attrGroupEntity.setDescription("attr group");
					
					if (attributeGroupEntities.containsKey(attrGroupEntity.getName()) == false)
					{
						attributeGroupEntities.put(attrGroupEntity.getName(), attrGroupEntity);
						schemaElementMap.put(attrGroupEntity.hashCode(), attrGroupEntity);
						
						Enumeration<?> attrs = attrGroupRef.getAttributes();
					
					//	boolean sawSimpleContentVal = false;
						
						while (attrs.hasMoreElements()){
						
							AttributeDecl attrDecl = (AttributeDecl)attrs.nextElement();
							
							attributesInAttributeGroups.add(attrDecl.hashCode());
							try {
								while (attrDecl != null && attrDecl.isReference() == true && attrDecl.getReference() != null)
								attrDecl = attrDecl.getReference();
							} catch(IllegalStateException e){} // handle malformed XSDs that do not have parent set (depreciated attrs as parents)
						
							boolean containsID = attrDecl.getSimpleType() != null && attrDecl.getSimpleType().getName() != null && attrDecl.getSimpleType().getName().equals("ID");
						
							// TODO This was id based
							//Integer attrID = nextAutoInc();
							//Attribute attr = new Attribute(attrID,(attrDecl.getName() == null ? "" : attrDecl.getName()),getDocumentation(attrDecl),attrGroupEntity,null,(attrDecl.isRequired()? 1 : 0), 1, containsID, null); 
					//		if (attr.getName().equalsIgnoreCase("simpleContentValue")){
					//			sawSimpleContentVal = true;
					//		}
							//_schemaElementsHS.put(attrID, attr);
							
							Attribute attr = new Attribute();
							attr.setName((attrDecl.getName() == null ? "" : attrDecl.getName()));
							
							if (attrDecl.getSchema() != null && !attrDecl.getSchema().getTargetNamespace().isEmpty()) {
								String prefix = nsMap.get(attrDecl.getSchema().getTargetNamespace());
								if (prefix != null && !prefix.isEmpty()) {
									attr.setNsUri(prefix);
								}
							}
							
							attr.setDescription(getDocumentation(attrDecl));
							attr.setEntity(attrGroupEntity);
							attr.setMin((attrDecl.isRequired()? 1 : 0));
							attr.setMax(1);
							attr.setIsKey(containsID);
							
							schemaElementMap.put(attr.hashCode(), attr);
												
							processSimpleType(attrDecl.getSimpleType(), attr);
						
					
						} // while attrs left
					
					//	/** process simpleContent by creating special attr **/
					//	if (passedType.isSimpleContent()){
					//		Integer attrID = nextAutoInc();
					//		Attribute simpleContentAttr = new Attribute(attrID,(sawSimpleContentVal ? "simpleContentValue2" : "simpleContentValue"),"added attribute to handle simpleContent",attrGroupEntity.getId(),-1, 0, 1, false, 0);  
					//		_schemaElementsHS.put(attrID, simpleContentAttr);
					//		_schemaElems.put(attrID, passedType);
					//		processSimpleType(null, simpleContentAttr);
					//	}
						
					} // end if -- processing simple content
					
					/** create subtype **/
					attrGroupEntity = attributeGroupEntities.get(attrGroupEntity.getName());
					
					// TODO This was id based
					//Integer subTypeID = nextAutoInc();
					//Subtype subType = new Subtype(subTypeID,attrGroupEntity,entity,null);
					//_schemaElementsHS.put(subTypeID, subType);
					
					Subtype subType = new Subtype();
					subType.setParent(attrGroupEntity);
					subType.setChild(entity);
					schemaElementMap.put(subType.hashCode(), subType);
					
				} // while attr groups left
			
				Enumeration<?> attrDecls = passedType.getAttributeDecls(); 
				// boolean sawSimpleContentVal = false;
				while (attrDecls.hasMoreElements()){
				
					AttributeDecl attrDecl = (AttributeDecl)attrDecls.nextElement();
					
					/** check to see if attributes have already been processed **/
					if (!attributesInAttributeGroups.contains(attrDecl.hashCode())){
					
						try {
							while (attrDecl != null && attrDecl.isReference() == true && attrDecl.getReference() != null)
								attrDecl = attrDecl.getReference();
						} catch(IllegalStateException e){} // handle malformed XSDs that do not have parent set (depreciated attrs as parents)
						
						boolean containsID = attrDecl.getSimpleType() != null && attrDecl.getSimpleType().getName() != null && attrDecl.getSimpleType().getName().equals("ID");
						
						
						// TODO This was id based
						//Integer attrID = nextAutoInc();
						//Attribute attr = new Attribute(attrID,(attrDecl.getName() == null ? "" : attrDecl.getName()),getDocumentation(attrDecl),entity,null,(attrDecl.isRequired()? 1 : 0), 1, containsID, null); 
					//	if (attr.getName().equalsIgnoreCase("simpleContentValue")){
					//		sawSimpleContentVal = true;
					//	}
				
						//_schemaElementsHS.put(attrID, attr);
						
						
						Attribute attr = new Attribute();
						attr.setName((attrDecl.getName() == null ? "" : attrDecl.getName(true)));
						attr.setDescription(getDocumentation(attrDecl));
						attr.setEntity(entity);
						attr.setMin((attrDecl.isRequired()? 1 : 0));
						attr.setMax(1);
						attr.setIsKey(containsID);
						
						if (attrDecl.getSchema() != null && !attrDecl.getSchema().getTargetNamespace().isEmpty()) {
							String prefix = nsMap.get(attrDecl.getSchema().getTargetNamespace());
							if (prefix != null && !prefix.isEmpty()) {
								attr.setNsUri(prefix);
							}
						}
						
						schemaElementMap.put(attr.hashCode(), attr);
						
						
						processSimpleType(attrDecl.getSimpleType(), attr);
					}
					
				//	if (passedType.isSimpleContent()){
				//		Integer attrID = nextAutoInc();
				//		Attribute simpleContentAttr = new Attribute(attrID,(sawSimpleContentVal ? "simpleContentValue2" : "simpleContentValue"),"added attribute to handle simpleContent",entity.getId(),-1, 0, 1, false, 0);  
				//		_schemaElementsHS.put(attrID, simpleContentAttr);
				//		_schemaElems.put(attrID, passedType);
				//		processSimpleType(null, simpleContentAttr);
				//	}
				}	
			} catch (IllegalStateException e){}
			
			
			/** get Elements for current complexType **/
			Enumeration<?> elementDecls = passedType.enumerate();
			while (elementDecls.hasMoreElements()) {
				Group group = (Group)elementDecls.nextElement();
				processGroup(group, entity);
			}
		
			/** get SuperTypes for current complexType **/
			if (passedType.getBaseType() != null){
				XMLType baseType = passedType.getBaseType();
				
				/** process simpleType supertype here -- create a "special" Entity **/
				if (baseType instanceof SimpleType){
					//Subtype subtype = new Subtype(nextAutoInc(),null,entity,null);
					Subtype subtype = new Subtype();
					subtype.setChild(entity);
					schemaElementMap.put(subtype.hashCode(), subtype);
					
					//Entity simpleSuperTypeEntity = new Entity(nextAutoInc(), (baseType.getName() == null ? "" : baseType.getName()), this.getDocumentation(baseType), null);
					Entity simpleSuperTypeEntity = new Entity();
					simpleSuperTypeEntity.setName((baseType.getName() == null ? "" : baseType.getName()));
					simpleSuperTypeEntity.setDescription(this.getDocumentation(baseType));
					
					if (schemaElementMap.get(baseType.hashCode()) == null){
						schemaElementMap.put(baseType.hashCode(), simpleSuperTypeEntity);
					}
					simpleSuperTypeEntity = (Entity)schemaElementMap.get(baseType.hashCode());
					subtype.setParent(simpleSuperTypeEntity);
				}
				else if (baseType instanceof ComplexType){
					//Subtype subtype = new Subtype(nextAutoInc(),null, entity,null);
					Subtype subtype = new Subtype();
					subtype.setChild(entity);
					schemaElementMap.put(subtype.hashCode(), subtype);
					processComplexType((ComplexType)baseType, subtype);
				}	
			}	
		}
		
		/** add Entity for complexType as child of passed containment or subtype **/ 
		entity = (Entity)schemaElementMap.get(passedType.hashCode());
		
		if (parent instanceof Containment && parent != null)
			((Containment)parent).setChild(entity);
		else if (parent instanceof Subtype && parent != null)
			((Subtype)parent).setParent(entity);
				
	} // end method	
		
	
	/**
	 * processGroup:  Processes a grouping of elements in a ComplexType. 
	 * The Elements in a ComplexType are contained in 1 or more Groups, 
	 * each of which is processed by this method.
	 * 
	 * @param group Element Group to be processed 
	 * @param parent Entity corresponding to complexType
	 */
	public void processGroup (Group group, Entity parent){
		
		// step through item in a group
		Enumeration<?> e = group.enumerate();
		while (e.hasMoreElements()) {
			
			
			
			Object obj = e.nextElement();
			try {
			
			// For WildCard, create containment child to "Any" domain
			if (obj instanceof Wildcard){
				Domain anyDomain = domainMap.get("ANY");
				if (!schemaElementMap.containsKey(anyDomain.hashCode())) {
					schemaElementMap.put(anyDomain.hashCode(), anyDomain);
				}
				
				//Containment containment = new Containment(nextAutoInc(),"Any", this.getDocumentation((Annotated)obj), parent, anyDomain, 0, 1, null);
				Containment containment = new Containment();
				containment.setName("Any");
				containment.setDescription(this.getDocumentation((Annotated)obj));
				containment.setParent(parent);
				containment.setChild(anyDomain);
				containment.setMin(0);
				containment.setMax(1);
				
				schemaElementMap.put(containment.hashCode(), containment);
			}
			// process Group item
			else if (obj instanceof Group)
				processGroup((Group)obj, parent);	
			
			// process Element item
			else if (obj instanceof ElementDecl)  
				processElement((ElementDecl)obj, parent);
			
			else
				System.err.println("(E) XSDImporter:processGroup -- Encountered object named " + obj.toString() + " with unknown type " + obj.getClass());
							
			} catch (Exception e2) {
				logger.error("Exception during import", e2);
			}
		}
	} // end method

	
	/**
	 * processElement:  Creates an M3 Containment corresponding to the Element declaration in
	 * a ComplexType.  Parent of containment will be passed Entity, and the child will be either 
	 * M3 Entity for specified complexType or M3 Domain for specified simpleType.
	 * 
	 * @param elementDecl Element declaration in XSD ComplexType
	 * @param parent Entity corresponding to complexType containing elementDecl
	 */
	public void processElement(ElementDecl elementDecl, Entity parent)
	{
		/** dereference xs:ref until we find actual element declarations **/
		Integer origMin = elementDecl.getMinOccurs();
		Integer origMax = elementDecl.getMaxOccurs();
		Integer origHashcode = elementDecl.hashCode();
		ElementDecl origElementDecl = elementDecl;
		try {
			while (elementDecl.isReference() && elementDecl.getReference() != null)
				elementDecl = elementDecl.getReference();
		} catch (IllegalStateException e) {}{}
		
	
		//Containment containment = new Containment(nextAutoInc(),elementDecl.getName(),this.getDocumentation(elementDecl),((parent != null) ? parent : null),null,origMin,origMax,null);
		Containment containment = new Containment();
		containment.setName(elementDecl.getName());
		containment.setDescription(this.getDocumentation(elementDecl));
		containment.setParent(((parent != null) ? parent : null));
		//containment.setChild(anyDomain);
		containment.setMin(origMin);
		containment.setMax(origMax);
		
		if (elementDecl.getSchema() != null && elementDecl.getSchema().getTargetNamespace()!=null && !elementDecl.getSchema().getTargetNamespace().isEmpty()) {
			String prefix = nsMap.get(elementDecl.getSchema().getTargetNamespace());
			if (prefix != null && !prefix.isEmpty()) {
				containment.setNsUri(prefix);
			}
		}
		
		if (schemaElementMap.containsKey(origHashcode) == false){
			schemaElementMap.put(origHashcode, containment);
			
		}

		XMLType childElementType = null;
		try { 
			childElementType = elementDecl.getType();
		} catch (IllegalStateException e){} 
		if ((childElementType == null) || (childElementType instanceof SimpleType) || (childElementType instanceof AnyType)) 				
			processSimpleType(childElementType, containment);

		else if (childElementType instanceof ComplexType)
			processComplexType((ComplexType)childElementType,containment);

		else 
			System.err.println("(E) XSDImporter:processElement -- Encountered object named " 
				+ elementDecl.getName() + " with unknown type " 
				+  ((childElementType == null)? null : childElementType.getClass()));
	
		
	} // end method

	
	/**
	 * getDocumentation: Get the documentation associated with specified element
	 * @param element element to get documentation about
	 * @return The documentation associated with a specific element
	 */
	private String getDocumentation(Annotated element) {
		
		StringBuffer documentation = new StringBuffer("");
		documentation.append(appendDocumentation(element));
		
		// post-process documentation string to remove 
		String retVal = documentation.toString();
		retVal = retVal.replaceAll("\\s+", " ");
		
		return retVal;
	}

	/**
	 * appendDocumentation: Get the documentation associated with specified type
	 * @param type type to get documentation about
	 * @return The documentation associated with a specific element
	 */
	private StringBuffer appendDocumentation(Annotated type) {
		StringBuffer documentation = new StringBuffer();
		if (type != null) {
			Enumeration annotations = type.getAnnotations();
			while (annotations.hasMoreElements()) {
				Annotation annotation = (Annotation) annotations.nextElement();
				Enumeration docs = annotation.getDocumentation();
				while (docs.hasMoreElements()) {
					Documentation doc = (Documentation) docs.nextElement();
					if (doc.getContent() != null)
						documentation.append(doc.getContent().replaceAll("<",
								"&lt;").replaceAll(">", "&gt;").replaceAll("&",
								"&amp;"));
				}
			}
		}
		return documentation;
	}
}
