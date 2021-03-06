/*
 * Copyright 2016-2018 the original author or authors
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.spring.boot.development.eclipse;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class StandardProblemReporter implements ProblemReporter {

	public static final String MARKER_TYPE = "io.spring.boot.development.eclipse.problem";

	private final IResource resource;

	public StandardProblemReporter(IResource resource) {
		this.resource = resource;
		try {
			this.resource.deleteMarkers(MARKER_TYPE, true, IResource.DEPTH_ZERO);
		}
		catch (CoreException ex) {
			throw new IllegalStateException(ex);
		}
	}

	@Override
	public void warning(Problem problem, ASTNode node, Object... args) {
		report(problem, IMarker.SEVERITY_WARNING, node, args);
	}

	@Override
	public void error(Problem problem, ASTNode node, Object... args) {
		report(problem, IMarker.SEVERITY_ERROR, node, args);
	}

	@Override
	public void warning(Problem problem, Object... args) {
		try {
			createMarker(problem, IMarker.SEVERITY_WARNING, args);
		}
		catch (CoreException ex) {
			throw new IllegalStateException(ex);
		}
	}

	private IMarker createMarker(Problem problem, int severity, Object... args)
			throws CoreException {
		IMarker marker = this.resource.createMarker(MARKER_TYPE);
		marker.setAttribute(IMarker.SEVERITY, severity);
		marker.setAttribute(IMarker.MESSAGE, problem.getMessage(args));
		marker.setAttribute(IMarker.SOURCE_ID, Integer.toString(problem.getId()));
		return marker;
	}

	private void report(Problem problem, int severity, ASTNode node, Object... args) {
		try {
			IMarker marker = createMarker(problem, severity, args);
			int startPosition = node.getStartPosition();
			marker.setAttribute(IMarker.CHAR_START, startPosition);
			marker.setAttribute(IMarker.CHAR_END, startPosition + node.getLength());
			marker.setAttribute(IMarker.LOCATION,
					"Line " + getCompilationUnit(node).getLineNumber(startPosition));
		}
		catch (CoreException ex) {
			throw new IllegalStateException(ex);
		}
	}

	private CompilationUnit getCompilationUnit(ASTNode node) {
		if (node.getRoot() instanceof CompilationUnit) {
			return (CompilationUnit) node.getRoot();
		}
		ASTNode candidate = node;
		while (candidate != null) {
			if (candidate instanceof CompilationUnit) {
				return (CompilationUnit) candidate;
			}
			candidate = candidate.getParent();
		}
		throw new IllegalStateException(
				"Node " + node + " was not a descendant of a CompilationUnit");
	}

}
