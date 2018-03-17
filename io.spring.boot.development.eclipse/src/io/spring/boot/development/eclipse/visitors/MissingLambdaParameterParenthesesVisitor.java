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
		return true;
	}

}
