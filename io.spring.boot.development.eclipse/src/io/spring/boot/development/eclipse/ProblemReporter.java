/*
 * Copyright 2016-2018 the original author or authors
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.spring.boot.development.eclipse;

import org.eclipse.jdt.core.dom.ASTNode;

/**
 * A {@code ProblemReporter} is used to report problems during code analysis
 *
 * @author Andy Wilkinson
 */
public interface ProblemReporter {

	/**
	 * Reports a warning for the given {@code problem}, associating it with the given
	 * {@code node}.
	 *
	 * @param problem the problem
	 * @param node the node
	 * @param args arguments to substitute into the problem's message
	 */
	void warning(Problem problem, ASTNode node, Object... args);

	/**
	 * Reports an error for the given {@code problem}, associating it with the given
	 * {@code node}.
	 *
	 * @param problem the problem
	 * @param node the node
	 * @param args arguments to substitute into the problem's message
	 */
	void error(Problem problem, ASTNode node, Object... args);

	/**
	 * Reports a warning for the given {@code problem} for the resource targetted by this
	 * {@code ProblemReporter}.
	 *
	 * @param problem the problem
	 * @param args arguments to substitute into the problem's message
	 */
	void warning(Problem problem, Object... args);

}
