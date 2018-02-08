/*
 * Copyright 2016-2018 the original author or authors
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.spring.boot.development.eclipse.visitors;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.spring.boot.development.eclipse.Problem;
import io.spring.boot.development.eclipse.ProblemReporter;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;

/**
 * An {@link ASTVisitor} that detects incomplete invocations of AssertJ's
 * {@code Assertions.assertThat(…)} methods. An invocation is deemed to be incomplete if
 * no methods are chained to the result of the invocation.
 *
 * @author Andy Wilkinson
 */
public class IncompleteAssertThatVisitor extends ASTVisitor {

	private final ProblemReporter problemReporter;

	public IncompleteAssertThatVisitor(ProblemReporter problemReporter) {
		this.problemReporter = problemReporter;
	}

	@Override
	public boolean visit(ExpressionStatement expressionStatement) {
		MethodInvocationCollector collector = new MethodInvocationCollector();
		expressionStatement.getExpression().accept(collector);
		findAssertThatInvocation(collector.invocations).ifPresent((assertThat) -> {
			if (!MethodInvocation.class.isInstance(assertThat.getParent())) {
				this.problemReporter.error(Problem.INCOMPLETE_USE_OF_ASSERT_THAT,
						assertThat);
			}
		});
		return false;
	}

	private Optional<MethodInvocation> findAssertThatInvocation(
			List<MethodInvocation> invocations) {
		for (MethodInvocation invocation : invocations) {
			if (isAssertThatInvocation(invocation)) {
				return Optional.of(invocation);
			}
		}
		return Optional.empty();
	}

	private boolean isAssertThatInvocation(MethodInvocation methodInvocation) {
		IMethodBinding binding = methodInvocation.resolveMethodBinding();
		if (binding == null) {
			return false;
		}
		ITypeBinding declaringClass = binding.getDeclaringClass();
		return declaringClass != null
				&& "org.assertj.core.api.Assertions"
						.equals(declaringClass.getQualifiedName())
				&& "assertThat".equals(binding.getName());
	}

	private static final class MethodInvocationCollector extends ASTVisitor {

		private final List<MethodInvocation> invocations = new ArrayList<MethodInvocation>();

		@Override
		public boolean visit(MethodInvocation methodInvocation) {
			this.invocations.add(methodInvocation);
			return true;
		}

	}

}
