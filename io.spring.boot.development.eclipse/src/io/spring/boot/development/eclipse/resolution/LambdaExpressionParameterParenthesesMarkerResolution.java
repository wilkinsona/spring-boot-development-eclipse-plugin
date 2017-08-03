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

package io.spring.boot.development.eclipse.resolution;

import io.spring.boot.development.eclipse.visitors.AstUtils;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.ui.IMarkerResolution;

/**
 * An {@link IMarkerResolution} that adds parentheses to a lambda expression's parameter.
 *
 * @author Andy Wilkinson
 */
class LambdaExpressionParameterParenthesesMarkerResolution
		extends CompilationUnitRewritingMarkerResolution {

	@Override
	public String getLabel() {
		return "Add parentheses";
	}

	@Override
	protected boolean resolveMarker(CompilationUnit compilationUnit, ASTNode markedNode) {
		LambdaExpression lambdaExpression = AstUtils.findAncestor(markedNode,
				LambdaExpression.class);
		if (lambdaExpression == null) {
			return false;
		}
		lambdaExpression.setParentheses(true);
		return true;
	}

}
