/*
 * Copyright 2016-2018 the original author or authors
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.spring.boot.development.eclipse.visitors;

import io.spring.boot.development.eclipse.Problem;
import io.spring.boot.development.eclipse.ProblemReporter;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.TypeDeclaration;

/**
 * An {@link ASTVisitor} that reports a warning when a public or protected functional
 * interface is not annotated with {@link FunctionalInterface}.
 *
 * @author Andy Wilkinson
 */
class MissingFunctionalInterfaceVisitor extends ASTVisitor {

	private final ProblemReporter problemReporter;

	MissingFunctionalInterfaceVisitor(ProblemReporter problemReporter) {
		this.problemReporter = problemReporter;
	}

	@Override
	public boolean visit(TypeDeclaration type) {
		if (isMainCode(type) && isFunctionalInterface(type) && isPublicOrProtected(type)
				&& isNotAnnotatedWithFunctionalInterface(type)) {
			this.problemReporter.warning(Problem.MISSING_FUNCTIONAL_INTERFACE_ANNOTATION,
					type.getName());
		}
		return true;
	}

	private boolean isMainCode(TypeDeclaration type) {
		ITypeBinding binding = type.resolveBinding();
		return binding != null
				&& JavaElementUtils.isInSrcMainJava(binding.getJavaElement());
	}

	private boolean isPublicOrProtected(TypeDeclaration type) {
		return Modifier.isPublic(type.getModifiers())
				|| Modifier.isProtected(type.getModifiers());
	}

	private boolean isFunctionalInterface(TypeDeclaration type) {
		if (!type.isInterface()) {
			return false;
		}
		ITypeBinding binding = type.resolveBinding();
		return binding != null && binding.getFunctionalInterfaceMethod() != null;
	}

	private boolean isNotAnnotatedWithFunctionalInterface(TypeDeclaration type) {
		return AstUtils.findAnnotation(type, FunctionalInterface.class.getName()) == null;
	}

}
