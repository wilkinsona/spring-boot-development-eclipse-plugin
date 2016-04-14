/*
 * Copyright 2016 the original author or authors
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.spring.boot.development.eclipse;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;

/**
 * {@link IResourceDeltaVisitor} that calls an {@link IResourceVisitor} for every resource
 * that has changed or has been added.
 *
 * @author Andy Wilkinson
 */
final class SpringBootDeltaVisitor implements IResourceDeltaVisitor {

	private final IResourceVisitor delegate;

	SpringBootDeltaVisitor(IResourceVisitor delegate) {
		this.delegate = delegate;
	}

	@Override
	public boolean visit(IResourceDelta delta) throws CoreException {
		if (delta.getKind() == IResourceDelta.CHANGED
				|| delta.getKind() == IResourceDelta.ADDED) {
			IResource resource = delta.getResource();
			this.delegate.visit(resource);
		}
		return true;
	}

}