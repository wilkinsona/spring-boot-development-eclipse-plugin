/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.spring.boot.development.eclipse.visitors;

import io.spring.boot.development.eclipse.Problem;
import io.spring.boot.development.eclipse.ProblemReporter;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.TypeDeclaration;

/**
 * An {@link ASTVisitor} that warns when {@code @Component} is used in main code.
 *
 * @author Andy Wilkinson
 */
class NoComponentInMainCodeVisitor extends ASTVisitor {

	private final ProblemReporter problemReporter;

	NoComponentInMainCodeVisitor(ProblemReporter problemReporter) {
		this.problemReporter = problemReporter;
	}

	@Override
	public boolean visit(TypeDeclaration typeDeclaration) {
		if (isInSpringBootPackage(typeDeclaration)
				&& JavaElementUtils.isInSrcMainJava(
						typeDeclaration.resolveBinding().getJavaElement())
				&& isComponent(typeDeclaration)) {
			this.problemReporter.warning(Problem.MAIN_CODE_COMPONENT, typeDeclaration);
		}
		return true;
	}

	private boolean isInSpringBootPackage(TypeDeclaration typeDeclaration) {
		return typeDeclaration.resolveBinding().getQualifiedName()
				.startsWith("org.springframework.boot.");
	}

	private boolean isComponent(TypeDeclaration typeDeclaration) {
		return AstUtils.findAnnotation(typeDeclaration,
				"org.springframework.stereotype.Component") != null;

	}

}
