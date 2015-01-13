package de.dariah.schereg.matcher;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import de.dariah.base.model.base.SchemaElement;
import de.dariah.schereg.util.ContextService;

public class EditDistanceMatcher extends Matcher
{
	// Scoring penalties
	public static final double PREFIX_PENALTY = -0.5;
	public static final double SUFFIX_PENALTY = -0.25;
	public static final double INSERT_PENALTY = -0.5;
	public static final double MISMATCH_PENALTY = -1;
	public static final double MAX_PENALTY = Math.min(PREFIX_PENALTY, Math.min(SUFFIX_PENALTY,Math.min(INSERT_PENALTY,MISMATCH_PENALTY / 2)));
	
	public static final double CHARS_PER_EVIDENCE = 4.0 * 2;

	//max value that this matcher can return.  A scaling factor.
	public static final double SCORE_CEILING = 8.0;
	
	// Scoring bonuses
	public static final double MATCH_BONUS = 1;

	/** Returns the name of the matcher */
	public String getName() { return "Name Similarity Matcher"; }

	/** Generates scores for the specified elements */
	public MatchResult match()
	{
		Hashtable<Class<? extends SchemaElement>, ArrayList<SchemaElement>> sourceElementMap = getSourceElementContainer().getClassLookupTable();
		Hashtable<Class<? extends SchemaElement>, ArrayList<SchemaElement>> targetElementMap = getTargetElementContainer().getClassLookupTable();

		// Sets the completed and total comparisons
		completedComparisons = 0;
		totalComparisons = sourceElementMap.size() * targetElementMap.size();
		
		// Generate the scores
		MatchResult result = new MatchResult();
		for (Class<? extends SchemaElement> sourceType : sourceElementMap.keySet()) {
			Collection<SchemaElement> sourceElements = sourceElementMap.get(sourceType);
						
			for (Class<? extends SchemaElement> targetType : targetElementMap.keySet()) {
				if (!isMappingAllowedForTypes(sourceType, targetType)) {
					continue;
				}
				
				Collection<SchemaElement> targetElements = targetElementMap.get(targetType);
				
				for (SchemaElement source : sourceElements) {
					for (SchemaElement target : targetElements) {
						Match score = matchElements(source, target);
						result.addMatch(source.getId(), target.getId(), score);
						//if(score != null) scores.setScore(sourceElement.getId(), targetElement.getId(), score);
					}
				}
				completedComparisons++;
			}
		}
		return result;
		
		
		
		/*MatcherScores scores = new MatcherScores(SCORE_CEILING);		
		for(SchemaElement sourceElement : sourceElements)
			for(SchemaElement targetElement : targetElements)
			{
				if(isAllowableMatch(sourceElement, targetElement))
					if(scores.getScore(sourceElement.getId(), targetElement.getId())==null)
					{
						MatcherScore score = matchElements(sourceElement, targetElement);
						if(score != null) scores.setScore(sourceElement.getId(), targetElement.getId(), score);
					}
				completedComparisons++;
			}
		return scores;*/
	}

	/** Matches a single pair of elements */
	private static Match matchElements(SchemaElement sourceElement, SchemaElement targetElement)
	{
		// Get character representations of the element names
		return matchStrings(sourceElement.getName(), targetElement.getName());
	}
	
	private static Match matchStrings(String sourceString, String targetString) {
				
		char[] source = sourceString == null ? new char[]{} : sourceString.toLowerCase().toCharArray();		
		char[] target = targetString == null ? new char[]{} : targetString.toLowerCase().toCharArray();
		
		// Generate the distance matrix
		double[][] distance = createDistanceMatrix(source.length, target.length);
		populateDistanceMatrix(distance, source, target);
		
		// Identify the edit distance score
		double editDistanceScore = distance[source.length][target.length];
		
//		if(editDistanceScore <= 0) return null;

		// Scale the result into the range (-1,+1)
//		double positive = (Math.min(source.length, target.length) + 1) * MATCH_BONUS;
//		double negative = -(source.length + target.length + 1) * MAX_PENALTY;
		
		// Determine the largest possible score for strings of this length.
		double positive = Math.min(source.length, target.length) * MATCH_BONUS;
		// Determine the smallest possible score for strings of this length
		// If there are no matches, the minimum distance will be purely a prefix and a suffix.
		double negative = Math.min(source.length, target.length) * PREFIX_PENALTY + Math.max(source.length, target.length) * SUFFIX_PENALTY;
		
		// Old (incorrect) calculation of the minimum possible score.
//		double negative = -(source.length + target.length) * MAX_PENALTY;
		
//		double reScaledEDScore = editDistanceScore+negative; //now in range (0,max_pos+max_neg).
//		double total_evidence = negative+positive; // should be max_pos+max_neg.
		
		// Shift into the range 0..TotalEvidence.
		double positiveEvidence = editDistanceScore - negative;
		double totalEvidence = positive - negative;
		
		// modify what this procedure returns to return a MatchScore object.
		return new Match(positiveEvidence / CHARS_PER_EVIDENCE, totalEvidence / CHARS_PER_EVIDENCE);				
	}
	
	public static void main(String[] args) {
		/*System.out.println("" + "," + "");
		MatcherScore result = matchStrings("", "");
		System.out.println(result.getPositiveEvidence());
		System.out.println(result.getTotalEvidence());
		System.out.println("Jolly" + "," + "Jolly");
		result = matchStrings("Jolly", "Jolly");
		System.out.println(result.getPositiveEvidence());
		System.out.println(result.getTotalEvidence());
		System.out.println("Jolly" + "," + "JollyRoger");
		result = matchStrings("Jolly", "JollyRoger");
		System.out.println(result.getPositiveEvidence());
		System.out.println(result.getTotalEvidence());
		System.out.println("ABCDE" + "," + "FGHIJ");
		result = matchStrings("ABCDE", "FGHIJ");
		System.out.println(result.getPositiveEvidence());
		System.out.println(result.getTotalEvidence());
		System.out.println("ABCDE" + "," + "FGHIJKLMNO");
		result = matchStrings("ABCDE", "FGHIJKLMNO");
		System.out.println(result.getPositiveEvidence());
		System.out.println(result.getTotalEvidence());
		System.out.println("ConditionViolationText" + "," + "EntityOrganization");
		result = matchStrings("ConditionViolationText", "EntityOrganization");
		System.out.println(result.getPositiveEvidence());
		System.out.println(result.getTotalEvidence());*/
	}

	/** Initializes a distance matrix */
	private static double[][] createDistanceMatrix(int sourceLength, int targetLength)
	{
		// Allocate space for the matrix.
		double[][] matrix = new double[sourceLength+1][];
		for(int i=0; i<=sourceLength; i++)
			matrix[i] = new double[targetLength+1];

		// Initialize the first row and column.
		for (int row=0; row<=sourceLength; row++)
			matrix[row][0] = row * PREFIX_PENALTY;
		for (int col=0; col<=targetLength; col++)
			matrix[0][col] = col * PREFIX_PENALTY;

		// Return the distance matrix
		return matrix;
	}

	/**
	 * Populates each cell with the maximum of a) The cell above/left + a match
	 * bonus/penalty, b) The cell left + an insertion/suffix penalty, c) The
	 * cell above + an insertion/suffix penalty.
	 */
	private static void populateDistanceMatrix(double[][] matrix, char[] source, char[] target)
	{
		for (int i = 0; i < source.length; i++)
		{
			for (int j = 0; j < target.length; j++)
			{
				double match = (source[i] == target[j]) ? MATCH_BONUS : MISMATCH_PENALTY;
				double a = matrix[i][j] + match;
				double left = (j < target.length - 1) ? INSERT_PENALTY : SUFFIX_PENALTY;
				double b = matrix[i][j + 1] + left;
				double up = (i < source.length - 1) ? INSERT_PENALTY : SUFFIX_PENALTY;
				double c = matrix[i + 1][j] + up;
				matrix[i + 1][j + 1] = Math.max(a, Math.max(b, c));
			}
		}
	}
}