package org.openrs2.deob.bytecode.transform

import jakarta.inject.Singleton
import org.objectweb.asm.Type
import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldNode
import org.objectweb.asm.tree.LineNumberNode
import org.objectweb.asm.tree.MethodNode
import org.openrs2.asm.classpath.ClassPath
import org.openrs2.asm.classpath.Library
import org.openrs2.asm.transform.Transformer
import org.openrs2.deob.annotation.OriginalArg
import org.openrs2.deob.annotation.OriginalClass
import org.openrs2.deob.annotation.OriginalMember

@Singleton
public class OriginalNameTransformer : Transformer() {
    override fun transformClass(classPath: ClassPath, library: Library, clazz: ClassNode): Boolean {
        if (clazz.invisibleAnnotations == null) {
            clazz.invisibleAnnotations = mutableListOf()
        }
        clazz.invisibleAnnotations.add(createClassAnnotation(clazz.name))
        return false
    }

    override fun transformField(
        classPath: ClassPath,
        library: Library,
        clazz: ClassNode,
        field: FieldNode
    ): Boolean {
        if (field.invisibleAnnotations == null) {
            field.invisibleAnnotations = mutableListOf()
        }
        field.invisibleAnnotations.add(createMemberAnnotation(clazz.name, field.name, field.desc, null))
        return false
    }

    override fun preTransformMethod(
        classPath: ClassPath,
        library: Library,
        clazz: ClassNode,
        method: MethodNode
    ): Boolean {
        if (method.name == "<clinit>") {
            return false
        }

        if (method.invisibleAnnotations == null) {
            method.invisibleAnnotations = mutableListOf()
        }
        val firstLine = method.instructions.firstOrNull { it is LineNumberNode } as LineNumberNode?
        method.invisibleAnnotations.add(createMemberAnnotation(clazz.name, method.name, method.desc, firstLine?.line))

        val args = Type.getArgumentTypes(method.desc).size
        if (method.invisibleParameterAnnotations == null) {
            method.invisibleParameterAnnotations = arrayOfNulls(args)
        }
        for (i in method.invisibleParameterAnnotations.indices) {
            var annotations = method.invisibleParameterAnnotations[i]
            if (annotations == null) {
                annotations = mutableListOf()
                method.invisibleParameterAnnotations[i] = annotations
            }
            annotations.add(createArgAnnotation(i))
        }

        return false
    }

    private companion object {
        private fun createClassAnnotation(name: String): AnnotationNode {
            val annotation = AnnotationNode(Type.getDescriptor(OriginalClass::class.java))
            annotation.values = listOf("value", name)
            return annotation
        }

        private fun createMemberAnnotation(owner: String, name: String, desc: String, line: Int?): AnnotationNode {
            val annotation = AnnotationNode(Type.getDescriptor(OriginalMember::class.java))
            if (line != null) {
                annotation.values = listOf(
                    "owner", owner,
                    "name", name,
                    "descriptor", desc,
                    "line", line
                )
            } else {
                annotation.values = listOf(
                    "owner", owner,
                    "name", name,
                    "descriptor", desc
                )
            }
            return annotation
        }

        private fun createArgAnnotation(index: Int): AnnotationNode {
            val annotation = AnnotationNode(Type.getDescriptor(OriginalArg::class.java))
            annotation.values = listOf("value", index)
            return annotation
        }
    }
}
