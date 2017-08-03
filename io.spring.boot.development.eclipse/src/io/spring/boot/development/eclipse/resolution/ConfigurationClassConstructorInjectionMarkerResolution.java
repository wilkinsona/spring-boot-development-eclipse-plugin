/*
 * Copyright 2016-2017 the original author or authors
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.spring.boot.development.eclipse.resolution;

import java.util.ArrayList;
import java.util.List;

import io.spring.boot.development.eclipse.visitors.AstUtils;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.ui.IMarkerResolution;

/**
 * An {@link IMarkerResolution} that rewrites a {@code Configuration} class to use
 * constructor injection.
 *
 * @author Andy Wilkinson
 */
class ConfigurationClassConstructorInjectionMarkerResolution
		extends CompilationUnitRewritingMarkerResolution {

	@Override
	public String getLabel() {
		return "Use constructor injection";
	}

	@Override
	protected boolean resolveMarker(CompilationUnit compilationUnit, ASTNode markedNode) {
		TypeDeclaration declaringType = AstUtils.findAncestor(markedNode,
				TypeDeclaration.class);
		if (declaringType == null) {
			return false;
		}
		convertToConstructorInjection(compilationUnit, declaringType);
		return true;
	}

	@SuppressWarnings("unchecked")
	private void convertToConstructorInjection(CompilationUnit compilationUnit,
			TypeDeclaration type) {
		AST ast = type.getAST();
		MethodDeclaration constructor = createConstructor(type);
		Block body = ast.newBlock();
		List<AutowiredField> autowiredFields = findAutowiredFields(type);
		for (AutowiredField autowiredField : autowiredFields) {
			SingleVariableDeclaration argument = createArgument(compilationUnit,
					autowiredField);
			constructor.parameters().add(argument);
			makeFinal(autowiredField);
			body.statements().add(ast.newExpressionStatement(
					createFieldAssignment(autowiredField, argument)));
			autowiredField.getAutowired().delete();
		}
		constructor.setBody(body);
		int lastField = -1;
		for (int i = 0; i < type.bodyDeclarations().size(); i++) {
			if (type.bodyDeclarations().get(i) instanceof FieldDeclaration) {
				lastField = i;
			}
		}
		type.bodyDeclarations().add(lastField + 1, constructor);
	}

	@SuppressWarnings("unchecked")
	private MethodDeclaration createConstructor(TypeDeclaration type) {
		AST ast = type.getAST();
		MethodDeclaration method = ast.newMethodDeclaration();
		method.setConstructor(true);
		method.setName(ast.newSimpleName(type.getName().getIdentifier()));
		method.modifiers().add(ast.newModifier(ModifierKeyword.PUBLIC_KEYWORD));
		return method;
	}

	private List<AutowiredField> findAutowiredFields(TypeDeclaration type) {
		List<AutowiredField> autowiredFields = new ArrayList<AutowiredField>();
		for (FieldDeclaration field : type.getFields()) {
			Annotation autowired = findAutowiredAnnotation(field);
			if (autowired != null) {
				autowiredFields.add(new AutowiredField(field, autowired));
			}
		}
		return autowiredFields;
	}

	private Assignment createFieldAssignment(AutowiredField field,
			SingleVariableDeclaration argument) {
		AST ast = argument.getAST();
		Assignment assignment = ast.newAssignment();
		FieldAccess fieldAccess = ast.newFieldAccess();
		fieldAccess.setExpression(ast.newThisExpression());
		fieldAccess.setName(ast.newSimpleName(field.getName()));
		assignment.setLeftHandSide(fieldAccess);
		if (dependencyIsOptional(field.getAutowired())) {
			MethodInvocation methodInvocation = ast.newMethodInvocation();
			methodInvocation
					.setExpression(ast.newName(argument.getName().getIdentifier()));
			methodInvocation.setName(ast.newSimpleName("getIfAvailable"));
			assignment.setRightHandSide(methodInvocation);
		}
		else {
			assignment.setRightHandSide(ast.newName(argument.getName().getIdentifier()));
		}
		return assignment;
	}

	@SuppressWarnings("unchecked")
	private void makeFinal(AutowiredField autowiredField) {
		autowiredField.getField().modifiers().add(autowiredField.getField().getAST()
				.newModifier(ModifierKeyword.FINAL_KEYWORD));
	}

	@SuppressWarnings("unchecked")
	private SingleVariableDeclaration createArgument(CompilationUnit compilationUnit,
			AutowiredField autowiredField) {
		FieldDeclaration field = autowiredField.getField();
		AST ast = field.getAST();
		SingleVariableDeclaration argument = ast.newSingleVariableDeclaration();
		Annotation autowired = autowiredField.getAutowired();
		boolean optionalDependency = dependencyIsOptional(autowired);
		String name = autowiredField.getName();
		if (optionalDependency) {
			ParameterizedType argumentType = ast.newParameterizedType(
					ast.newSimpleType(ast.newSimpleName("ObjectProvider")));
			argumentType.typeArguments()
					.add(ASTNode.copySubtree(ast, autowiredField.getField().getType()));
			argument.setType(argumentType);
			addImportIfNecessary(compilationUnit,
					"org.springframework.beans.factory.ObjectProvider");
			MethodInvocation methodInvocation = ast.newMethodInvocation();
			methodInvocation.setExpression(ast.newName(name));
			methodInvocation.setName(ast.newSimpleName("getIfAvailable"));
			name += "Provider";
		}
		else {
			argument.setType(
					(Type) ASTNode.copySubtree(ast, autowiredField.getField().getType()));
		}
		argument.setName(ast.newSimpleName(name));
		return argument;
	}

	@SuppressWarnings("unchecked")
	private void addImportIfNecessary(CompilationUnit compilationUnit,
			String classToImport) {
		List<ImportDeclaration> imports = compilationUnit.imports();
		for (ImportDeclaration importDeclaration : imports) {
			if (classToImport
					.equals(importDeclaration.getName().getFullyQualifiedName())) {
				return;
			}
		}
		ImportDeclaration newImport = compilationUnit.getAST().newImportDeclaration();
		newImport.setName(compilationUnit.getAST().newName(classToImport));
		imports.add(newImport);
	}

	@SuppressWarnings("unchecked")
	private boolean dependencyIsOptional(Annotation autowired) {
		if (autowired instanceof NormalAnnotation) {
			for (MemberValuePair pair : (List<MemberValuePair>) ((NormalAnnotation) autowired)
					.values()) {
				if ("required".equals(pair.getName().getIdentifier())
						&& !(Boolean) pair.getValue().resolveConstantExpressionValue()) {
					return true;
				}
			}
		}
		return false;
	}

	private Annotation findAutowiredAnnotation(FieldDeclaration field) {
		return AstUtils.findAnnotation(field,
				"org.springframework.beans.factory.annotation.Autowired");
	}

	private static class AutowiredField {

		private final FieldDeclaration field;

		private final Annotation autowired;

		private AutowiredField(FieldDeclaration field, Annotation autowired) {
			this.field = field;
			this.autowired = autowired;
		}

		public FieldDeclaration getField() {
			return this.field;
		}

		public Annotation getAutowired() {
			return this.autowired;
		}

		public String getName() {
			return ((VariableDeclarationFragment) this.field.fragments().get(0)).getName()
					.getIdentifier();
		}

	}

}
