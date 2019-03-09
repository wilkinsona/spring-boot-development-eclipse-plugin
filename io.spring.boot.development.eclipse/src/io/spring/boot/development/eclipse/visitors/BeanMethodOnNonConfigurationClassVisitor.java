/*
 * Copyright 2016-2019 the original author or authors
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.spring.boot.development.eclipse.visitors;

import java.util.List;

import io.spring.boot.development.eclipse.Problem;
import io.spring.boot.development.eclipse.ProblemReporter;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;

/**
 * {@link ASTVisitor} that reports a warning when a {@code @Bean} method is declared on a
 * non-{@code @Configuration} class.
 *
 * @author Andy Wilkinson
 */
final class BeanMethodOnNonConfigurationClassVisitor extends ASTVisitor {

	private static final String BEAN_ANNOTATION_NAME = "org.springframework.context.annotation.Bean";

	private static final String CONFIGURATION_ANNOTATION_NAME = "org.springframework.context.annotation.Configuration";

	private final ProblemReporter problemReporter;

	BeanMethodOnNonConfigurationClassVisitor(ProblemReporter problemReporter) {
		this.problemReporter = problemReporter;
	}

	@Override
	public boolean visit(MethodDeclaration methodDeclaration) {
		Annotation beanAnnotation = findBeanAnnotation(methodDeclaration);
		if (beanAnnotation != null) {
			IMethodBinding binding = methodDeclaration.resolveBinding();
			if (binding != null) {
				if (!Modifier.isAbstract(binding.getDeclaringClass().getModifiers())
						&& findConfigurationAnnotation(
								binding.getDeclaringClass()) == null) {
					this.problemReporter.warning(
							Problem.BEAN_METHOD_ON_NON_CONFIGURATION_CLASS,
							beanAnnotation);
				}
			}
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	private Annotation findBeanAnnotation(MethodDeclaration methodDeclaration) {
		List<IExtendedModifier> modifiers = methodDeclaration.modifiers();
		for (IExtendedModifier modifier : modifiers) {
			if (modifier.isAnnotation()) {
				IAnnotationBinding annotationBinding = ((org.eclipse.jdt.core.dom.Annotation) modifier)
						.resolveAnnotationBinding();
				ITypeBinding annotationType = annotationBinding.getAnnotationType();
				if (BEAN_ANNOTATION_NAME.equals(annotationType.getQualifiedName())) {
					return (Annotation) modifier;
				}
			}
		}
		return null;
	}

	private IAnnotationBinding findConfigurationAnnotation(ITypeBinding typeBinding) {
		IAnnotationBinding[] annotations = typeBinding.getAnnotations();
		for (IAnnotationBinding annotation : annotations) {
			ITypeBinding annotationType = annotation.getAnnotationType();
			if (CONFIGURATION_ANNOTATION_NAME.equals(annotationType.getQualifiedName())) {
				return annotation;
			}
			if (!annotationType.getQualifiedName().startsWith("java.lang.")) {
				IAnnotationBinding metaAnnotation = findConfigurationAnnotation(
						annotationType);
				if (metaAnnotation != null) {
					return metaAnnotation;
				}
			}
		}
		return null;
	}

}
