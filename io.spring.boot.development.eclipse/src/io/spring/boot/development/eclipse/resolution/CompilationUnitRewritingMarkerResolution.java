/*
 * Copyright 2016-2018 the original author or authors
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.spring.boot.development.eclipse.resolution;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.TextEdit;
import org.eclipse.ui.IMarkerResolution;

/**
 * A base class for {@link IMarkerResolution} implementations that rewrite a
 * {@link CompilationUnit}.
 *
 * @author Andy Wilkinson
 */
abstract class CompilationUnitRewritingMarkerResolution implements IMarkerResolution {

	@Override
	public void run(IMarker marker) {
		try {
			ICompilationUnit sourceUnit = (ICompilationUnit) JavaCore
					.create(marker.getResource());
			CompilationUnit compilationUnit = parse(sourceUnit);
			compilationUnit.recordModifications();
			int start = (int) marker.getAttribute(IMarker.CHAR_START);
			ASTNode node = NodeFinder.perform(compilationUnit, start,
					((int) marker.getAttribute(IMarker.CHAR_END)) - start);
			if (resolveMarker(compilationUnit, node)) {
				Document document = new Document(sourceUnit.getSource());
				TextEdit changes = compilationUnit.rewrite(document, null);
				changes.apply(document);
				sourceUnit.getBuffer().setContents(document.get());
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private CompilationUnit parse(ICompilationUnit source) {
		ASTParser parser = ASTParser.newParser(AST.JLS10);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(source);
		parser.setResolveBindings(true);
		return (CompilationUnit) parser.createAST(null);
	}

	protected abstract boolean resolveMarker(CompilationUnit compilationUnit,
			ASTNode markedNode);

}
