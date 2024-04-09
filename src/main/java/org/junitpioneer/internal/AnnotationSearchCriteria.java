package org.junitpioneer.internal;

public class AnnotationSearchCriteria {

	private final Class<? extends Annotation> annotationType;
	private final boolean findRepeated;
	private final boolean findAllEnclosing;

	public AnnotationSearchCriteria(Class<? extends Annotation> annotationType, boolean findRepeated, boolean findAllEnclosing) {
		this.annotationType = annotationType;
		this.findRepeated = findRepeated;
		this.findAllEnclosing = findAllEnclosing;
	}

	public Class<? extends Annotation> getAnnotationType() {
		return annotationType;
	}

	public boolean isFindRepeated() {
		return findRepeated;
	}

	public boolean isFindAllEnclosing() {
		return findAllEnclosing;
	}

}