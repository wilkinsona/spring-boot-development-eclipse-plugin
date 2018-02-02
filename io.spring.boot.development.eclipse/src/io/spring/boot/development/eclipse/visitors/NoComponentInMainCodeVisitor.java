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
import org.eclipse.jdt.core.dom.TypeDeclaration;

/**
 * An {@link ASTVisitor} that warns when {@code @Component} is used in main code.
 *
 * @author Andy Wilkinson
 */
class NoComponentInMainCodeVisitor extends ASTVisitor {

	private final ProblemReporter problemReporter;

	NoComponentInMainCodeVisitor(ProblemReporter problemReporter) {
		this.problemReporter = problemReporter;
	}

	@Override
	public boolean visit(TypeDeclaration typeDeclaration) {
		ITypeBinding binding = typeDeclaration.resolveBinding();
		if (binding != null && isInSpringBootPackage(binding)
				&& JavaElementUtils.isInSrcMainJava(binding.getJavaElement())
				&& isComponent(typeDeclaration)) {
			this.problemReporter.warning(Problem.MAIN_CODE_COMPONENT,
					typeDeclaration.getName());
		}
		return true;
	}

	private boolean isInSpringBootPackage(ITypeBinding binding) {
		return binding.getQualifiedName().startsWith("org.springframework.boot.");
	}

	private boolean isComponent(TypeDeclaration typeDeclaration) {
		return AstUtils.findAnnotation(typeDeclaration,
				"org.springframework.stereotype.Component") != null;

	}

}
