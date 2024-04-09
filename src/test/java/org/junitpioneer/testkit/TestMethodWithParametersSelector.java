package org.junitpioneer.testkit;

public class TestMethodWithParametersSelector {

	private final Class<?> testClass;
	private final String testMethodName;
	private final String methodParameterTypes;

	public TestMethodWithParametersSelector(Class<?> testClass, String testMethodName, String methodParameterTypes) {
		this.testClass = testClass;
		this.testMethodName = testMethodName;
		this.methodParameterTypes = methodParameterTypes;
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