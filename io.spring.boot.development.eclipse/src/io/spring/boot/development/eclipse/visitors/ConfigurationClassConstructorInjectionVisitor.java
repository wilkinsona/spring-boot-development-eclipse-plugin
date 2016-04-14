/*
 * Copyright 2016 the original author or authors
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.spring.boot.development.eclipse.visitors;

import io.spring.boot.development.eclipse.Problem;
import io.spring.boot.development.eclipse.ProblemReporter;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

/**
 * An {@link ASTVisitor} that recommends the use of constructor injection rather than
 * field injection in {@code @Configuration} classes.
 *
 * @author Andy Wilkinson
 */
final class ConfigurationClassConstructorInjectionVisitor extends ASTVisitor {

	private final ProblemReporter problemReporter;

	ConfigurationClassConstructorInjectionVisitor(ProblemReporter problemReporter) {
		this.problemReporter = problemReporter;
	}

	@Override
	public boolean visit(TypeDeclaration type) {
		if (AstUtils.findAnnotation(type,
				"org.springframework.context.annotation.Configuration") != null) {
			analyzeFields(type.getFields());
		}
		return true;
	}

	private void analyzeFields(FieldDeclaration[] fields) {
		for (FieldDeclaration field : fields) {
			Annotation autowired = AstUtils.findAnnotation(field,
					"org.springframework.beans.factory.annotation.Autowired");
			if (autowired != null) {
				this.problemReporter.warning(
						Problem.CONFIGURATION_CLASS_CONSTRUCTOR_INJECTION, autowired);
			}
		}
	}

}
