package de.dariah.schereg;

import junit.framework.Assert;

import org.junit.Test;


import de.dariah.schereg.base.model.Annotation;
import de.dariah.schereg.base.model.Entity;

public class AnnotationTest {

	@Test
	public void testAnnotationHashAndEquals() {
		
		Annotation a1 = new Annotation();
		Annotation a2 = new Annotation();
		doAssertEqualsAndHash(a1, a2, true);
		
		a1.setElement(new Entity());
		doAssertEqualsAndHash(a1, a2, false);
		
		a2.setElement(new Entity());
		doAssertEqualsAndHash(a1, a2, false);
		
		a1.setElement(a2.getElement());
		doAssertEqualsAndHash(a1, a2, true);
		
		a1.setGroup(new Entity());
		doAssertEqualsAndHash(a1, a2, false);
		
		a2.setGroup(new Entity());
		doAssertEqualsAndHash(a1, a2, false);
		
		a2.setGroup(a1.getGroup());
		doAssertEqualsAndHash(a1, a2, true);
		
	}
	
	private void doAssertEqualsAndHash(Object lhs, Object rhs, boolean goal) {
		Assert.assertTrue(lhs.equals(rhs) == goal);
		Assert.assertTrue(rhs.equals(lhs) == goal);
		Assert.assertTrue((lhs.hashCode()==rhs.hashCode())==goal);
	}
	
}
