package org.junitpioneer.testkit;

import java.util.List;

public class NestedTestClassSelector {

	private final List<Class<?>> enclosingClasses;
	private final Class<?> testClass;

	public NestedTestClassSelector(List<Class<?>> enclosingClasses, Class<?> testClass) {
		this.enclosingClasses = enclosingClasses;
		this.testClass = testClass;
	}

	public List<Class<?>> getEnclosingClasses() {
		return enclosingClasses;
	}

	public Class<?> getTestClass() {
		return testClass;
	}

}