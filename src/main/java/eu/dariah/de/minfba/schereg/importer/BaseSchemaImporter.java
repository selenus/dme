package eu.dariah.de.minfba.schereg.importer;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.dariah.de.dariahsp.model.web.AuthPojo;
import eu.dariah.de.minfba.core.metamodel.interfaces.Nonterminal;
import eu.dariah.de.minfba.core.metamodel.interfaces.Schema;

public abstract class BaseSchemaImporter implements SchemaImporter {
	protected final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private SchemaImportListener listener;
	
	private Schema schema;
	private String schemaFilePath;
	private String rootElementName; 
	private String rootElementType;
	private String elementId;
	
	private AuthPojo auth;
	
	private Nonterminal rootNonterminal;
	private List<Nonterminal> additionalRootElements;
	
	
	@Override public Schema getSchema() { return schema; }
	@Override public void setSchema(Schema schema) { this.schema = schema; }
	
	protected String getSchemaFilePath() { return schemaFilePath; }
	@Override public void setSchemaFilePath(String schemaFilePath) { this.schemaFilePath = schemaFilePath; }
 	
 	protected String getRootElementName() { return rootElementName; }
	@Override public void setRootElementName(String rootElementName) { this.rootElementName = rootElementName; }
	
	public String getRootElementType() { return rootElementType; }
	@Override public void setRootElementType(String rootElementType) { this.rootElementType = rootElementType; }
	
	public String getElementId() { return elementId; }
	@Override public void setElementId(String elementId) { this.elementId = elementId; }
	
	@Override public Nonterminal getRootNonterminal() { return rootNonterminal; }
	protected void setRootNonterminal(Nonterminal rootNonterminal) { this.rootNonterminal = rootNonterminal; }
	
	@Override public List<Nonterminal> getAdditionalRootElements() { return additionalRootElements; }
	protected void setAdditionalRootElements(List<Nonterminal> additionalRootElements) { this.additionalRootElements = additionalRootElements; }

	protected SchemaImportListener getListener() { return listener; }
	@Override public void setListener(SchemaImportListener listener) { this.listener = listener; }
	
	@Override public void setAuth(AuthPojo auth) { this.auth = auth; }
	@Override public AuthPojo getAuth() { return this.auth; }
	
	
	protected String createNonterminalName(String terminalName) {
		String name = terminalName.substring(0, 1).toUpperCase() + terminalName.substring(1);
		
		name = name.replaceAll("([^\\p{L}])([^\\p{L}\\p{N}-_.])*", "");
		
		return name; 
	}
}
