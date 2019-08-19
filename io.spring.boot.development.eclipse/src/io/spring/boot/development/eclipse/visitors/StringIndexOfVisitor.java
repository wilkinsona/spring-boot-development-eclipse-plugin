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
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.StringLiteral;

/**
 * An {@ASTVistor} that warns about calls to {@link String#indexOf(String)} and
 * {@link String#lastIndexOf(String)} with a single character string literal and
 * recommends the use of their {@code char} variants instead.
 *
 * @author Andy Wilkinson
 */
class StringIndexOfVisitor extends ASTVisitor {

	private final ProblemReporter problemReporter;

	StringIndexOfVisitor(ProblemReporter problemReporter) {
		this.problemReporter = problemReporter;
	}

	@Override
	public boolean visit(MethodInvocation methodInvocation) {
		IMethodBinding binding = methodInvocation.resolveMethodBinding();
		if (binding == null) {
			return true;
		}
		ITypeBinding declaringClass = binding.getDeclaringClass();
		if (declaringClass.getQualifiedName().equals(String.class.getName())
				&& (binding.getName().equals("indexOf")
						|| binding.getName().equals("lastIndexOf"))
				&& methodInvocation.arguments().size() == 1) {
			ASTNode argument = (ASTNode) methodInvocation.arguments().get(0);
			if (argument instanceof StringLiteral
					&& ((StringLiteral) argument).getLiteralValue().length() == 1) {
				this.problemReporter.warning(
						Problem.SINGLE_CHARACTER_STRING_LITERAL_INDEX_OF,
						methodInvocation.getName(), binding.getName(), binding.getName());
			}
		}
		return true;
	}

}
