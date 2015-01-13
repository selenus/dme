package de.dariah.base.model.base;

import java.util.List;

import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.JoinColumn;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public class ConfigurableSchemaElementImpl extends SchemaElementImpl {
	private static final long serialVersionUID = -8577524775797512106L;
	
	@Column(name="ns_uri")
	private String nsUri;
	
	@ElementCollection
	@CollectionTable(name="schema_element_analyzer", joinColumns=@JoinColumn(name="schema_element_id"))
	@Column(name="analyzer")
	private List<String> analyzers;
	
	@Column(name="process_source_links")
	private boolean processSourceLinks;
	
	@Column(name="process_geo_entities")
	private boolean processGeoData;
	
	@Column(name="use_for_title")
	private boolean useForTitle;
	
	@Column(name="use_for_topic_modelling")
	private boolean useForTopicModelling;
	
	public String getNsUri() { return nsUri; }
	public void setNsUri(String nsUri) { this.nsUri = nsUri; }
	
	public List<String> getAnalyzers() { return analyzers; }
	public void setAnalyzers(List<String> analyzers) { this.analyzers = analyzers; }
	
	public boolean isProcessSourceLinks() { return processSourceLinks; }
	public void setProcessSourceLinks(boolean processSourceLinks) {  this.processSourceLinks = processSourceLinks; }
	
	public boolean isProcessGeoData() { return processGeoData; }
	public void setProcessGeoData(boolean processGeoData) { this.processGeoData = processGeoData; }
	
	public boolean isUseForTitle() { return useForTitle; }
	public void setUseForTitle(boolean useForTitle) { this.useForTitle = useForTitle; }
	
	public boolean isUseForTopicModelling() { return useForTopicModelling; }
	public void setUseForTopicModelling(boolean useForTopicModelling) { this.useForTopicModelling = useForTopicModelling; }		
}
