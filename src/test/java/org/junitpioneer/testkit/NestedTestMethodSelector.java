package org.junitpioneer.testkit;

import java.util.List;

public class NestedTestMethodSelector {

	private final List<Class<?>> enclosingClasses;
	private final Class<?> testClass;
	private final String testMethodName;

	public NestedTestMethodSelector(List<Class<?>> enclosingClasses, Class<?> testClass, String testMethodName) {
		this.enclosingClasses = enclosingClasses;
		this.testClass = testClass;
		this.testMethodName = testMethodName;
	}

	public List<Class<?>> getEnclosingClasses() {
		return enclosingClasses;
	}

	public Class<?> getTestClass() {
		return testClass;
	}

	public String getTestMethodName() {
		return testMethodName;
	}

}