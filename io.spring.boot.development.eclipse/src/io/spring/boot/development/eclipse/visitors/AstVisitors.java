/*
 * Copyright 2016-2018 the original author or authors
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.spring.boot.development.eclipse.visitors;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import io.spring.boot.development.eclipse.StandardProblemReporter;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.dom.ASTVisitor;

public class AstVisitors implements Iterable<ASTVisitor> {

	private final List<ASTVisitor> visitors;

	public AstVisitors(IResource resource) {
		StandardProblemReporter problemReporter = new StandardProblemReporter(resource);
		this.visitors = Arrays.asList(
				new NoAutowiredWithSingleConstructorVisitor(problemReporter),
				new ConfigurationClassConstructorInjectionVisitor(problemReporter),
				new FailureAnalyzerSpringFactoriesVisitor(problemReporter,
						resource.getProject()),
				new NoComponentInMainCodeVisitor(problemReporter),
				new MissingFunctionalInterfaceVisitor(problemReporter),
				new MissingLambdaParameterParenthesesVisitor(problemReporter),
				new LambdaExpressionWithUnnecessaryBlockBodyVisitor(problemReporter),
				new UnusedMethodParameterVisitor(problemReporter),
				new IncompleteAssertThatVisitor(problemReporter));
	}

	@Override
	public Iterator<ASTVisitor> iterator() {
		return this.visitors.iterator();
	}

}
