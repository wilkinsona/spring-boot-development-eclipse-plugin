/*
 * Copyright 2016-2019 the original author or authors
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.spring.boot.development.eclipse;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragmentRoot;

/**
 * Utility methods for working with {@code IJavaElement}.
 *
 * @author Andy Wilkinson
 */
public final class JavaElementUtils {

	private static final Path SRC_MAIN_JAVA = new Path("src/main/java");

	private static final Path SRC_IT_JAVA = new Path("src/it/java");

	private static final Path SRC_TEST_JAVA = new Path("src/test/java");

	private JavaElementUtils() {

	}

	/**
	 * Returns {@code true} if the given {@code javaElement} is main code, otherwise
	 * {@code false}. Code is considered to be main code if it resides in
	 * {@code src/main/java}.
	 *
	 * @param javaElement the element to examine
	 * @return {@code true} if the element is in main code, otherwise {@code false}.
	 */
	public static boolean isMainCode(IJavaElement javaElement) {
		IPath relativePath = findPackageRootRelativePath(javaElement);
		return (relativePath != null) ? SRC_MAIN_JAVA.equals(relativePath) : false;
	}

	/**
	 * Returns {@code true} if the given {@code javaElement} is test code, otherwise
	 * {@code false}. Code is considered to be test code if it resides in
	 * {@code src/it/java} or {@code src/main/java}.
	 *
	 * @param javaElement the element to examine
	 * @return {@code true} if the element is in test code, otherwise {@code false}.
	 */
	public static boolean isTestCode(IJavaElement javaElement) {
		IPath relativePath = findPackageRootRelativePath(javaElement);
		return (relativePath != null)
				? SRC_IT_JAVA.equals(relativePath) || SRC_TEST_JAVA.equals(relativePath)
				: false;
	}

	private static IPath findPackageRootRelativePath(IJavaElement javaElement) {
		IPackageFragmentRoot packageRoot = findPackageRoot(javaElement);
		return (packageRoot != null) ? packageRoot.getPath().makeRelativeTo(
				packageRoot.getJavaProject().getProject().getFullPath()) : null;
	}

	private static IPackageFragmentRoot findPackageRoot(IJavaElement javaElement) {
		while (javaElement != null) {
			if (javaElement instanceof IPackageFragmentRoot) {
				return (IPackageFragmentRoot) javaElement;
			}
			javaElement = javaElement.getParent();
		}
		return null;
	}

}
