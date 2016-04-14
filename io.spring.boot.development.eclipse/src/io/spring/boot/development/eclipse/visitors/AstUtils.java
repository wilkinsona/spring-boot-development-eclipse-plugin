/*
 * Copyright 2016 the original author or authors
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package io.spring.boot.development.eclipse.visitors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.BodyDeclaration;
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
	 * Gets the fully qualified name of all of the interfaces and their super-interfaces
	 * implemented by the given {@code type} and its superclasses.
	 *
	 * @param type the type
	 * @return the fully qualified interface names
	 */
	public static List<String> getImplementedInterfaces(TypeDeclaration type) {
		return getImplementedInterfaces(type.resolveBinding());
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
