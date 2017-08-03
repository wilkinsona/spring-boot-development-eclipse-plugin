/*
 * Copyright 2016-2017 the original author or authors
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.spring.boot.development.eclipse.resolution;

import java.util.HashMap;
import java.util.Map;

import io.spring.boot.development.eclipse.Problem;
import org.eclipse.ui.IMarkerResolution;

public class MarkerResolutions {

	private static Map<Problem, IMarkerResolution> resolutions;

	static {
		resolutions = new HashMap<Problem, IMarkerResolution>();
		resolutions.put(Problem.CONFIGURATION_CLASS_CONSTRUCTOR_INJECTION,
				new ConfigurationClassConstructorInjectionMarkerResolution());
		resolutions.put(Problem.MISSING_PARENTHESES_AROUND_LAMBDA_PARAMETER,
				new LambdaExpressionParameterParenthesesMarkerResolution());
	}

	public static IMarkerResolution resolutionForProblem(Problem problem) {
		return resolutions.get(problem);
	}

}
