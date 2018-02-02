/*
 * Copyright 2016-2018 the original author or authors
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
