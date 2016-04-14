/*
 * Copyright 2016 the original author or authors
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.spring.boot.development.eclipse.ui;

import io.spring.boot.development.eclipse.Problem;
import io.spring.boot.development.eclipse.resolution.MarkerResolutions;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolutionGenerator;

public class SpringBootMarkerResolutionGenerator implements IMarkerResolutionGenerator {

	@Override
	public IMarkerResolution[] getResolutions(IMarker marker) {
		try {
			Object sourceId = marker.getAttribute(IMarker.SOURCE_ID);
			if (sourceId != null) {
				Problem problem = Problem.valueOf(Integer.parseInt((String) sourceId));
				IMarkerResolution resolutionForProblem = MarkerResolutions
						.resolutionForProblem(problem);
				if (resolutionForProblem != null) {
					return new IMarkerResolution[] { resolutionForProblem };
				}
			}
		}
		catch (CoreException ex) {
			ex.printStackTrace();
		}
		return new IMarkerResolution[0];
	}

}
