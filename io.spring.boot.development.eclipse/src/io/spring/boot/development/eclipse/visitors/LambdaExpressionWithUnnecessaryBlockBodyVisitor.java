/*
 * Copyright 2016-2018 the original author or authors
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.spring.boot.development.eclipse.visitors;

import java.util.List;

import io.spring.boot.development.eclipse.Problem;
import io.spring.boot.development.eclipse.ProblemReporter;
import io.spring.boot.development.eclipse.StandardProblemReporter;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;

/**
 * An {@link ASTVisitor} that reports a warning when a lambda expression's
 * {@link LambdaExpression#getBody()} is a {@link Block} that contains a single
 * {@link Statement}.
 *
 * @author Andy Wilkinson
 */
class LambdaExpressionWithUnnecessaryBlockBodyVisitor extends ASTVisitor {

	private final ProblemReporter problemReporter;

	LambdaExpressionWithUnnecessaryBlockBodyVisitor(
			StandardProblemReporter problemReporter) {
		this.problemReporter = problemReporter;
	}

	@Override
	public boolean visit(LambdaExpression lambdaExpression) {
		ASTNode body = lambdaExpression.getBody();
		if (body instanceof Block) {
			List<?> statements = ((Block) body).statements();
			if (statements.size() == 1) {
				checkSingleStatement((ASTNode) statements.get(0));
			}
		}
		return true;
	}

	private void checkSingleStatement(ASTNode singleStatement) {
		if (isInstance(singleStatement, SwitchStatement.class, ThrowStatement.class)) {
			return;
		}
		BlockCountingVisitor blockCounter = new BlockCountingVisitor();
		singleStatement.accept(blockCounter);
		if (blockCounter.blocks == 0) {
			this.problemReporter.warning(
					Problem.LAMBDA_EXPRESSION_BODY_IS_SINGLE_STATEMENT_BLOCK,
					singleStatement);
		}
	}

	@SafeVarargs
	private final boolean isInstance(ASTNode node, Class<? extends ASTNode>... types) {
		for (Class<? extends ASTNode> type : types) {
			if (type.isInstance(node)) {
				return true;
			}
		}
		return false;
	}

	private static class BlockCountingVisitor extends ASTVisitor {

		private int blocks;

		@Override
		public boolean visit(Block node) {
			this.blocks++;
			return true;
		}

	}

}
