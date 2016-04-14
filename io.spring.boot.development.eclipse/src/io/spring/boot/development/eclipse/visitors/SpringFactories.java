/*
 * Copyright 2016 the original author or authors
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.spring.boot.development.eclipse.visitors;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;

/**
 * Programmatic representation of a project's {@code META-INF/spring.factories} file.
 *
 * @author Andy Wilkinson
 */
class SpringFactories {

	private final Properties properties;

	SpringFactories(Properties properties) {
		this.properties = properties;
	}

	static SpringFactories find(IProject project) {
		IJavaProject javaProject = JavaCore.create(project);
		try {
			for (IPackageFragmentRoot root : javaProject.getAllPackageFragmentRoots()) {
				if (root.getKind() == IPackageFragmentRoot.K_SOURCE) {
					IFile springFactories = project
							.getFile(root.getResource().getProjectRelativePath()
									.append("META-INF/spring.factories"));
					if (springFactories.exists()) {
						Properties properties = new Properties();
						properties.load(springFactories.getContents());
						return new SpringFactories(properties);
					}
				}
			}
		}
		catch (Exception ex) {
			throw new RuntimeException("Failure while finding spring.factories", ex);
		}
		return null;
	}

	List<String> get(String key) {
		String value = this.properties.getProperty(key);
		List<String> values = new ArrayList<String>();
		if (value != null) {
			StringTokenizer tokenizer = new StringTokenizer(value, ",");
			while (tokenizer.hasMoreTokens()) {
				values.add(tokenizer.nextToken().trim());
			}
		}
		return values;
	}

}
