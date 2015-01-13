package de.dariah.schereg.matcher;

import java.util.HashMap;

public class MatchResult {

	HashMap<Integer, HashMap<Integer, Match>> associations = new HashMap<Integer, HashMap<Integer,Match>>();
	
	public void addMatch(int sourceId, int targetId, Match score) {
		
		if (!associations.containsKey(sourceId)) {
			associations.put(sourceId, new HashMap<Integer, Match>());
		}
		
		// No check required - there should not be the same target for a source calculated by the same Matcher
		associations.get(sourceId).put(targetId, score);
	}
}
