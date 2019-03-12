/*
 * Copyright 2016-2019 the original author or authors
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.spring.boot.development.eclipse.visitors;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import io.spring.boot.development.eclipse.Problem;
import io.spring.boot.development.eclipse.ProblemReporter;
import io.spring.boot.development.eclipse.StandardProblemReporter;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TypeDeclaration;

/**
 * {@link ASTVisitor} that reports problems related to proxying of {@code @Bean} methods.
 *
 * @author Andy Wilkinson
 */
final class BeanMethodProxyingVisitor extends ASTVisitor {

	private static final Set<String> CONFIGURATION_ANNOTATION_NAMES = new HashSet<>(
			Arrays.asList("org.springframework.context.annotation.Configuration",
					"org.springframework.boot.actuate.autoconfigure.web.ManagementContextConfiguration"));

	private static final String BEAN_ANNOTATION_NAME = "org.springframework.context.annotation.Bean";

	private final ProblemReporter problemReporter;

	BeanMethodProxyingVisitor(StandardProblemReporter problemReporter) {
		this.problemReporter = problemReporter;
	}

	@Override
	public boolean visit(TypeDeclaration typeDeclaration) {
		ITypeBinding binding = typeDeclaration.resolveBinding();
		if (binding != null) {
			if (beanMethodsAreProxied(binding.getSuperclass())) {
				Annotation beanMethodProxyingAnnotation = findBeanMethodProxyingAnnotation(
						typeDeclaration);
				if (beanMethodProxyingAnnotation != null
						&& !beanMethodsAreProxied(beanMethodProxyingAnnotation)) {
					this.problemReporter.error(Problem.MISMATCHED_BEAN_METHOD_PROXYING,
							beanMethodProxyingAnnotation);
				}
			}
		}
		return true;
	}

	@Override
	public boolean visit(MethodInvocation methodInvocation) {
		IMethodBinding invokedMethod = methodInvocation.resolveMethodBinding();
		if (isBeanMethod(invokedMethod)) {
			if (!beanMethodsAreProxied(invokedMethod.getDeclaringClass())) {
				this.problemReporter.error(Problem.INVOCATION_OF_UNPROXIED_BEAN_METHOD,
						methodInvocation);
			}
		}
		return true;
	}

	private Annotation findBeanMethodProxyingAnnotation(TypeDeclaration typeDeclaration) {
		for (String name : CONFIGURATION_ANNOTATION_NAMES) {
			Annotation annotation = AstUtils.findAnnotation(typeDeclaration, name);
			if (annotation != null) {
				return annotation;
			}
		}
		return null;
	}

	private boolean beanMethodsAreProxied(ITypeBinding typeBinding) {
		if (typeBinding == null) {
			return false;
		}
		for (IAnnotationBinding annotation : typeBinding.getAnnotations()) {
			for (String name : CONFIGURATION_ANNOTATION_NAMES) {
				if (name.equals(annotation.getAnnotationType().getQualifiedName())) {
					if (beanMethodsAreProxied(annotation)) {
						return true;
					}
				}
			}
		}
		return beanMethodsAreProxied(typeBinding.getSuperclass());
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

	private boolean beanMethodsAreProxied(Annotation configurationAnnotation) {
		if (configurationAnnotation == null) {
			return false;
		}
		return beanMethodsAreProxied(configurationAnnotation.resolveAnnotationBinding());
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

}
