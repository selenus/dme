package de.unibamberg.minf.dme.importer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.xerces.dom.DOMXSImplementationSourceImpl;
import org.apache.xerces.impl.xs.XSElementDecl;
import org.apache.xerces.xs.XSAttributeDeclaration;
import org.apache.xerces.xs.XSAttributeUse;
import org.apache.xerces.xs.XSComplexTypeDefinition;
import org.apache.xerces.xs.XSConstants;
import org.apache.xerces.xs.XSElementDeclaration;
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
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import de.unibamberg.minf.core.util.Stopwatch;
import de.unibamberg.minf.dme.importer.model.ImportAwareNonterminal;
import de.unibamberg.minf.dme.model.base.Identifiable;
import de.unibamberg.minf.dme.model.base.ModelElement;
import de.unibamberg.minf.dme.model.base.Nonterminal;
import de.unibamberg.minf.dme.model.base.Terminal;
import de.unibamberg.minf.dme.model.datamodel.NonterminalImpl;
import de.unibamberg.minf.dme.model.datamodel.natures.XmlDatamodelNature;
import de.unibamberg.minf.dme.model.datamodel.natures.xml.XmlNamespace;
import de.unibamberg.minf.dme.model.datamodel.natures.xml.XmlTerminal;
import de.unibamberg.minf.dme.model.exception.MetamodelConsistencyException;
import de.unibamberg.minf.dme.model.tracking.ChangeType;
import de.unibamberg.minf.dme.service.IdentifiableServiceImpl;

@Component
@Scope(value=ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class XmlSchemaImporter extends BaseSchemaImporter implements SchemaImporter {
	
	private Map<String, XmlTerminal> existingTerminalQNs = new HashMap<String, XmlTerminal>();
	private XSModel model;
	private String[] namespaces;
	private Map<String, List<ImportAwareNonterminal>> extensionIdNonterminalMap;
	private Map<String, ImportAwareNonterminal> terminalIdNonterminalMap;

	private XmlDatamodelNature xmlNature;
	
	@Override public String[] getNamespaces() { return namespaces; }
	public void setNamespaces(String[] namespaces) { this.namespaces = namespaces; }
	
	@Override
	public void run() {
		try {
			Stopwatch sw = new Stopwatch().start();
			logger.debug(String.format("Started importing schema %s", this.getSchema().getId()));
			
			this.prepareXmlNature();
			
			this.extensionIdNonterminalMap = new HashMap<String, List<ImportAwareNonterminal>>();
			this.terminalIdNonterminalMap = new HashMap<String, ImportAwareNonterminal>();
			
			this.importXmlSchema();
			if (this.getListener()!=null) {
				xmlNature.setNamespaces(new ArrayList<XmlNamespace>());
				for (String ns : namespaces) {
					XmlNamespace namespace = new XmlNamespace("", ns);
					namespace.setId(new ObjectId().toString());
					namespace.addChange(ChangeType.NEW_OBJECT, "namespace", null, namespace.getId());
					xmlNature.getNamespaces().add(namespace);
				}				
				xmlNature.setTerminals(new ArrayList<XmlTerminal>(this.existingTerminalQNs.values()));
				
				logger.info(String.format("Finished importing schema %s in %sms", xmlNature.getId(), sw.getElapsedTime()));
				
				this.getListener().registerImportFinished(this.getSchema(), this.getElementId(), this.getRootElements(), this.getAdditionalRootElements(), this.getAuth());
			}
		} catch (Exception e) {
			logger.error("Error while importing XML Schema", e);
			if (this.getListener()!=null) {
				this.getListener().registerImportFailed(this.getSchema());
			}
		}
	}
	
	@Override
	public List<? extends ModelElement> getElementsByTypes(List<Class<? extends ModelElement>> allowedSubtreeRoots) {
		this.run();
		return IdentifiableServiceImpl.extractAllByTypes(this.getRootElements().get(0), allowedSubtreeRoots);
	}
	
	@Override
	public boolean getIsSupported() {
		try {
			XSImplementation impl = (XSImplementation)(new DOMXSImplementationSourceImpl()).getDOMImplementation ("XS-Loader");
			XSLoader schemaLoader = impl.createXSLoader(null);
			model = schemaLoader.loadURI(this.getSchemaFilePath());
			
			if (model!=null) {
				return true;
			}
		} catch (Exception e) {}
		return false;
	}
	
	@Override
	public List<XmlTerminal> getPossibleRootElements() {
		try {
			Stopwatch sw = new Stopwatch().start();
			
			XSImplementation impl = (XSImplementation)(new DOMXSImplementationSourceImpl()).getDOMImplementation ("XS-Loader");
			XSLoader schemaLoader = impl.createXSLoader(null);
			model = schemaLoader.loadURI(this.getSchemaFilePath());
			
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
			
			logger.debug("Detection of possible root nonterminals took {}ms", sw.getElapsedTime());
			
			return rootTerminals;
		} catch (Exception e) {}
		return null;
	}
	
	private void prepareXmlNature() {
		if (this.getSchema().getNature(XmlDatamodelNature.class)!=null) {
			xmlNature = this.getSchema().getNature(XmlDatamodelNature.class);
		} else {
			xmlNature = new XmlDatamodelNature();
			xmlNature.setId(this.getSchema().getId());
			this.getSchema().addOrReplaceNature(xmlNature);
		}
		
		XmlTerminal rootTerminal = null;
		String compQN;
		for (Terminal possibleRoot : this.getPossibleRootElements()) {
			compQN = this.createTerminalQN(((XmlTerminal)possibleRoot).getNamespace(), possibleRoot.getName(), ((XmlTerminal)possibleRoot).isAttribute());
			if (compQN.equals(this.getRootElementName())) {
				rootTerminal = (XmlTerminal)possibleRoot;
				break;
			}
		}
		
		xmlNature.setRootElementNamespace(rootTerminal.getNamespace());
		xmlNature.setRootElementName(rootTerminal.getName());
	}
	
	protected void importXmlSchema() throws MetamodelConsistencyException {
		Stopwatch sw = new Stopwatch().start();
		XSImplementation impl = (XSImplementation)(new DOMXSImplementationSourceImpl()).getDOMImplementation ("XS-Loader");
		XSLoader schemaLoader = impl.createXSLoader(null);
		model = schemaLoader.loadURI(this.getSchemaFilePath());
		
		/* Namespace precollection */
		XSNamespaceItemList nsList = model.getNamespaceItems();
		if (nsList!=null && nsList.size()>0) {
			namespaces = new String[nsList.size()];
			for (int i=0; i<nsList.getLength(); i++) {
				namespaces[i] = nsList.item(i).getSchemaNamespace();
			}
		}
		
		/* Element processing */
		Nonterminal rootN = this.getRoot(xmlNature.getRootElementNamespace(), xmlNature.getRootElementName());
		rootN.setProcessingRoot(true);
		this.getRootElements().add(rootN);
		
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
		
		Map<String, NonterminalImpl> serializedNonterminals = new HashMap<String, NonterminalImpl>();
		List<ImportAwareNonterminal> processedN = new ArrayList<ImportAwareNonterminal>();
		
		// No "additional roots" possible
		if (rootTerminals.size()==0) {
			this.combineExtensionNonterminals();
			
			// We expect that no abstract element can be selected as root!
			this.resolveExtensionHierarchy((ImportAwareNonterminal)this.getRootElements().get(0), processedN);
			
			
			rootN = this.convertToSerializableNonterminals((ImportAwareNonterminal)this.getRootElements().get(0), serializedNonterminals);
			rootN.setProcessingRoot(true);
			this.getRootElements().set(0, rootN);
	
			
			logger.debug("Schema import took {}ms", sw.getElapsedTime());
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
		
		List<ModelElement> compareN = new ArrayList<ModelElement>(potentialRootElements);
		List<Nonterminal> checkedReuseN = new ArrayList<Nonterminal>();
		// Check for each potential root if it is a child in any other potential tree
		for (Nonterminal addRoot : potentialRootElements) {
			for (ModelElement compareRoot : compareN) {
				if (addRoot.equals(compareRoot)) {
					continue;
				}
				if (this.getChildrenContainTerminalId(compareRoot, this.xmlNature.getTerminalId(addRoot.getId()), checkedReuseN)) {
					compareN.remove(addRoot);
					break;
				}
			}
		}
		this.setAdditionalRootElements(compareN);
		
		
		this.combineExtensionNonterminals();
		
		
		
		// We expect that no abstract element can be selected as root!
		this.resolveExtensionHierarchy((ImportAwareNonterminal)this.getRootElements().get(0), processedN);
		
		rootN = this.convertToSerializableNonterminals((ImportAwareNonterminal)this.getRootElements().get(0), serializedNonterminals);
		rootN.setProcessingRoot(true);
		this.getRootElements().set(0, rootN);
		
		for (int i=0; i<this.getAdditionalRootElements().size(); i++) {
			this.resolveExtensionHierarchy((ImportAwareNonterminal)this.getAdditionalRootElements().get(i), processedN);
			this.getAdditionalRootElements().set(i, this.convertToSerializableNonterminals((ImportAwareNonterminal)this.getAdditionalRootElements().get(i), serializedNonterminals));
		}
		logger.debug("Schema import took {}ms", sw.getElapsedTime());
	}
	
	private Nonterminal convertToSerializableNonterminals(ImportAwareNonterminal n, Map<String, NonterminalImpl> serializedNonterminals) {
		if (n==null) {
			return null;
		}
		if (serializedNonterminals.containsKey(n.getId())) {
			return serializedNonterminals.get(n.getId());
		}
		NonterminalImpl nResult = new NonterminalImpl(n.getEntityId(), n.getId(), n.getName());
		serializedNonterminals.put(nResult.getId(), nResult);
		if (n.getChildNonterminals()!=null) {
			nResult.setChildNonterminals(new ArrayList<Nonterminal>());
			for (Nonterminal nChild : n.getChildNonterminals()) {
				nResult.getChildNonterminals().add(this.convertToSerializableNonterminals((ImportAwareNonterminal)nChild, serializedNonterminals));
			}
		}
		return nResult;
	}
	
	private void resolveExtensionHierarchy(ImportAwareNonterminal n, List<ImportAwareNonterminal> processedN) {
		if (processedN.contains(n)) {
			return;
		} 
		processedN.add(n);
		if (n.getChildNonterminals()!=null) {
			ImportAwareNonterminal childN;
			List<ImportAwareNonterminal> extensionChildren = new ArrayList<ImportAwareNonterminal>();
			List<ImportAwareNonterminal> abstractChildren = new ArrayList<ImportAwareNonterminal>();
			for (int i=0; i<n.getChildNonterminals().size(); i++) {
				childN = (ImportAwareNonterminal)n.getChildNonterminals().get(i);
				if (childN.isAbstract()) {
					childN.setParentCount(childN.getParentCount()-1);
					abstractChildren.add(childN);
				}
				this.resolveExtensionHierarchy(childN, processedN);
				
				if (extensionIdNonterminalMap.containsKey(childN.getTerminalQN())) {
					for (ImportAwareNonterminal extN : extensionIdNonterminalMap.get(childN.getTerminalQN())) {
						if (!n.getChildNonterminals().contains(extN) && !extensionChildren.contains(extN)) {
							extensionChildren.add(extN);
						}
					}
				}
			}
			for (ImportAwareNonterminal extChild : extensionChildren) {
				extChild.setParentCount(extChild.getParentCount()+1);
				n.getChildNonterminals().add(extChild);
			}
			n.getChildNonterminals().removeAll(abstractChildren);
		}
	}
	
	private void combineExtensionNonterminals() {
		/* Iterate over all extension nonterminals (the ones that belong to a substitution group) and
		 *  check if they are extended even furter... */
		for (Collection<ImportAwareNonterminal> extensibleNonterminals : this.extensionIdNonterminalMap.values()) {
			for (String terminalQN : this.extensionIdNonterminalMap.keySet()) {
				for (ImportAwareNonterminal extN : extensibleNonterminals) {
					if (extN.getTerminalQN().equals(terminalQN)) {
						for (ImportAwareNonterminal furtherExtN : this.extensionIdNonterminalMap.get(terminalQN)) {
							if (extN.getExtensions()==null) {
								extN.setExtensions(new ArrayList<ImportAwareNonterminal>());
							}
							extN.getExtensions().add(furtherExtN);
						}
					}
				}
			}	
		}
	}
	
	private boolean getChildrenContainTerminalId(Identifiable parent, String terminalId, List<Nonterminal> checkedReuseN) {
		
		if (this.xmlNature.getTerminalId(parent.getId()).equals(terminalId)) {
			return true;
		}
		if (Nonterminal.class.isAssignableFrom(parent.getClass())) {
			Nonterminal parentN = (Nonterminal)parent;
			if (parentN.getChildNonterminals()!=null && !parentN.getChildNonterminals().isEmpty()) {
				for (Nonterminal child : parentN.getChildNonterminals()) {
					if (!checkedReuseN.contains(child)) {
						checkedReuseN.add(child);
						if (this.getChildrenContainTerminalId(child, terminalId, checkedReuseN)) {
							return true;
						}	
					}
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
	
	protected ImportAwareNonterminal processElement(ImportAwareNonterminal parentNonterminal, XSElementDecl element, List<String> processedTerminalQNs) throws MetamodelConsistencyException {	
		String terminalQN = this.createTerminalQN(element.getNamespace(), element.getName(), false);
		
		if (terminalIdNonterminalMap.containsKey(terminalQN)) {
			logger.debug("Reusing existing nonterminal for qn: " + terminalQN);
			
			ImportAwareNonterminal n = terminalIdNonterminalMap.get(terminalQN);
			
			addChildNonterminal(parentNonterminal, n);
			return n;
		}
		
		ImportAwareNonterminal n = this.createNonterminal(element.getNamespace(), element.getName(), false, element.getAbstract());
		XSElementDeclaration substDec = element.getSubstitutionGroupAffiliation();
		
		if (substDec!=null) {
			String extTerminalQN = this.createTerminalQN(substDec.getNamespace(), substDec.getName(), false);
			List<ImportAwareNonterminal> extensionNonterminals = extensionIdNonterminalMap.get(extTerminalQN);
			if (extensionNonterminals==null) {
				extensionNonterminals = new ArrayList<ImportAwareNonterminal>();
			}
			extensionNonterminals.add(n);
			extensionIdNonterminalMap.put(extTerminalQN, extensionNonterminals);
		}
		
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
	
	protected void processComplexElement(ImportAwareNonterminal nonterminal, XSElementDecl element, XSComplexTypeDefinition typeDef, List<String> processedTerminalQNs) throws MetamodelConsistencyException {
		XSObjectList attrList = typeDef.getAttributeUses();
		
		// Process attributes
		ImportAwareNonterminal attr;
		for (int i=0; i<attrList.getLength(); i++) {
			XSAttributeDeclaration attrDecl = ((XSAttributeUse)attrList.item(i)).getAttrDeclaration();
			if (attrDecl.getNamespace()!=null) {
				attr = this.createNonterminal(attrDecl.getNamespace(), attrDecl.getName(), true, false);
			} else {
				attr = this.createNonterminal(typeDef.getNamespace(), attrDecl.getName(), true, false);
			}
			addChildNonterminal(nonterminal, attr);
		}
		
		// Process content model
		if (typeDef.getContentType()==XSComplexTypeDefinition.CONTENTTYPE_ELEMENT || 
				typeDef.getContentType()==XSComplexTypeDefinition.CONTENTTYPE_MIXED) {
			this.processContentModel(nonterminal, typeDef.getParticle(), processedTerminalQNs);
		}
		
		
		
		// NOTE: Nothing to do for simple elements as attributes have already been processed
	}

	protected void processContentModel(ImportAwareNonterminal parentNonterminal, XSParticle particle, List<String> processedTerminalQNs) throws MetamodelConsistencyException {
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
	
	protected void addChildNonterminal(ImportAwareNonterminal parent, ImportAwareNonterminal child) {
		if (parent!=null) {
			if (parent.getChildNonterminals()==null) {
				parent.setChildNonterminals(new ArrayList<Nonterminal>());
			}
			child.setParentCount(child.getParentCount()+1);
			parent.getChildNonterminals().add(child);
		}
	}

	
	
	protected ImportAwareNonterminal createNonterminal(String terminalNamespace, String terminalName, boolean isAttribute, boolean isAbstract) throws MetamodelConsistencyException {
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
				
		ImportAwareNonterminal n = new ImportAwareNonterminal();
		n.setId(new ObjectId().toString());
		n.setName(this.createNonterminalName(terminalName));
		n.setAbstract(isAbstract);
		n.setTerminalQN(this.createTerminalQN(terminalNamespace, terminalName, isAttribute));
		
		this.xmlNature.mapNonterminal(n.getId(), terminalId);
		
		n.setEntityId(this.getSchema().getId());
		
		terminalIdNonterminalMap.put(terminalQN, n);
		
		return n;
	}
	
	protected String createTerminalQN(String terminalNamespace, String terminalName, boolean isAttribute) {
		return String.format("{%s}:%s%s", terminalNamespace, (isAttribute ? "#" : ""), terminalName);
	}	
}