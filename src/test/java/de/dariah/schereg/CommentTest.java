package de.dariah.schereg;

import java.util.Deque;
import java.util.LinkedList;

import junit.framework.Assert;

import org.junit.Test;

public class CommentTest {
	@Test
	public void testDeque() {
		Deque<Integer> testDeque = new LinkedList<Integer>();
		
		testDeque.add(555);
		testDeque.add(234);
		testDeque.add(752);
		testDeque.add(354);
		
		Assert.assertTrue(testDeque.getFirst().intValue()==555);
		
		Assert.assertTrue(testDeque.getLast().intValue()==354);
	}
}
