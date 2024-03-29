package org.openrs2.deob.bytecode.transform

import com.github.michaelbull.logging.InlineLogger
import jakarta.inject.Singleton
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.MethodNode
import org.openrs2.asm.InsnMatcher
import org.openrs2.asm.MemberRef
import org.openrs2.asm.classpath.ClassPath
import org.openrs2.asm.classpath.Library
import org.openrs2.asm.hasCode
import org.openrs2.asm.transform.Transformer

@Singleton
public class CounterTransformer : Transformer() {
    private val counters = mutableSetOf<MemberRef>()

    override fun preTransform(classPath: ClassPath) {
        counters.clear()

        val references = mutableMapOf<MemberRef, Int>()
        val resets = mutableMapOf<MemberRef, Int>()
        val increments = mutableMapOf<MemberRef, Int>()

        for (library in classPath.libraries) {
            for (clazz in library) {
                for (method in clazz.methods) {
                    if (method.hasCode) {
                        findCounters(method, references, resets, increments)
                    }
                }
            }
        }

        deleteCounters(classPath, references, resets, increments)
    }

    private fun findCounters(
        method: MethodNode,
        references: MutableMap<MemberRef, Int>,
        resets: MutableMap<MemberRef, Int>,
        increments: MutableMap<MemberRef, Int>
    ) {
        for (insn in method.instructions) {
            if (insn is FieldInsnNode) {
                references.merge(MemberRef(insn), 1, Integer::sum)
            }
        }

        for (match in RESET_PATTERN.match(method)) {
            val putstatic = MemberRef(match[1] as FieldInsnNode)
            resets.merge(putstatic, 1, Integer::sum)
        }

        for (match in INCREMENT_PATTERN.match(method)) {
            val getstatic = MemberRef(match[0] as FieldInsnNode)
            val putstatic = MemberRef(match[3] as FieldInsnNode)
            if (getstatic == putstatic) {
                increments.merge(putstatic, 1, Integer::sum)
            }
        }
    }

    private fun deleteCounters(
        classPath: ClassPath,
        references: Map<MemberRef, Int>,
        resets: Map<MemberRef, Int>,
        increments: Map<MemberRef, Int>
    ) {
        for ((counter, value) in references) {
            if (resets[counter] != 1) {
                continue
            }

            // one for the reset, two for each increment
            val counterIncrements = increments[counter] ?: 0
            if (value != counterIncrements * 2 + 1) {
                continue
            }

            val owner = classPath.getClassNode(counter.owner)!!
            owner.fields.removeIf { it.name == counter.name && it.desc == counter.desc }
            counters.add(counter)
        }
    }

    override fun transformCode(
        classPath: ClassPath,
        library: Library,
        clazz: ClassNode,
        method: MethodNode
    ): Boolean {
        for (match in RESET_PATTERN.match(method)) {
            val putstatic = match[1] as FieldInsnNode
            if (MemberRef(putstatic) in counters) {
                match.forEach(method.instructions::remove)
            }
        }

        for (match in INCREMENT_PATTERN.match(method)) {
            val getstatic = MemberRef(match[0] as FieldInsnNode)
            val putstatic = MemberRef(match[3] as FieldInsnNode)
            if (getstatic == putstatic && putstatic in counters) {
                match.forEach(method.instructions::remove)
            }
        }

        return false
    }

    override fun postTransform(classPath: ClassPath) {
        logger.info { "Removed ${counters.size} counters" }
    }

    private companion object {
        private val logger = InlineLogger()
        private val RESET_PATTERN = InsnMatcher.compile("ICONST_0 PUTSTATIC")
        private val INCREMENT_PATTERN = InsnMatcher.compile("GETSTATIC ICONST_1 IADD PUTSTATIC")
    }
}
