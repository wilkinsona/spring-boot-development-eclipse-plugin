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
		return false;
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
