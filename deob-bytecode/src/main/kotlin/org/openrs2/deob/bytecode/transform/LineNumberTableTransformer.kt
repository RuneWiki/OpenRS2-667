package org.openrs2.deob.bytecode.transform

import jakarta.inject.Singleton
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.LineNumberNode
import org.objectweb.asm.tree.MethodNode
import org.openrs2.asm.classpath.ClassPath
import org.openrs2.asm.classpath.Library
import org.openrs2.asm.transform.Transformer

@Singleton
public class LineNumberTableTransformer : Transformer() {
    override fun transformClass(classPath: ClassPath, library: Library, clazz: ClassNode): Boolean {
        clazz.methods.sortWith(LINE_NUMBER_COMPARATOR)
        return false
    }

    private companion object {
        // LineNumberTable gives us insight into the pre-obfuscation order of methods and doesn't change between revs
        private val LINE_NUMBER_COMPARATOR = Comparator<MethodNode> { a, b ->
            val aFirst = a.instructions.firstOrNull { it is LineNumberNode } as LineNumberNode?
            val bFirst = b.instructions.firstOrNull { it is LineNumberNode } as LineNumberNode?

            if (aFirst != null && bFirst != null) {
                aFirst.line - bFirst.line
            } else if (aFirst != null) {
                -1
            } else if (bFirst != null) {
                1
            } else {
                0
            }
        }
    }
}
