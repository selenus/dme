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

import de.dariah.samlsp.model.pojo.AuthPojo;
import eu.dariah.de.minfba.core.metamodel.Nonterminal;
import eu.dariah.de.minfba.core.metamodel.xml.XmlNamespace;
import eu.dariah.de.minfba.core.metamodel.xml.XmlSchema;
import eu.dariah.de.minfba.core.metamodel.xml.XmlTerminal;

@Component
@Scope(value=ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class XmlSchemaImporter implements SchemaImporter<XmlSchema> {
	private static final Logger logger = LoggerFactory.getLogger(XmlSchemaImporter.class);

	private SchemaImportListener listener;
	private Map<String, XmlTerminal> existingTerminalQNs = new HashMap<String, XmlTerminal>();
	private XSModel model;
	
	private XmlSchema schema;
	private String schemaFilePath;
	private String rootElementNs;
	private String rootElementName; 
	private String[] namespaces;
	
	private AuthPojo auth;
	
	private Nonterminal rootNonterminal;
	
	public XmlSchema getSchema() { return schema; }
	@Override public void setSchema(XmlSchema schema) { this.schema = schema; }
	
	public String getSchemaFilePath() { return schemaFilePath; }
	@Override public void setSchemaFilePath(String schemaFilePath) { this.schemaFilePath = schemaFilePath; }
 	
 	public String getRootElementNs() { return rootElementNs; }
 	@Override public void setRootElementNs(String rootElementNs) { this.rootElementNs = rootElementNs; }
	
	public String getRootElementName() { return rootElementName; }
	@Override public void setRootElementName(String rootElementName) { this.rootElementName = rootElementName; }
	
	@Override public Nonterminal getRootNonterminal() { return rootNonterminal; }
	public void setRootNonterminal(Nonterminal rootNonterminal) { this.rootNonterminal = rootNonterminal; }
	
	@Override public String[] getNamespaces() { return namespaces; }
	public void setNamespaces(String[] namespaces) { this.namespaces = namespaces; }
	
	public SchemaImportListener getListener() { return listener; }
	@Override public void setListener(SchemaImportListener listener) { this.listener = listener; }
	
	@Override public void setAuth(AuthPojo auth) { this.auth = auth; }
	@Override public AuthPojo getAuth() { return this.auth; }
	
	@Override
	public void run() {
		try {
			this.importXmlSchema();
			if (this.getListener()!=null) {
				schema.setNamespaces(new ArrayList<XmlNamespace>());
				for (String ns : namespaces) {
					schema.getNamespaces().add(new XmlNamespace("", ns));
				}				
				schema.setTerminals(new ArrayList<XmlTerminal>(this.existingTerminalQNs.values()));
				this.getListener().registerImportFinished(schema, rootNonterminal, auth);
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
	
	protected void importXmlSchema() {
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
	}
	
	protected Nonterminal getRoot(String rootElementNs, String rootElementName) {
		XSNamedMap elements = this.model.getComponents(XSConstants.ELEMENT_DECLARATION);
		for (int j=0; j<elements.getLength(); j++) {
			XSElementDecl elem = (XSElementDecl)elements.item(j);
			if (elem.getNamespace().equals(rootElementNs) && elem.getName().equals(rootElementName)) {
				logger.info("Identified root element declaration [{}]{}", rootElementNs, rootElementName);
				// Enter element processing at identified root element
				return this.processElement(null, elem, new ArrayList<String>());
			}
		}
		logger.warn("Unknown element declaration [{}]{}", rootElementNs, rootElementName);
		return null;
	}
	
	protected Nonterminal processElement(Nonterminal parentNonterminal, XSElementDecl element, List<String> processedTerminalQNs) {
		String terminalQN = this.createTerminalQN(element.getNamespace(), element.getName(), false);
		if (processedTerminalQNs.contains(terminalQN)) {
			logger.warn("Recursion detected [{}]...element processing is cut", terminalQN);
		}
		
		Nonterminal n = this.createNonterminal(element.getNamespace(), element.getName(), false);
		
		XSTypeDefinition typeDef = element.getTypeDefinition();		
		if (typeDef.getTypeCategory()==XSTypeDefinition.COMPLEX_TYPE) {
			// New element hierarchy level only here, split 'knownItems' list 
			this.processComplexElement(n, element,(XSComplexTypeDefinition)typeDef, new ArrayList<String>(processedTerminalQNs));
		} 
		// NOTE: Nothing to do for simple elements
		
		addChildNonterminal(parentNonterminal, n);
		return n;
	}
	
	protected void processComplexElement(Nonterminal nonterminal, XSElementDecl element, XSComplexTypeDefinition typeDef, List<String> processedTerminalQNs) {
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

	protected void processContentModel(Nonterminal parentNonterminal, XSParticle particle, List<String> processedTerminalQNs) {
		XSTerm contentModel = particle.getTerm();

		if (contentModel.getType()==XSConstants.ELEMENT_DECLARATION) {
			this.processElement(parentNonterminal, (XSElementDecl)contentModel, processedTerminalQNs);
		} else if (contentModel.getType()==XSConstants.MODEL_GROUP) {
			XSModelGroup modelGroup = (XSModelGroup) contentModel;
			XSObjectList groupElements = modelGroup.getParticles();
			for (int i=0; i<groupElements.getLength(); i++) {
				this.processContentModel(parentNonterminal, (XSParticle)groupElements.get(i), processedTerminalQNs);
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

	protected Nonterminal createNonterminal(String terminalNamespace, String terminalName, boolean isAttribute) {
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
		
		Nonterminal n = new Nonterminal();
		n.setId(new ObjectId().toString());
		n.setName(this.createNonterminalName(terminalName));
		n.setTerminalId(terminalId);
		n.setSchemaId(this.schema.getId());
		return n;
	}
	
	protected String createTerminalQN(String terminalNamespace, String terminalName, boolean isAttribute) {
		return String.format("{%s}:%s%s", terminalNamespace, (isAttribute ? "#" : ""), terminalName);
	}
	
	protected String createNonterminalName(String terminalName) {
		return terminalName.substring(0, 1).toUpperCase() + terminalName.substring(1); 
	}
}