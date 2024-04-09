package org.junitpioneer.testkit;

public class TestClassSelector {

	private final Class<?> testClass;

	public TestClassSelector(Class<?> testClass) {
		this.testClass = testClass;
	}

	public Class<?> getTestClass() {
		return testClass;
	}

}