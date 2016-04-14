/*
 * Copyright 2016 the original author or authors
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.spring.boot.development.eclipse.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Base class for {@link AbstractHandler AbstractHandlers} that configure a project's
 * natures.
 *
 * @author Andy Wilkinson
 */
abstract class AbstractNatureHandler extends AbstractHandler {

	@Override
	public final Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		for (IProject project : findProjects(selection)) {
			try {
				configureNature(project);
			}
			catch (CoreException ex) {
				throw new ExecutionException(
						"Failed to update Spring Boot development nature configuration",
						ex);
			}
		}
		return null;
	}

	private List<IProject> findProjects(ISelection selection) {
		List<IProject> projects = new ArrayList<IProject>();
		if (selection instanceof IStructuredSelection) {
			for (Object item : ((IStructuredSelection) selection).toList()) {
				IProject project = extractProjectIfPossible(item);
				if (project != null) {
					projects.add(project);
				}
			}
		}
		return projects;
	}

	private IProject extractProjectIfPossible(Object item) {
		if (item instanceof IProject) {
			return (IProject) item;
		}
		if (item instanceof IAdaptable) {
			return ((IAdaptable) item).getAdapter(IProject.class);
		}
		return null;
	}

	private void configureNature(IProject project) throws CoreException {
		IProjectDescription description = project.getDescription();
		List<String> natureIds = new ArrayList<String>(
				Arrays.asList(description.getNatureIds()));
		configureNatures(natureIds);
		description.setNatureIds(natureIds.toArray(new String[natureIds.size()]));
		project.setDescription(description, null);
	}

	protected abstract void configureNatures(List<String> natureIds) throws CoreException;

}
