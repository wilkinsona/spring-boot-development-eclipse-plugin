/*
 * Copyright 2016-2018 the original author or authors
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.spring.boot.development.eclipse.visitors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.TypeDeclaration;

/**
 * Utility methods for working with JDT's AST.
 *
 * @author Andy Wilkinson
 */
public final class AstUtils {

	private AstUtils() {

	}

	/**
	 * Finds the annotation with the given {@code className} declared on the given
	 * {@code bodyDeclaration}.
	 *
	 * @param bodyDeclaration the body declaration
	 * @param className the class name
	 * @return the matching annotation, or {@code null}
	 */
	@SuppressWarnings("unchecked")
	public static Annotation findAnnotation(BodyDeclaration bodyDeclaration,
			String className) {
		for (IExtendedModifier modifier : (List<IExtendedModifier>) bodyDeclaration
				.modifiers()) {
			if (modifier.isAnnotation()) {
				Annotation annotation = ((Annotation) modifier);
				if (className.equals(findQualifiedTypeName(annotation))) {
					return annotation;
				}
			}
		}
		return null;
	}

	/**
	 * Returns true if the given {@code bodyDeclaration} is annotated or meta-annotated
	 * with an annotation with the given {@code className}.
	 *
	 * @param bodyDeclaration the body declaration to examine
	 * @param className the class name of the annotation to look for
	 *
	 * @return {@code true} if the annotation is present, otherwise false.
	 */
	@SuppressWarnings("unchecked")
	public static boolean hasAnnotation(BodyDeclaration bodyDeclaration,
			String className) {
		for (IExtendedModifier modifier : (List<IExtendedModifier>) bodyDeclaration
				.modifiers()) {
			if (modifier.isAnnotation()) {
				Annotation annotation = ((Annotation) modifier);
				if (className.equals(findQualifiedTypeName(annotation))) {
					return true;
				}
				if (hasAnnotation(annotation.resolveTypeBinding(), className)) {
					return true;
				}
			}
		}
		return false;
	}

	private static boolean hasAnnotation(ITypeBinding typeBinding, String className) {
		return hasAnnotation(typeBinding, className, new HashSet<String>());
	}

	private static boolean hasAnnotation(ITypeBinding typeBinding, String className,
			Set<String> seen) {
		if (!seen.add(typeBinding.getQualifiedName())) {
			return false;
		}
		for (IAnnotationBinding annotationBinding : typeBinding.getAnnotations()) {
			if (className
					.equals(annotationBinding.getAnnotationType().getQualifiedName())) {
				return true;
			}
			if (hasAnnotation(annotationBinding.getAnnotationType(), className, seen)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Gets the fully qualified name of all of the interfaces and their super-interfaces
	 * implemented by the given {@code type} and its superclasses.
	 *
	 * @param type the type
	 * @return the fully qualified interface names
	 */
	public static List<String> getImplementedInterfaces(TypeDeclaration type) {
		ITypeBinding binding = type.resolveBinding();
		return binding == null ? Collections.emptyList()
				: getImplementedInterfaces(binding);
	}

	/**
	 * Finds the first ancestor of the given {@node} that is of the given {@code type}.
	 *
	 * @param node the node to search from
	 * @param type the type of node to search for
	 * @return the first ancestor of the required type or {@code null}
	 */
	public static <T extends ASTNode> T findAncestor(ASTNode node, Class<T> type) {
		ASTNode candidate = node.getParent();
		while (candidate != null) {
			if (type.isInstance(candidate)) {
				return type.cast(candidate);
			}
			candidate = candidate.getParent();
		}
		return null;
	}

	private static List<String> getImplementedInterfaces(ITypeBinding type) {
		List<String> implementedInterfaces = new ArrayList<String>();
		for (ITypeBinding iface : type.getInterfaces()) {
			implementedInterfaces.add(iface.getQualifiedName());
			implementedInterfaces.addAll(getImplementedInterfaces(iface));
		}
		if (type.getSuperclass() != null) {
			implementedInterfaces.addAll(getImplementedInterfaces(type.getSuperclass()));
		}
		return implementedInterfaces;
	}

	private static String findQualifiedTypeName(Annotation annotation) {
		Name name = annotation.getTypeName();
		if (name.isSimpleName()) {
			ITypeBinding typeBinding = annotation.resolveTypeBinding();
			if (typeBinding == null) {
				return null;
			}
			return typeBinding.getQualifiedName();
		}
		return name.getFullyQualifiedName();
	}

}
