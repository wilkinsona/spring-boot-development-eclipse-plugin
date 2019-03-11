/*
 * Copyright 2016-2019 the original author or authors
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.spring.boot.development.eclipse.visitors;

import java.util.Stack;
import java.util.function.Consumer;

import io.spring.boot.development.eclipse.Problem;
import io.spring.boot.development.eclipse.ProblemReporter;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;

/**
 * An {@link ASTVisitor} that recommends the use of AssertJ's exception assert support is
 * used rather than {@code fail()} in a try-block and {@code assertThat()} in a
 * catch-block.
 *
 * @author Andy Wilkinson
 */
class PreferAssertJExceptionAssertionVisitor extends ASTVisitor {

	private Stack<Consumer<MethodInvocation>> methodInvocationHandler = new Stack<>();

	private final ProblemReporter problemReporter;

	PreferAssertJExceptionAssertionVisitor(ProblemReporter problemReporter) {
		this.problemReporter = problemReporter;
	}

	@Override
	public boolean visit(TryStatement tryStatement) {
		this.methodInvocationHandler.push(this::checkForAssertFail);
		return true;
	}

	@Override
	public void endVisit(TryStatement tryStatement) {
		this.methodInvocationHandler.pop();
	}

	@Override
	public boolean visit(CatchClause node) {
		this.methodInvocationHandler.push(this::checkForAssertionsAssertThat);
		return true;
	}

	@Override
	public void endVisit(CatchClause node) {
		this.methodInvocationHandler.pop();
	}

	@Override
	public boolean visit(MethodInvocation methodInvocation) {
		if (!this.methodInvocationHandler.isEmpty()) {
			this.methodInvocationHandler.peek().accept(methodInvocation);
		}
		return true;
	}

	private void checkForAssertFail(MethodInvocation methodInvocation) {
		checkForUnwantedInvocation(methodInvocation, "org.junit.Assert", "fail");
	}

	private void checkForAssertionsAssertThat(MethodInvocation methodInvocation) {
		checkForUnwantedInvocation(methodInvocation, "org.assertj.core.api.Assertions",
				"assertThat");
	}

	private void checkForUnwantedInvocation(MethodInvocation methodInvocation,
			String declaringClassName, String methodName) {
		IMethodBinding binding = methodInvocation.resolveMethodBinding();
		if (binding != null) {
			MethodDeclaration containingMethod = AstUtils.findAncestor(methodInvocation,
					MethodDeclaration.class);
			if (containingMethod == null) {
				return;
			}
			Type returnType = containingMethod.getReturnType2();
			if (returnType != null) {
				ITypeBinding returnTypeBinding = returnType.resolveBinding();
				if (returnTypeBinding != null) {
					ITypeBinding exception = returnType.getAST()
							.resolveWellKnownType("java.lang.Exception");
					if (exception != null
							&& exception.isCastCompatible(returnTypeBinding)) {
						return;
					}
				}
			}
			IMethodBinding methodDeclaration = binding.getMethodDeclaration();
			if (methodDeclaration.getDeclaringClass().getQualifiedName()
					.equals(declaringClassName)
					&& methodDeclaration.getName().equals(methodName)) {
				PreferAssertJExceptionAssertionVisitor.this.problemReporter.warning(
						Problem.ASSERTJ_EXCEPTION_ASSERTION_SUPPORT_NOT_USED,
						methodInvocation);
			}
		}
	}

}
