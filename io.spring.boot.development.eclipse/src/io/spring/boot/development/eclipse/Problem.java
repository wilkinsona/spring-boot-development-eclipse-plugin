/*
 * Copyright 2016-2019 the original author or authors
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
			"Functional interface is not annotated with @FuntionalInterface"),

	MISSING_PARENTHESES_AROUND_LAMBDA_PARAMETER(5,
			"Lambda parameter should be enclosed in parentheses"),

	LAMBDA_EXPRESSION_BODY_IS_SINGLE_STATEMENT_BLOCK(6,
			"Lambda expression with a single statement body does not require a block"),

	UNUSED_METHOD_PARAMETER(7, "Method parameter is unused"),

	INCOMPLETE_USE_OF_ASSERT_THAT(8, "Usage of assertThat is incomplete"),

	MISSING_PACKAGE_INFO(9, "Package does not contain a package-info.java file"),

	INCORRECT_ANNOTATION_LINK(10, "Link to annotation %s should use the text @%s"),

	ASSERTJ_EXCEPTION_ASSERTION_SUPPORT_NOT_USED(11,
			"Use AssertJ's exception assertion support instead");

	private final int id;

	private final String message;

	Problem(int id, String message) {
		this.id = id;
		this.message = message;
	}

	public int getId() {
		return this.id;
	}

	public String getMessage(Object... args) {
		if (args.length == 0) {
			return this.message;
		}
		return String.format(this.message, args);
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
