package org.junitpioneer.testkit;

public class TestMethodSelector {

	private final Class<?> testClass;
	private final String testMethodName;

	public TestMethodSelector(Class<?> testClass, String testMethodName) {
		this.testClass = testClass;
		this.testMethodName = testMethodName;
	}

	public Class<?> getTestClass() {
		return testClass;
	}

	public String getTestMethodName() {
		return testMethodName;
	}

}