/*
 * Copyright 2016 the original author or authors
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.spring.boot.development.eclipse;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * {@link IncrementalProjectBuilder} that performs static analysis to identify code that
 * does not confirm to Spring Boot's conventions.
 *
 * @author Andy Wilkinson
 */
public final class SpringBootDevelopmentBuilder extends IncrementalProjectBuilder {

	public static final String BUILDER_ID = "io.spring.boot.development.eclipse.builder";

	private final List<IResourceVisitor> resourceVisitors = Arrays
			.asList(new JavaSourceCodeAnalyzer(), new MissingPackageInfoAnalyzer());

	@Override
	@SuppressWarnings("rawtypes")
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
			throws CoreException {
		if (kind == FULL_BUILD) {
			fullBuild(monitor);
		}
		else {
			IResourceDelta delta = getDelta(getProject());
			if (delta == null) {
				fullBuild(monitor);
			}
			else {
				incrementalBuild(delta, monitor);
			}
		}
		return null;
	}

	@Override
	protected void clean(IProgressMonitor monitor) throws CoreException {
		getProject().deleteMarkers(StandardProblemReporter.MARKER_TYPE, true,
				IResource.DEPTH_INFINITE);
	}

	private void fullBuild(IProgressMonitor monitor) throws CoreException {
		IProject project = getProject();
		for (IResourceVisitor resourceVisitor : this.resourceVisitors) {
			project.accept(resourceVisitor);
		}
	}

	private void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor)
			throws CoreException {
		for (IResourceVisitor resourceVisitor : this.resourceVisitors) {
			delta.accept(new SpringBootDeltaVisitor(resourceVisitor));
		}
	}

}
