/*
 * Copyright 2016-2017 the original author or authors.
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

import io.spring.boot.development.eclipse.Problem;
import io.spring.boot.development.eclipse.ProblemReporter;
import io.spring.boot.development.eclipse.StandardProblemReporter;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.LambdaExpression;

/**
 * An {@link ASTVisitor} that reports a warning when an expression for a single-parameter
 * lambda does not wrap the parameter in parentheses.
 *
 * @author Andy Wilkinson
 */
class MissingLambdaParameterParenthesesVisitor extends ASTVisitor {

	private final ProblemReporter problemReporter;

	MissingLambdaParameterParenthesesVisitor(StandardProblemReporter problemReporter) {
		this.problemReporter = problemReporter;
	}

	@Override
	public boolean visit(LambdaExpression lambdaExpression) {
		if (!lambdaExpression.hasParentheses()) {
			this.problemReporter.warning(
					Problem.MISSING_PARENTHESES_AROUND_LAMBDA_PARAMETER,
					(ASTNode) lambdaExpression.parameters().get(0));
		}
		return false;
	}

}
