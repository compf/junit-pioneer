package org.junitpioneer.testkit;

import java.util.List;

public class NestedTestMethodWithParametersSelector {

	private final List<Class<?>> enclosingClasses;
	private final Class<?> testClass;
	private final String testMethodName;
	private final String methodParameterTypes;

	public NestedTestMethodWithParametersSelector(List<Class<?>> enclosingClasses, Class<?> testClass, String testMethodName, String methodParameterTypes) {
		this.enclosingClasses = enclosingClasses;
		this.testClass = testClass;
		this.testMethodName = testMethodName;
		this.methodParameterTypes = methodParameterTypes;
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

	public String getMethodParameterTypes() {
		return methodParameterTypes;
	}

}