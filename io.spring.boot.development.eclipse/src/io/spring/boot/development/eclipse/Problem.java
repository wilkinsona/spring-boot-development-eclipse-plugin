/*
 * Copyright 2016 the original author or authors
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.spring.boot.development.eclipse;

public enum Problem {

	CONFIGURATION_CLASS_CONSTRUCTOR_INJECTION(0,
			"@Configuration classes should use constructor injection"),

	AUTOWIRED_SINGLE_CONSTRUCTOR(1,
			"@Autowired is unnecessary as the class has a single constructor"),

	FAILURE_ANALYZER_NOT_IN_SPRING_FACTORIES(2,
			"Implements FailureAnalyzer but is not listed in spring.factories"),

	MAIN_CODE_COMPONENT(3, "@Component should not be used in main code"),

	MISSING_FUNCTIONAL_INTERFACE_ANNOTATION(4,
			"Functional interface is not annotated with @FuntionalInterface");

	private final int id;

	private final String message;

	Problem(int id, String message) {
		this.id = id;
		this.message = message;
	}

	public int getId() {
		return this.id;
	}

	public String getMessage() {
		return this.message;
	}

	public static Problem valueOf(int id) {
		for (Problem problem : Problem.values()) {
			if (problem.getId() == id) {
				return problem;
			}
		}
		throw new IllegalArgumentException("No problem with id '" + id + "' exists");
	}

}
