/*
 * Copyright 2016 the original author or authors
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
	 */
	void warning(Problem problem, ASTNode node);

	/**
	 * Reports an error for the given {@code problem}, associating it with the given
	 * {@code node}.
	 *
	 * @param problem the problem
	 * @param node the node
	 */
	void error(Problem problem, ASTNode node);

}
