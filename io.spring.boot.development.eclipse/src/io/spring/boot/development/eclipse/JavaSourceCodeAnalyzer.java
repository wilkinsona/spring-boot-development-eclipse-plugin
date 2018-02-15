/*
 * Copyright 2016-2018 the original author or authors
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.spring.boot.development.eclipse;

import io.spring.boot.development.eclipse.visitors.AstVisitors;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;

/**
 * An {@link IResourceVisitor} that analyzes Java source code.
 *
 * @author Andy Wilkinson
 */
class JavaSourceCodeAnalyzer implements IResourceVisitor {

	@Override
	public boolean visit(IResource resource) throws CoreException {
		if (resource instanceof IFile && resource.getName().endsWith(".java")) {
			visitCompilationUnit(resource);
		}
		return true;
	}

	private void visitCompilationUnit(IResource resource) {
		CompilationUnit compilationUnit = parse(
				(ICompilationUnit) JavaCore.create(resource));
		for (ASTVisitor visitor : new AstVisitors(resource)) {
			compilationUnit.accept(visitor);
		}
	}

	private CompilationUnit parse(ICompilationUnit source) {
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(source);
		parser.setResolveBindings(true);
		return (CompilationUnit) parser.createAST(null);
	}

}