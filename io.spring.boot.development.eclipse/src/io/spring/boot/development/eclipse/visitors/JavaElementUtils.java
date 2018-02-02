/*
 * Copyright 2016-2018 the original author or authors
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.spring.boot.development.eclipse.visitors;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragmentRoot;

/**
 * Utility methods for working with {@code IJavaElement}.
 *
 * @author Andy Wilkinson
 */
final class JavaElementUtils {

	private JavaElementUtils() {

	}

	/**
	 * Returns {@code true} if the given {@code javaElement} resides in
	 * {@code src/main/java}, otherwise {@code false}.
	 *
	 * @param javaElement the element to examine
	 * @return {@code true} if the element is in {@code src/main/java}, otherwise
	 * {@code false}.
	 */
	public static boolean isInSrcMainJava(IJavaElement javaElement) {
		while (javaElement != null) {
			if (javaElement instanceof IPackageFragmentRoot) {
				return ((IPackageFragmentRoot) javaElement).getPath().toFile()
						.getAbsolutePath().endsWith("/src/main/java");
			}
			javaElement = javaElement.getParent();
		}
		return false;
	}

}
