package org.junitpioneer.internal;

public class AnnotationSearchCriteria<A extends Annotation> {

	private final Class<A> annotationType;
	private final boolean findRepeated;
	private final boolean findAllEnclosing;

	public AnnotationSearchCriteria(Class<A> annotationType, boolean findRepeated, boolean findAllEnclosing) {
		this.annotationType = annotationType;
		this.findRepeated = findRepeated;
		this.findAllEnclosing = findAllEnclosing;
	}

	public Class<A> getAnnotationType() {
		return annotationType;
	}

	public boolean isFindRepeated() {
		return findRepeated;
	}

	public boolean isFindAllEnclosing() {
		return findAllEnclosing;
	}
}
