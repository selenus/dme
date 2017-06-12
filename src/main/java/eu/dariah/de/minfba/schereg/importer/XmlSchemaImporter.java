package eu.dariah.de.minfba.schereg.importer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.xerces.dom.DOMXSImplementationSourceImpl;
import org.apache.xerces.impl.xs.XSElementDecl;
import org.apache.xerces.xs.XSAttributeDeclaration;
import org.apache.xerces.xs.XSAttributeUse;
import org.apache.xerces.xs.XSComplexTypeDefinition;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSImplementation;
import org.apache.xerces.xs.XSLoader;
import org.apache.xerces.xs.XSModel;
import org.apache.xerces.xs.XSModelGroup;
import org.apache.xerces.xs.XSNamedMap;
import org.apache.xerces.xs.XSNamespaceItemList;
import org.apache.xerces.xs.XSObjectList;
import org.apache.xerces.xs.XSParticle;
import org.apache.xerces.xs.XSTerm;
import org.apache.xerces.xs.XSTypeDefinition;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import eu.dariah.de.dariahsp.model.web.AuthPojo;
import eu.dariah.de.minfba.core.metamodel.NonterminalImpl;
import eu.dariah.de.minfba.core.metamodel.exception.MetamodelConsistencyException;
import eu.dariah.de.minfba.core.metamodel.interfaces.Nonterminal;
import eu.dariah.de.minfba.core.metamodel.tracking.ChangeType;
import eu.dariah.de.minfba.core.metamodel.xml.XmlNamespace;
import eu.dariah.de.minfba.core.metamodel.xml.XmlSchemaNature;
import eu.dariah.de.minfba.core.metamodel.xml.XmlTerminal;
import eu.dariah.de.minfba.core.util.Stopwatch;

@Component
@Scope(value=ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class XmlSchemaImporter implements SchemaImporter<XmlSchemaNature> {
	private static final Logger logger = LoggerFactory.getLogger(XmlSchemaImporter.class);

	private SchemaImportListener listener;
	private Map<String, XmlTerminal> existingTerminalQNs = new HashMap<String, XmlTerminal>();
	private XSModel model;
	
	private XmlSchemaNature schema;
	private String schemaFilePath;
	private String rootElementNs;
	private String rootElementName; 
	private String[] namespaces;
	
	private AuthPojo auth;
	
	private Nonterminal rootNonterminal;
	private List<Nonterminal> additionalRootElements;
	
	public XmlSchemaNature getSchema() { return schema; }
	@Override public void setSchema(XmlSchemaNature schema) { this.schema = schema; }
	
	public String getSchemaFilePath() { return schemaFilePath; }
	@Override public void setSchemaFilePath(String schemaFilePath) { this.schemaFilePath = schemaFilePath; }
 	
 	public String getRootElementNs() { return rootElementNs; }
 	@Override public void setRootElementNs(String rootElementNs) { this.rootElementNs = rootElementNs; }
	
	public String getRootElementName() { return rootElementName; }
	@Override public void setRootElementName(String rootElementName) { this.rootElementName = rootElementName; }
	
	@Override public Nonterminal getRootNonterminal() { return rootNonterminal; }
	public void setRootNonterminal(Nonterminal rootNonterminal) { this.rootNonterminal = rootNonterminal; }
	
	@Override public List<Nonterminal> getAdditionalRootElements() { return additionalRootElements; }
	public void setAdditionalRootElements(List<Nonterminal> additionalRootElements) { this.additionalRootElements = additionalRootElements; }
	
	@Override public String[] getNamespaces() { return namespaces; }
	public void setNamespaces(String[] namespaces) { this.namespaces = namespaces; }
	
	public SchemaImportListener getListener() { return listener; }
	@Override public void setListener(SchemaImportListener listener) { this.listener = listener; }
	
	@Override public void setAuth(AuthPojo auth) { this.auth = auth; }
	@Override public AuthPojo getAuth() { return this.auth; }
	
	@Override
	public void run() {
		try {
			Stopwatch sw = new Stopwatch().start();
			logger.debug(String.format("Started importing schema %s", schema.getEntityId()));
			
			this.importXmlSchema();
			if (this.getListener()!=null) {
				schema.setNamespaces(new ArrayList<XmlNamespace>());
				for (String ns : namespaces) {
					XmlNamespace namespace = new XmlNamespace("", ns);
					namespace.setId(new ObjectId().toString());
					namespace.addChange(ChangeType.NEW_OBJECT, "namespace", null, namespace.getId());
					schema.getNamespaces().add(namespace);
				}				
				schema.setTerminals(new ArrayList<XmlTerminal>(this.existingTerminalQNs.values()));
				
				logger.info(String.format("Finisched importing schema %s in %sms", schema.getEntityId(), sw.getElapsedTime()));
				this.getListener().registerImportFinished(schema, rootNonterminal, additionalRootElements, auth);
			}
		} catch (Exception e) {
			logger.error("Error while importing XML Schema", e);
			if (this.getListener()!=null) {
				this.getListener().registerImportFailed(schema);
			}
		}
	}
	
	@Override
	public boolean getIsSupported() {
		try {
			XSImplementation impl = (XSImplementation)(new DOMXSImplementationSourceImpl()).getDOMImplementation ("XS-Loader");
			XSLoader schemaLoader = impl.createXSLoader(null);
			model = schemaLoader.loadURI(schemaFilePath);
			
			if (model!=null) {
				return true;
			}
		} catch (Exception e) {}
		return false;
	}
	
	@Override
	public List<XmlTerminal> getPossibleRootTerminals() {
		try {
			XSImplementation impl = (XSImplementation)(new DOMXSImplementationSourceImpl()).getDOMImplementation ("XS-Loader");
			XSLoader schemaLoader = impl.createXSLoader(null);
			model = schemaLoader.loadURI(schemaFilePath);
			
			XSNamedMap elements = this.model.getComponents(XSConstants.ELEMENT_DECLARATION);
			List<XmlTerminal> rootTerminals = new ArrayList<XmlTerminal>(elements.getLength());
			XmlTerminal root;
			XSElementDecl elem;
			for (int j=0; j<elements.getLength(); j++) {
				elem = (XSElementDecl)elements.item(j);
				root = new XmlTerminal();
				root.setName(elem.getName());
				root.setNamespace(elem.getNamespace());
				rootTerminals.add(root);
			}
			return rootTerminals;
		} catch (Exception e) {}
		return null;
	}
	
	protected void importXmlSchema() throws MetamodelConsistencyException {
		XSImplementation impl = (XSImplementation)(new DOMXSImplementationSourceImpl()).getDOMImplementation ("XS-Loader");
		XSLoader schemaLoader = impl.createXSLoader(null);
		model = schemaLoader.loadURI(schemaFilePath);
		
		/* Namespace precollection */
		XSNamespaceItemList nsList = model.getNamespaceItems();
		if (nsList!=null && nsList.size()>0) {
			namespaces = new String[nsList.size()];
			for (int i=0; i<nsList.getLength(); i++) {
				namespaces[i] = nsList.item(i).getSchemaNamespace();
			}
		}
		
		/* Element processing */
		this.rootNonterminal = this.getRoot(rootElementNs, rootElementName);
		
		XSNamedMap elements = this.model.getComponents(XSConstants.ELEMENT_DECLARATION);
		Map<String, XmlTerminal> rootTerminals = new HashMap<String, XmlTerminal>();
		XmlTerminal root;
		
		XSElementDecl elem;
		String terminalQN;
		for (int j=0; j<elements.getLength(); j++) {
			elem = (XSElementDecl)elements.item(j);
			root = new XmlTerminal();
			root.setName(elem.getName());
			root.setNamespace(elem.getNamespace());
			
			terminalQN = createTerminalQN(elem.getNamespace(), elem.getName(), false);
			
			if (!existingTerminalQNs.containsKey(terminalQN)) {
				rootTerminals.put(root.getName() + ":" + root.getNamespace(), root);
			}
		}
		
		if (rootTerminals.size()==0) {
			return;
		}
		
		List<Nonterminal> potentialRootElements = new ArrayList<Nonterminal>(rootTerminals.keySet().size());
		Nonterminal additionalRootNonterminal;
		XmlTerminal additionalRootTerminal;
		for (String qn : rootTerminals.keySet()) {
			additionalRootTerminal = rootTerminals.get(qn);
			additionalRootNonterminal = this.getRoot(additionalRootTerminal.getNamespace(), additionalRootTerminal.getName());
			if (additionalRootNonterminal!=null) {
				potentialRootElements.add(additionalRootNonterminal);
			}
		}
		
		List<Nonterminal> compareN = new ArrayList<Nonterminal>(potentialRootElements);
		
		// Check for each potential root if it is a child in any other potential tree
		for (Nonterminal addRoot : potentialRootElements) {
			for (Nonterminal compareRoot : compareN) {
				if (addRoot.equals(compareRoot)) {
					continue;
				}
				if (this.getChildrenContainTerminalId(compareRoot, schema.getTerminalId(addRoot.getId()))) {
					compareN.remove(addRoot);
					break;
				}
			}
		}
		this.additionalRootElements = compareN;
	}
	
	private boolean getChildrenContainTerminalId(Nonterminal parent, String terminalId) {
		
		if (schema.getTerminalId(parent.getId()).equals(terminalId)) {
			return true;
		}
		if (parent.getChildNonterminals()!=null && !parent.getChildNonterminals().isEmpty()) {
			for (Nonterminal child : parent.getChildNonterminals()) {
				if (this.getChildrenContainTerminalId(child, terminalId)) {
					return true;
				}
			}
		}
		return false;
	}
	
	protected Nonterminal getRoot(String rootElementNs, String rootElementName) throws MetamodelConsistencyException {
		XSNamedMap elements = this.model.getComponents(XSConstants.ELEMENT_DECLARATION);
		boolean nsMatch;
		for (int j=0; j<elements.getLength(); j++) {
			XSElementDecl elem = (XSElementDecl)elements.item(j);
			nsMatch = elem.getNamespace()==null && rootElementNs==null || 
					( elem.getNamespace()!=null && rootElementNs!=null && elem.getNamespace().equals(rootElementNs) );
			
			if (nsMatch && elem.getName().equals(rootElementName)) {
				// Enter element processing at identified root element
				return this.processElement(null, elem, new ArrayList<String>());
			}
		}
		logger.warn("Unknown element declaration [{}]{}", rootElementNs, rootElementName);
		return null;
	}
	
	protected Nonterminal processElement(Nonterminal parentNonterminal, XSElementDecl element, List<String> processedTerminalQNs) throws MetamodelConsistencyException {
		String terminalQN = this.createTerminalQN(element.getNamespace(), element.getName(), false);
		Nonterminal n = this.createNonterminal(element.getNamespace(), element.getName(), false);
		if (processedTerminalQNs.contains(terminalQN)) {
			logger.warn("Recursion detected [{}]...element processing is cut", terminalQN);
			return n;
		} else {
			processedTerminalQNs.add(terminalQN);
		}
		
		XSTypeDefinition typeDef = element.getTypeDefinition();		
		if (typeDef.getTypeCategory()==XSTypeDefinition.COMPLEX_TYPE) {
			// New element hierarchy level only here, split 'knownItems' list 
			this.processComplexElement(n, element,(XSComplexTypeDefinition)typeDef, new ArrayList<String>(processedTerminalQNs));
		} 
		// NOTE: Nothing to do for simple elements
		
		addChildNonterminal(parentNonterminal, n);
		return n;
	}
	
	protected void processComplexElement(Nonterminal nonterminal, XSElementDecl element, XSComplexTypeDefinition typeDef, List<String> processedTerminalQNs) throws MetamodelConsistencyException {
		XSObjectList attrList = typeDef.getAttributeUses();
		
		// Process attributes
		Nonterminal attr;
		for (int i=0; i<attrList.getLength(); i++) {
			XSAttributeDeclaration attrDecl = ((XSAttributeUse)attrList.item(i)).getAttrDeclaration();
			attr = this.createNonterminal(attrDecl.getNamespace(), attrDecl.getName(), true);
			addChildNonterminal(nonterminal, attr);
		}
		
		// Process content model
		if (typeDef.getContentType()==XSComplexTypeDefinition.CONTENTTYPE_ELEMENT || 
				typeDef.getContentType()==XSComplexTypeDefinition.CONTENTTYPE_MIXED) {
			this.processContentModel(nonterminal, typeDef.getParticle(), processedTerminalQNs);
		}
		// NOTE: Nothing to do for simple elements as attributes have already been processed
	}

	protected void processContentModel(Nonterminal parentNonterminal, XSParticle particle, List<String> processedTerminalQNs) throws MetamodelConsistencyException {
		XSTerm contentModel = particle.getTerm();

		if (contentModel.getType()==XSConstants.ELEMENT_DECLARATION) {
			this.processElement(parentNonterminal, (XSElementDecl)contentModel, new ArrayList<String>(processedTerminalQNs));
		} else if (contentModel.getType()==XSConstants.MODEL_GROUP) {
			XSModelGroup modelGroup = (XSModelGroup) contentModel;
			XSObjectList groupElements = modelGroup.getParticles();
			for (int i=0; i<groupElements.getLength(); i++) {
				this.processContentModel(parentNonterminal, (XSParticle)groupElements.get(i), new ArrayList<String>(processedTerminalQNs));
			}
		}
	}
	
	protected void addChildNonterminal(Nonterminal parent, Nonterminal child) {
		if (parent!=null) {
			if (parent.getChildNonterminals()==null) {
				parent.setChildNonterminals(new ArrayList<Nonterminal>());
			}
			parent.getChildNonterminals().add(child);
		}
	}

	protected Nonterminal createNonterminal(String terminalNamespace, String terminalName, boolean isAttribute) throws MetamodelConsistencyException {
		String terminalQN = createTerminalQN(terminalNamespace, terminalName, isAttribute);
		
		String terminalId = null;
		if (existingTerminalQNs.containsKey(terminalQN)) {
			terminalId = existingTerminalQNs.get(terminalQN).getId();
		} else {
			XmlTerminal t = new XmlTerminal();
			t.setNamespace(terminalNamespace);
			t.setId(new ObjectId().toString());
			t.setName(terminalName);
			t.setAttribute(isAttribute);
			
			terminalId = t.getId();
			existingTerminalQNs.put(terminalQN, t);
		}
				
		Nonterminal n = new NonterminalImpl();
		n.setId(new ObjectId().toString());
		n.setName(this.createNonterminalName(terminalName));
		
		schema.mapNonterminal(n.getId(), terminalId);
		
		n.setEntityId(this.schema.getId());
		return n;
	}
	
	protected String createTerminalQN(String terminalNamespace, String terminalName, boolean isAttribute) {
		return String.format("{%s}:%s%s", terminalNamespace, (isAttribute ? "#" : ""), terminalName);
	}
	
	protected String createNonterminalName(String terminalName) {
		String name = terminalName.substring(0, 1).toUpperCase() + terminalName.substring(1);
		
		name = name.replaceAll("([^\\p{L}])([^\\p{L}\\p{N}-_.])*", "");
		
		return name; 
	}
}