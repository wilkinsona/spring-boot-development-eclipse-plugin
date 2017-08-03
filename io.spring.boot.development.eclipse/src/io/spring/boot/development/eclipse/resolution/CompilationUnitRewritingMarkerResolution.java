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
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setSource(source);
		parser.setResolveBindings(true);
		return (CompilationUnit) parser.createAST(null);
	}

	protected abstract boolean resolveMarker(CompilationUnit compilationUnit,
			ASTNode markedNode);

}
