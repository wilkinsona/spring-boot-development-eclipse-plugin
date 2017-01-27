/*
 * Copyright 2016-2017 the original author or authors.
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
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.TypeDeclaration;

/**
 * An {@link ASTVisitor} that reports a warning when a public or protected functional
 * interface is not annotated with {@link FunctionalInterface}.
 *
 * @author Andy Wilkinson
 */
class MissingFunctionalInterfaceVisitor extends ASTVisitor {

	private final ProblemReporter problemReporter;

	MissingFunctionalInterfaceVisitor(ProblemReporter problemReporter) {
		this.problemReporter = problemReporter;
	}

	@Override
	public boolean visit(TypeDeclaration type) {
		if (isMainCode(type) && isFunctionalInterface(type) && isPublicOrProtected(type)
				&& isNotAnnotatedWithFunctionalInterface(type)) {
			this.problemReporter.warning(Problem.MISSING_FUNCTIONAL_INTERFACE_ANNOTATION,
					type.getName());
		}
		return true;
	}

	private boolean isMainCode(TypeDeclaration type) {
		return JavaElementUtils.isInSrcMainJava(type.resolveBinding().getJavaElement());
	}

	private boolean isPublicOrProtected(TypeDeclaration type) {
		return Modifier.isPublic(type.getModifiers())
				|| Modifier.isProtected(type.getModifiers());
	}

	private boolean isFunctionalInterface(TypeDeclaration type) {
		return type.isInterface()
				&& type.resolveBinding().getFunctionalInterfaceMethod() != null;
	}

	private boolean isNotAnnotatedWithFunctionalInterface(TypeDeclaration type) {
		return AstUtils.findAnnotation(type, FunctionalInterface.class.getName()) == null;
	}

}
