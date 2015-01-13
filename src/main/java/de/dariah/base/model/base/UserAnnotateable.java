package de.dariah.base.model.base;

import java.util.Deque;

import de.dariah.base.model.impl.UserAnnotation;

public interface UserAnnotateable {
	public Deque<UserAnnotation> getUserAnnotationsInSession();
	public Identifiable getAggregatorObject();
}
