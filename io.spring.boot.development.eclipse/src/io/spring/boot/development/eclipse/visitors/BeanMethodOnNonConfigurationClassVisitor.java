/*
 * Copyright 2016-2019 the original author or authors
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.spring.boot.development.eclipse.visitors;

import java.lang.reflect.Modifier;

import io.spring.boot.development.eclipse.Problem;
import io.spring.boot.development.eclipse.ProblemReporter;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

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
	public boolean visit(TypeDeclaration typeDeclaration) {
		ITypeBinding binding = typeDeclaration.resolveBinding();
		if (hasInheritedBeanMethod(binding) && !AstUtils.hasAnnotation(typeDeclaration,
				CONFIGURATION_ANNOTATION_NAME)) {
			this.problemReporter.warning(
					Problem.NON_CONFIGURATION_CLASS_HAS_INHERITED_BEAN_METHODS,
					typeDeclaration.getSuperclassType());
		}
		return true;
	}

	private boolean hasInheritedBeanMethod(ITypeBinding binding) {
		if (binding != null) {
			while ((binding = binding.getSuperclass()) != null) {
				IMethodBinding[] methods = binding.getDeclaredMethods();
				for (IMethodBinding method : methods) {
					IAnnotationBinding[] annotations = method.getAnnotations();
					for (IAnnotationBinding annotation : annotations) {
						if (BEAN_ANNOTATION_NAME.equals(
								annotation.getAnnotationType().getQualifiedName())) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	@Override
	public boolean visit(MethodDeclaration methodDeclaration) {
		Annotation beanAnnotation = AstUtils.findAnnotation(methodDeclaration,
				BEAN_ANNOTATION_NAME);
		if (beanAnnotation != null) {
			TypeDeclaration typeDeclaration = AstUtils.findAncestor(methodDeclaration,
					TypeDeclaration.class);
			if (typeDeclaration != null) {
				if (!Modifier.isAbstract(typeDeclaration.getModifiers()) && !AstUtils
						.hasAnnotation(typeDeclaration, CONFIGURATION_ANNOTATION_NAME)) {
					this.problemReporter.warning(
							Problem.BEAN_METHOD_ON_NON_CONFIGURATION_CLASS,
							beanAnnotation);
				}
			}
		}
		return true;
	}

}
