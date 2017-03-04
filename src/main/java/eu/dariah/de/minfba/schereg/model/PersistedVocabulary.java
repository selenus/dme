package eu.dariah.de.minfba.schereg.model;

import de.unibamberg.minf.gtf.model.vocabulary.Vocabulary;
import de.unibamberg.minf.gtf.model.vocabulary.VocabularyItem;
import eu.dariah.de.minfba.core.metamodel.interfaces.Identifiable;

public class PersistedVocabulary extends Vocabulary implements Identifiable {
	private static final long serialVersionUID = 9212494681956653352L;
	
	private String description;

	
	public String getDescription() { return description; }
	public void setDescription(String description) { this.description = description; }
	
	@Override
	public Vocabulary compile() {
		if (this.getItems()!=null&&this.getItems().size()>0) {
			for (VocabularyItem vItem : this.getItems()) {
				vItem.compile();
			}
		}
		return this;
	}
}
