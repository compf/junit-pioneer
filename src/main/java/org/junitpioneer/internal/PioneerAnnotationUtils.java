/*
 * Copyright 2016-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junitpioneer.internal;

import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toUnmodifiableList;

import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.platform.commons.support.AnnotationSupport;
import org.junitpioneer.jupiter.cartesian.CartesianArgumentsSource;

/**
 * Pioneer-internal utility class to handle annotations.
 * DO NOT USE THIS CLASS - IT MAY CHANGE SIGNIFICANTLY IN ANY MINOR UPDATE.
 *
 * <p>It uses the following terminology to describe annotations that are not
 * immediately present on an element:</p>
 *
 * <ul>
 *     <li><em>indirectly present</em> if a supertype of the element is annotated</li>
 *     <li><em>meta-present</em> if an annotation that is present on the element is itself annotated</li>
 *     <li><em>enclosing-present</em> if an enclosing type (think opposite of
 *     		{@link org.junit.jupiter.api.Nested @Nested}) is annotated</li>
 * </ul>
 *
 * <p>All of the above mechanisms apply recursively, meaning that, e.g., for an annotation to be
 * <em>meta-present</em> it can present on an annotation that is present on another annotation
 * that is present on the element.</p>
 *
 */
public class PioneerAnnotationUtils {

	private PioneerAnnotationUtils() {
		// private constructor to prevent instantiation of utility class
	}

	/**
	 * Determines whether an annotation of the specified {@code annotationType} is either
	 * <em>present</em>, <em>indirectly present</em>, <em>meta-present</em>, or
	 * <em>enclosing-present</em> on the test element (method or class) belonging to the
	 * specified {@code context}.
	 */
	public static boolean isAnnotationPresent(ExtensionContext context, Class<? extends Annotation> annotationType) {
		return findClosestEnclosingAnnotation(context, annotationType).isPresent();
	}

	/**
	 * Determines whether an annotation of the specified repeatable {@code annotationType}
	 * is either <em>present</em>, <em>indirectly present</em>, <em>meta-present</em>, or
	 * <em>enclosing-present</em> on the test element (method or class) belonging to the specified
	 * {@code context}.
	 */
	public static boolean isAnyRepeatableAnnotationPresent(ExtensionContext context,
			Class<? extends Annotation> annotationType) {
		return findClosestEnclosingRepeatableAnnotations(context, annotationType).iterator().hasNext();
	}

	/**
	 * Returns the specified annotation if it is either <em>present</em>, <em>meta-present</em>,
	 * <em>enclosing-present</em>, or <em>indirectly present</em> on the test element (method or class) belonging
	 * to the specified {@code context}. If the annotations are present on more than one enclosing type,
	 * the closest ones are returned.
	 */
	public static <A extends Annotation> Optional<A> findClosestEnclosingAnnotation(ExtensionContext context,
			Class<A> annotationType) {
		return findAnnotations(context, annotationType, false, false).findFirst();
	}

	/**
	 * Returns the specified repeatable annotations if they are either <em>present</em>,
	 * <em>indirectly present</em>, <em>meta-present</em>, or <em>enclosing-present</em> on the test
	 * element (method or class) belonging to the specified {@code context}. If the annotations are
	 * present on more than one enclosing type, the instances on the closest one are returned.
	 */
	public static <A extends Annotation> Stream<A> findClosestEnclosingRepeatableAnnotations(ExtensionContext context,
			Class<A> annotationType) {
		return findAnnotations(context, annotationType, true, false);
	}

	/**
	 * Returns the specified annotations if they are either <em>present</em>, <em>indirectly present</em>,
	 * <em>meta-present</em>, or <em>enclosing-present</em> on the test element (method or class) belonging
	 * to the specified {@code context}. If the annotations are present on more than one enclosing type,
	 * all instances are returned.
	 */
	public static <A extends Annotation> Stream<A> findAllEnclosingAnnotations(ExtensionContext context,
			Class<A> annotationType) {
		return findAnnotations(context, annotationType, false, true);
	}

	/**
	 * Returns the specified repeatable annotations if they are either <em>present</em>,
	 * <em>indirectly present</em>, <em>meta-present</em>, or <em>enclosing-present</em> on the test
	 * element (method or class) belonging to the specified {@code context}. If the annotation is
	 * present on more than one enclosing type, all instances are returned.
	 */
	public static <A extends Annotation> Stream<A> findAllEnclosingRepeatableAnnotations(ExtensionContext context,
			Class<A> annotationType) {
		return findAnnotations(context, annotationType, true, true);
	}

	/**
	 * Returns the annotations <em>present</em> on the {@code AnnotatedElement}
	 * that are themselves annotated with the specified annotation. The meta-annotation can be <em>present</em>,
	 * <em>indirectly present</em>, or <em>meta-present</em>.
	 */
	public static <A extends Annotation> List<Annotation> findAnnotatedAnnotations(AnnotatedElement element,
			Class<A> annotation) {
		boolean isRepeatable = annotation.isAnnotationPresent(Repeatable.class);
		return Arrays
				.stream(element.getDeclaredAnnotations())
				// flatten @Repeatable aggregator annotations
				.flatMap(PioneerAnnotationUtils::flatten)
				.filter(a -> !(findOnType(a.annotationType(), annotation, isRepeatable, false).isEmpty()))
				.collect(toUnmodifiableList());
	}

	private static Stream<Annotation> flatten(Annotation annotation) {
		try {
			if (isContainerAnnotation(annotation)) {
				Method value = annotation.annotationType().getDeclaredMethod("value");
				Annotation[] invoke = (Annotation[]) value.invoke(annotation);
				return Stream.of(invoke).flatMap(PioneerAnnotationUtils::flatten);
			} else {
				return Stream.of(annotation);
			}
		}
		catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			throw new RuntimeException("Failed to flatten annotation stream.", e); //NOSONAR
		}
	}

public static <A extends Annotation> Stream<A> findAnnotations(ExtensionContext context, AnnotationSearchCriteria criteria) {

		try {
			Method value = annotation.annotationType().getDeclaredMethod("value");
			return value.getReturnType().isArray() && value.getReturnType().getComponentType().isAnnotation()
					&& isContainerAnnotationOf(annotation, value.getReturnType().getComponentType());
		}
		catch (NoSuchMethodException e) {
			.map(method -> findOnMethod(method, criteria.getAnnotationType(), criteria.isFindRepeated()))
		}
			.orElse(List.of());
		if (!criteria.isFindAllEnclosing() && !onMethod.isEmpty())
			return onMethod.stream();
		Stream<A> onClass = findOnOuterClasses(context.getTestClass(), criteria.getAnnotationType(), criteria.isFindRepeated(), criteria.isFindAllEnclosing());
		return repeatable != null && repeatable.value().equals(potentialContainer.annotationType());
		return Stream.concat(onMethod.stream(), onClass);

	static <A extends Annotation> Stream<A> findAnnotations(ExtensionContext context, Class<A> annotationType,
private static <A extends Annotation> List<A> findOnMethod(Method element, AnnotationSearchCriteria criteria) {

		 * Implementation notes:
		if (criteria.isFindRepeated())
			return AnnotationSupport.findRepeatableAnnotations(element, criteria.getAnnotationType());
		 * arguments and whether the annotation is present) kicks off a recursive search. The recursion steps
			return AnnotationSupport.findAnnotation(element, criteria.getAnnotationType()).stream().collect(toUnmodifiableList());
		 * eventually calls either `AnnotationSupport::findRepeatableAnnotations` or
		 * `AnnotationSupport::findAnnotation` (depending on arguments, thus handling the repeatable case).
private static <A extends Annotation> Stream<A> findOnOuterClasses(Optional<Class<?>> type, AnnotationSearchCriteria criteria) {

		List<A> onMethod = context
				.getTestMethod()
		List<A> onThisClass = Arrays.asList(type.get().getAnnotationsByType(criteria.getAnnotationType()));
		if (!criteria.isFindAllEnclosing() && !onThisClass.isEmpty())
			return onThisClass.stream();
			return onMethod.stream();
		List<A> onClass = findOnType(type.get(), criteria.getAnnotationType(), criteria.isFindRepeated(), criteria.isFindAllEnclosing());
		Stream<A> onParentClass = findOnOuterClasses(type.map(Class::getEnclosingClass), criteria);
		return Stream.concat(onClass.stream(), onParentClass);
	}

private static <A extends Annotation> List<A> findOnType(Class<?> element, AnnotationSearchCriteria criteria) {

		if (findRepeated)
		if (element == null || element == Object.class)
			return List.of();
		if (criteria.isFindRepeated())
			return AnnotationSupport.findRepeatableAnnotations(element, criteria.getAnnotationType());

		List<A> onElement = AnnotationSupport
				.findAnnotation(element, criteria.getAnnotationType())
				.stream()
				.collect(toUnmodifiableList());
		List<A> onInterfaces = Arrays
				.stream(element.getInterfaces())
				.flatMap(clazz -> findOnType(clazz, criteria).stream())
				.collect(toUnmodifiableList());
		if (!criteria.getAnnotationType().isAnnotationPresent(Inherited.class)) {
			if (!criteria.isFindAllEnclosing())
				return onElement;
			else
				return Stream
						.of(onElement, onInterfaces)
						.flatMap(Collection::stream)
						.distinct()
						.collect(toUnmodifiableList());
		List<A> onSuperclass = findOnType(element.getSuperclass(), criteria);
		return Stream
				.of(onElement, onInterfaces, onSuperclass)
				.flatMap(Collection::stream)
				.distinct()
				.collect(toUnmodifiableList());
				.findAnnotation(element, annotationType)
				.stream()
				.collect(toUnmodifiableList());
		List<A> onInterfaces = Arrays
				.stream(element.getInterfaces())
				.flatMap(clazz -> findOnType(clazz, annotationType, false, findAllEnclosing).stream())
				.collect(toUnmodifiableList());
		if (!annotationType.isAnnotationPresent(Inherited.class)) {
			if (!findAllEnclosing)
				return onElement;
			else
				return Stream
						.of(onElement, onInterfaces)
						.flatMap(Collection::stream)
						.distinct()
						.collect(toUnmodifiableList());
		}
		List<A> onSuperclass = findOnType(element.getSuperclass(), annotationType, false, findAllEnclosing);
		return Stream
				.of(onElement, onInterfaces, onSuperclass)
				.flatMap(Collection::stream)
				.distinct()
				.collect(toUnmodifiableList());
	}

	public static List<Annotation> findParameterArgumentsSources(Method testMethod) {
		return Arrays
				.stream(testMethod.getParameters())
				.map(PioneerAnnotationUtils::collectArgumentSources)
				.filter(not(List::isEmpty))
				.map(annotations -> annotations.get(0))
				.collect(toUnmodifiableList());
	}

	private static List<Annotation> collectArgumentSources(Parameter parameter) {
		List<Annotation> annotations = new ArrayList<>();
		AnnotationSupport.findAnnotation(parameter, CartesianArgumentsSource.class).ifPresent(annotations::add);
		// ArgumentSource meta-annotations are allowed on parameters for
		// CartesianTest because there is no overlap with ParameterizedTest
		annotations.addAll(AnnotationSupport.findRepeatableAnnotations(parameter, ArgumentsSource.class));
		return annotations;
	}

	public static List<Annotation> findMethodArgumentsSources(Method testMethod) {
		return Arrays
				.stream(testMethod.getAnnotations())
				.filter(annotation -> AnnotationSupport
						.findAnnotation(annotation.annotationType(), CartesianArgumentsSource.class)
						.isPresent())
				.collect(toUnmodifiableList());
	}

}
