/*
 * Copyright 2016-2018 the original author or authors
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.spring.boot.development.eclipse.visitors;

import java.util.List;

import io.spring.boot.development.eclipse.Problem;
import io.spring.boot.development.eclipse.ProblemReporter;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TextElement;

/**
 * An {@link ASTVisitor} that validates that an {@code @link} to an annotation use
 * {@code @$AnnotationName} as the link's text.
 *
 * @author Andy Wilkinson
 */
class JavadocLinkToAnnotationValidatingVisitor extends ASTVisitor {

	private final ProblemReporter problemReporter;

	JavadocLinkToAnnotationValidatingVisitor(ProblemReporter problemReporter) {
		this.problemReporter = problemReporter;
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean visit(Javadoc node) {
		visit(node.tags());
		return true;
	}

	@SuppressWarnings("unchecked")
	private void visit(List<Object> elements) {
		for (Object element : elements) {
			if (element instanceof TagElement) {
				TagElement tag = (TagElement) element;
				if (TagElement.TAG_LINK.equals(tag.getTagName())) {
					visitLink(tag);
				}
				visit(tag.fragments());
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void visitLink(TagElement link) {
		List<Object> fragments = link.fragments();
		if (!fragments.isEmpty()) {
			Object first = fragments.get(0);
			if (first instanceof Name) {
				IBinding nameBinding = ((Name) first).resolveBinding();
				if (isAnnotationName(nameBinding)) {
					String annotationName = nameBinding.getName();
					if (!linkTextIsCorrect(fragments, annotationName)) {
						this.problemReporter.warning(Problem.INCORRECT_ANNOTATION_LINK,
								link, annotationName, annotationName);
					}
				}
			}
		}
	}

	private boolean linkTextIsCorrect(List<Object> fragments, String annotationName) {
		if (fragments.size() == 1) {
			return false;
		}
		Object possibleText = fragments.get(1);
		if (possibleText instanceof TextElement) {
			String text = ((TextElement) possibleText).getText().trim();
			return text.equals("@" + annotationName) || !text.equals(annotationName);
		}
		return true;
	}

	private boolean isAnnotationName(IBinding nameBinding) {
		return nameBinding != null && ((nameBinding.getKind() == IBinding.ANNOTATION)
				|| ((nameBinding.getKind() == IBinding.TYPE)
						&& (((ITypeBinding) nameBinding).isAnnotation())));
	}

}
