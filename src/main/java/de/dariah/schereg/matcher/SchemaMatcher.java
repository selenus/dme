package de.dariah.schereg.matcher;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import de.dariah.schereg.base.model.Mapping;
import de.dariah.schereg.base.model.MappingCell;
import de.dariah.schereg.base.model.MappingCellInput;
import de.dariah.schereg.base.model.Schema;
import de.dariah.schereg.base.service.MappingService;
import de.dariah.schereg.base.service.SchemaElementService;
import de.dariah.schereg.base.service.SchemaService;
import de.dariah.schereg.matcher.algorithms.EditDistanceMatcher;
import de.dariah.schereg.util.ContextService;
import de.dariah.schereg.util.ScheRegConstants;
import de.dariah.schereg.util.SchemaElementContainer;

@Component
@Scope(value="prototype")
@Transactional
public class SchemaMatcher {

	@Autowired private MappingService mappingSvc;
	@Autowired private SchemaService schemaSvc;
	@Autowired private SchemaElementService schemaElementSvc;
	
	private Double minEvidence;
	
	private Mapping mapping;

	public Mapping getMapping() { return mapping; }
	public void setMapping(Mapping mapping) { this.mapping = mapping; }

	public void match() {
		
		Schema source = schemaSvc.getSchema(mapping.getSourceId(), true);
		Schema target = schemaSvc.getSchema(mapping.getTargetId(), true);
		
		if (minEvidence == null) {
			minEvidence = Double.parseDouble(ContextService.getInstance().getPropertyValue("matching.minimum_evidence", "0.01"));
		}
		
		SchemaMatchingThread t = new SchemaMatchingThread(this);
		t.source = source;
		t.target = target;
		t.sourceElementContainer = schemaElementSvc.getSchemaElements(source.getId());
		t.targetElementContainer = schemaElementSvc.getSchemaElements(target.getId());
				
		t.start();
	}
	
	protected synchronized void persistResult(MatchResult result) {
		
		// TODO: Integration with other Matchers/manual matchings required
		ArrayList<MappingCell> mappings = new ArrayList<MappingCell>();
		DateTime timestamp = DateTime.now();
		
		Iterator<Integer> sourceIterator = result.associations.keySet().iterator();
		while(sourceIterator.hasNext()) {
		    Integer sourceID = sourceIterator.next();
		    HashMap<Integer, Match> targets = result.associations.get(sourceID);
		    Iterator<Integer> targetIterator = targets.keySet().iterator(); 
		    
		    while(targetIterator.hasNext()) {
			    Integer targetID = targetIterator.next(); 

			    Match match = targets.get(targetID);			    
				double positiveEvidence = match.getPositiveEvidence();
				double totalEvidence = match.getTotalEvidence();
				
				if (totalEvidence <= 0) {
			    	continue;
			    }

				// Calculate the match score
				double evidenceRatio = positiveEvidence / totalEvidence;
				double weightedEvidenceRatio = Math.pow(evidenceRatio, 0.5) * (Math.E - 1) + 1;
				double scaledPositiveEvidence = positiveEvidence * 0.5;
				double evidenceFactor = Math.pow((1 + scaledPositiveEvidence), (1 / scaledPositiveEvidence));
				double score = Math.log(weightedEvidenceRatio / evidenceFactor);
				
				// Stores the match score if positive			    
			    if (score < this.minEvidence || Double.isNaN(score)) {
			    	continue;
			    }
			    
			    MappingCell m = new MappingCell();		    
			    m.addMappingCellInput(new MappingCellInput(sourceID));
			    m.setOutput(targetID);
			    m.setScore(score);
			    m.setAuthor(this.getClass().getName());
			    m.setCreated(timestamp);
			    m.setModified(timestamp);
			    //m.setModificationDate(timestamp);
			    m.setMapping(this.mapping);

			    mappings.add(m);
			} 	
		} 
		
		mappingSvc.saveMappingCells(mappings);
		
		this.mapping = mappingSvc.getMapping(this.mapping.getId());
		this.mapping.setState(ScheRegConstants.STATE_OK);
		this.mapping.setIsLocked(false);
		mappingSvc.saveMapping(this.mapping);
	}
	
	private class SchemaMatchingThread extends Thread {
	
		public Schema source = null;
		public Schema target = null;
		public SchemaElementContainer sourceElementContainer = null;
		public SchemaElementContainer targetElementContainer = null;
	
		private SchemaMatcher owner = null;
		
		public SchemaMatchingThread (SchemaMatcher owner) {
			this.owner = owner;
		}
		
		@Override
		public void run() {
			executeMatching(source, target);
		}
	
		private void executeMatching(Schema source, Schema target) {
			EditDistanceMatcher m1 = new EditDistanceMatcher();
			m1.setSourceSchema(source);
			m1.setTargetSchema(target);
			m1.setSourceElementContainer(sourceElementContainer);
			m1.setTargetElementContainer(targetElementContainer);
			
			MatchResult result = m1.match();
			owner.persistResult(result);
		}
	}
}