/*
 * Copyright 2016-2019 the original author or authors
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.spring.boot.development.eclipse.visitors;

import io.spring.boot.development.eclipse.Problem;
import io.spring.boot.development.eclipse.ProblemReporter;
import io.spring.boot.development.eclipse.StandardProblemReporter;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

/**
 * {@link ASTVisitor} that reports an error for an invocation of an unproxied
 * {@code @Bean} method.
 *
 * @author Andy Wilkinson
 */
final class UnproxiedBeanMethodInvocationVisitor extends ASTVisitor {

	private static final String BEAN_ANNOTATION_NAME = "org.springframework.context.annotation.Bean";

	private static final String CONFIGURATION_ANNOTATION_NAME = "org.springframework.context.annotation.Configuration";

	private final ProblemReporter problemReporter;

	UnproxiedBeanMethodInvocationVisitor(StandardProblemReporter problemReporter) {
		this.problemReporter = problemReporter;
	}

	@Override
	public boolean visit(MethodInvocation methodInvocation) {
		IMethodBinding invokedMethod = methodInvocation.resolveMethodBinding();
		if (isBeanMethod(invokedMethod)) {
			IAnnotationBinding configurationAnnotation = findConfigurationAnnotation(
					invokedMethod.getDeclaringClass());
			if (!beanMethodsAreProxied(configurationAnnotation)) {
				this.problemReporter.error(Problem.INVOCATION_OF_UNPROXIED_BEAN_METHOD,
						methodInvocation);
			}
		}
		return true;
	}

	private boolean isBeanMethod(IMethodBinding methodBinding) {
		if (methodBinding == null) {
			return false;
		}
		IAnnotationBinding[] annotations = methodBinding.getAnnotations();
		for (IAnnotationBinding annotation : annotations) {
			ITypeBinding annotationType = annotation.getAnnotationType();
			if (BEAN_ANNOTATION_NAME.equals(annotationType.getQualifiedName())) {
				return true;
			}
		}
		return false;
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

	private boolean beanMethodsAreProxied(IAnnotationBinding configurationAnnotation) {
		if (configurationAnnotation == null) {
			return false;
		}
		for (IMemberValuePairBinding pair : configurationAnnotation
				.getAllMemberValuePairs()) {
			if ("proxyBeanMethods".equals(pair.getName())) {
				return (boolean) pair.getValue();
			}
		}
		return true;
	}

}
