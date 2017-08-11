package eu.dariah.de.minfba.schereg.model;

import de.unibamberg.minf.dme.model.base.Identifiable;
import de.unibamberg.minf.gtf.model.vocabulary.VocabularyItem;
import de.unibamberg.minf.gtf.serialization.JsonSyntaxTreeDeserializer;

public class PersistedVocabularyItem extends VocabularyItem implements Identifiable {
	private static final long serialVersionUID = -8004767190967069592L;
	
	private String id;
	private String serializedData;
	
	
	public String getId() { return id; }
	public void setId(String id) { this.id = id; }
	
	public String getSerializedData() { return serializedData; }
	public void setSerializedData(String serializedData) { this.serializedData = serializedData; }
	
	@Override
	public void compile() {
		this.setData(JsonSyntaxTreeDeserializer.deserialize(this.getSerializedData()));
		super.compile();
	}
}
