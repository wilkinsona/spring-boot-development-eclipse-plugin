/*
 * Copyright 2016 the original author or authors
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.spring.boot.development.eclipse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 * The Spring Boot Development {@link IProjectNature project nature}.
 *
 * @author Andy Wilkinson
 */
public final class SpringBootDevelopmentNature implements IProjectNature {

	public static final String NATURE_ID = "io.spring.boot.development.eclipse.nature";

	private IProject project;

	@Override
	public IProject getProject() {
		return this.project;
	}

	@Override
	public void setProject(IProject project) {
		this.project = project;
	}

	@Override
	public void configure() throws CoreException {
		if (hasBuilder()) {
			return;
		}
		addBuilder();
	}

	@Override
	public void deconfigure() throws CoreException {
		removeBuilder();
		deleteMarkers();
	}

	private boolean hasBuilder() throws CoreException {
		for (ICommand command : this.project.getDescription().getBuildSpec()) {
			if (SpringBootDevelopmentBuilder.BUILDER_ID
					.equals(command.getBuilderName())) {
				return true;
			}
		}
		return false;
	}

	private void addBuilder() throws CoreException {
		IProjectDescription description = this.project.getDescription();
		List<ICommand> commands = new ArrayList<ICommand>(
				Arrays.asList(description.getBuildSpec()));
		ICommand command = description.newCommand();
		command.setBuilderName(SpringBootDevelopmentBuilder.BUILDER_ID);
		commands.add(command);
		configureBuildSpec(commands);
	}

	private void removeBuilder() throws CoreException {
		List<ICommand> commands = new ArrayList<ICommand>();
		for (ICommand candidate : this.project.getDescription().getBuildSpec()) {
			if (!candidate.getBuilderName()
					.equals(SpringBootDevelopmentBuilder.BUILDER_ID)) {
				commands.add(candidate);
			}
		}
		configureBuildSpec(commands);
	}

	private void configureBuildSpec(List<ICommand> commands) throws CoreException {
		IProjectDescription description = this.project.getDescription();
		description.setBuildSpec(commands.toArray(new ICommand[commands.size()]));
		this.project.setDescription(description, null);
	}

	private void deleteMarkers() throws CoreException {
		this.project.deleteMarkers(StandardProblemReporter.MARKER_TYPE, true,
				IResource.DEPTH_INFINITE);
	}

}
