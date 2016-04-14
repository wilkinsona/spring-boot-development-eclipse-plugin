/*
 * Copyright 2016 the original author or authors
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.spring.boot.development.eclipse.ui;

import java.util.List;

import io.spring.boot.development.eclipse.SpringBootDevelopmentNature;
import org.eclipse.core.runtime.CoreException;

/**
 * {@link AbstractNatureHandler} for disabling the Spring Boot development nature.
 *
 * @author Andy Wilkinson
 */
public class DisableSpringBootDevelopmentNatureHandler extends AbstractNatureHandler {

	@Override
	protected void configureNatures(List<String> natureIds) throws CoreException {
		natureIds.remove(SpringBootDevelopmentNature.NATURE_ID);
	}

}