/*
 * Copyright 2016 the original author or authors
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
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.TypeDeclaration;

/**
 * An {@link ASTVisitor} that checks that a {@code FailureAnalyzer} is listed in
 * {@code META-INF/spring.factories}.
 *
 * @author Andy Wilkinson
 */
class FailureAnalyzerSpringFactoriesVisitor extends ASTVisitor {

	private static final String CLASS_NAME_FAILURE_ANALYZER = "org.springframework.boot.diagnostics.FailureAnalyzer";

	private final ProblemReporter problemReporter;

	private final IProject project;

	FailureAnalyzerSpringFactoriesVisitor(ProblemReporter problemReporter,
			IProject project) {
		this.problemReporter = problemReporter;
		this.project = project;
	}

	@Override
	public boolean visit(TypeDeclaration type) {
		if (isFailureAnalyzer(type)
				&& JavaElementUtils
						.isInSrcMainJava(type.resolveBinding().getJavaElement())
				&& !isListedInSpringFactories(type)) {
			this.problemReporter.error(Problem.FAILURE_ANALYZER_NOT_IN_SPRING_FACTORIES,
					type.getName());
		}
		return true;
	}

	private boolean isListedInSpringFactories(TypeDeclaration type) {
		SpringFactories springFactories = SpringFactories.find(this.project);
		return springFactories != null && springFactories.get(CLASS_NAME_FAILURE_ANALYZER)
				.contains(type.resolveBinding().getQualifiedName());
	}

	private boolean isFailureAnalyzer(TypeDeclaration type) {
		return !type.isInterface() && !Modifier.isAbstract(type.getModifiers())
				&& AstUtils.getImplementedInterfaces(type)
						.contains(CLASS_NAME_FAILURE_ANALYZER);
	}

}
