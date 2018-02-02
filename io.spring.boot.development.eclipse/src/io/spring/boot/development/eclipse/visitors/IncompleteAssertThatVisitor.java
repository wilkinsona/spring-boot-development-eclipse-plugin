/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
		return binding != null
				&& "org.assertj.core.api.Assertions"
						.equals(binding.getDeclaringClass().getQualifiedName())
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
