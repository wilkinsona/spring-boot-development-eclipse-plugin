/*
 * Copyright 2016 the original author or authors
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.spring.boot.development.eclipse.visitors;

import java.util.ArrayList;
import java.util.List;

import io.spring.boot.development.eclipse.Problem;
import io.spring.boot.development.eclipse.ProblemReporter;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

/**
 * An {@link ASTVisitor} that identifies the unnecessary use of {@code @Autowired} on
 * classes with a single constructor.
 *
 * @author Andy Wilkinson
 */
class NoAutowiredWithSingleConstructorVisitor extends ASTVisitor {

	private final ProblemReporter problemReporter;

	NoAutowiredWithSingleConstructorVisitor(ProblemReporter problemReporter) {
		this.problemReporter = problemReporter;
	}

	@Override
	public boolean visit(TypeDeclaration type) {
		List<MethodDeclaration> constructors = getConstructors(type);
		if (constructors.size() == 1) {
			analyzeAnnotations(constructors.iterator().next());
		}
		return true;
	}

	private void analyzeAnnotations(MethodDeclaration constructor) {
		Annotation autowired = AstUtils.findAnnotation(constructor,
				"org.springframework.beans.factory.annotation.Autowired");
		if (autowired != null) {
			this.problemReporter.warning(Problem.AUTOWIRED_SINGLE_CONSTRUCTOR, autowired);
		}
	}

	private List<MethodDeclaration> getConstructors(TypeDeclaration type) {
		List<MethodDeclaration> constructors = new ArrayList<MethodDeclaration>();
		for (MethodDeclaration method : type.getMethods()) {
			if (method.isConstructor()) {
				constructors.add(method);
			}
		}
		return constructors;
	}

}
