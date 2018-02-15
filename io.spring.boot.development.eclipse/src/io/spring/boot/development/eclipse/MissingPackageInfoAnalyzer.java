/*
 * Copyright 2016-2018 the original author or authors
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.spring.boot.development.eclipse;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

/**
 * An {@link IResourceVisitor} that warns about missing {@code package-info.java} files.
 *
 * @author Andy Wilkinson
 */
class MissingPackageInfoAnalyzer implements IResourceVisitor {

	private static final Set<String> IGNORED_PROJECT_NAMES = Collections
			.unmodifiableSet(new HashSet<>(Arrays.asList("spring-boot-maven-plugin",
					"spring-boot-docs", "spring-boot-configuration-processor")));

	private static final Set<String> IGNORED_PROJECT_NAME_PREFIXES = Collections
			.unmodifiableSet(new HashSet<>(Arrays.asList("spring-boot-sample-")));

	@Override
	public boolean visit(IResource resource) throws CoreException {
		if (resource instanceof IFolder) {
			visitFolder(resource);
		}
		return true;
	}

	private void visitFolder(IResource resource) throws JavaModelException {
		IJavaElement javaElement = JavaCore.create(resource);
		if (!(javaElement instanceof IPackageFragment)) {
			return;
		}
		IPackageFragment packageFragment = (IPackageFragment) javaElement;
		if (!isInterestingProject(packageFragment.getJavaProject())) {
			return;
		}
		ProblemReporter problemReporter = new StandardProblemReporter(resource);
		if (JavaElementUtils.isInSrcMainJava(packageFragment)
				&& containsPublicOrProtectedJavaResources(packageFragment)) {
			if (!Stream.of(packageFragment.getChildren())
					.map((element) -> element.getPath().lastSegment())
					.anyMatch("package-info.java"::equals)) {
				problemReporter.warning(Problem.MISSING_PACKAGE_INFO);
			}
		}
	}

	private boolean isInterestingProject(IJavaProject project) {
		String projectName = project.getElementName();
		for (String ignoredProjectName : IGNORED_PROJECT_NAMES) {
			if (ignoredProjectName.equals(projectName)) {
				return false;
			}
		}
		for (String ignoreProjectNamePrefix : IGNORED_PROJECT_NAME_PREFIXES) {
			if (projectName.startsWith(ignoreProjectNamePrefix)) {
				return false;
			}
		}
		return true;
	}

	private boolean containsPublicOrProtectedJavaResources(
			IPackageFragment packageFragment) throws JavaModelException {
		if (!packageFragment.containsJavaResources()) {
			return false;
		}
		for (IJavaElement child : packageFragment.getChildren()) {
			if (child instanceof ICompilationUnit) {
				for (IType type : ((ICompilationUnit) child).getTypes()) {
					if (Flags.isPublic(type.getFlags())
							|| Flags.isProtected(type.getFlags())) {
						return true;
					}
				}
			}
		}
		return false;
	}

}
