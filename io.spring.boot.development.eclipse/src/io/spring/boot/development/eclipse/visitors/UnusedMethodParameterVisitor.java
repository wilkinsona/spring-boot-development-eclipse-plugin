/*
 * Copyright 2016-2018 the original author or authors
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.spring.boot.development.eclipse.visitors;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import io.spring.boot.development.eclipse.JavaElementUtils;
import io.spring.boot.development.eclipse.Problem;
import io.spring.boot.development.eclipse.ProblemReporter;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

/**
 * An {@link ASTVisitor} that warns about unused method parameters.
 *
 * @author Andy Wilkinson
 */
class UnusedMethodParameterVisitor extends ASTVisitor {

	private final ProblemReporter problemReporter;

	UnusedMethodParameterVisitor(ProblemReporter problemReporter) {
		this.problemReporter = problemReporter;
	}

	@Override
	public boolean visit(CompilationUnit compilationUnit) {
		return JavaElementUtils.isInSrcMainJava(compilationUnit.getJavaElement());
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean visit(MethodDeclaration method) {
		if (method.getBody() == null) {
			return false;
		}
		IMethodBinding methodBinding = method.resolveBinding();
		if (methodBinding == null || isMethodOverridable(methodBinding)
				|| isMethodOverriding(methodBinding)) {
			return false;
		}
		SimpleNameVariableBindingCollector collector = new SimpleNameVariableBindingCollector();
		method.getBody().accept(collector);
		for (SingleVariableDeclaration parameter : (List<SingleVariableDeclaration>) method
				.parameters()) {
			IVariableBinding parameterBinding = parameter.resolveBinding();
			if (!collector.getBindings().contains(parameterBinding)) {
				this.problemReporter.warning(Problem.UNUSED_METHOD_PARAMETER, parameter);
			}
		}
		return false;
	}

	private boolean isMethodOverridable(IMethodBinding methodBinding) {
		ITypeBinding declaringClass = methodBinding.getDeclaringClass();
		if (Modifier.isFinal(declaringClass.getModifiers())
				|| Modifier.isFinal(methodBinding.getModifiers())) {
			return false;
		}
		return (Modifier.isPublic(methodBinding.getModifiers())
				|| Modifier.isProtected(methodBinding.getModifiers()));
	}

	private boolean isMethodOverriding(IMethodBinding methodBinding) {
		return isMethodOverriding(methodBinding, methodBinding.getDeclaringClass());
	}

	private boolean isMethodOverriding(IMethodBinding method, ITypeBinding type) {
		if (type == null) {
			return false;
		}
		for (ITypeBinding implementedInterface : type.getInterfaces()) {
			if (isMethodOverriding(method, implementedInterface)) {
				return true;
			}
		}
		for (IMethodBinding classMethod : type.getDeclaredMethods()) {
			if (method.overrides(classMethod)) {
				return true;
			}
		}
		return isMethodOverriding(method, type.getSuperclass());
	}

	private static final class SimpleNameVariableBindingCollector extends ASTVisitor {

		private List<IVariableBinding> bindings = new ArrayList<>();

		@Override
		public boolean visit(SimpleName name) {
			IBinding nameBinding = name.resolveBinding();
			if (nameBinding instanceof IVariableBinding) {
				this.bindings.add((IVariableBinding) nameBinding);
			}
			return true;
		}

		private List<IVariableBinding> getBindings() {
			return this.bindings;
		}

	}

}
