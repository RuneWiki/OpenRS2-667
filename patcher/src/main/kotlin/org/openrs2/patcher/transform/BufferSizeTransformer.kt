package org.openrs2.patcher.transform

import com.github.michaelbull.logging.InlineLogger
import jakarta.inject.Singleton
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldInsnNode
import org.objectweb.asm.tree.LdcInsnNode
import org.objectweb.asm.tree.MethodNode
import org.openrs2.asm.InsnMatcher
import org.openrs2.asm.MemberRef
import org.openrs2.asm.classpath.ClassPath
import org.openrs2.asm.classpath.Library
import org.openrs2.asm.hasCode
import org.openrs2.asm.toAbstractInsnNode
import org.openrs2.asm.transform.Transformer

@Singleton
public class BufferSizeTransformer : Transformer() {
    private var buffer: MemberRef? = null
    private var buffersResized = 0

    override fun preTransform(classPath: ClassPath) {
        buffer = null
        buffersResized = 0

        for (library in classPath.libraries) {
            for (clazz in library) {
                for (method in clazz.methods) {
                    if (!method.hasCode) {
                        continue
                    }

                    this.buffer = findBuffer(method) ?: continue
                    logger.info { "Identified buffer ${this.buffer}" }
                    break
                }
            }
        }
    }

    override fun transformCode(classPath: ClassPath, library: Library, clazz: ClassNode, method: MethodNode): Boolean {
        if (buffer == null) {
            return false
        }

        for (match in NEW_BUFFER_MATCHER.match(method)) {
            val putstatic = match[4] as FieldInsnNode
            if (MemberRef(putstatic) == buffer!!) {
                method.instructions[match[2]] = 65536.toAbstractInsnNode()
                buffersResized++
            }
        }

        return false
    }

    override fun postTransform(classPath: ClassPath) {
        logger.info { "Resized $buffersResized buffers to 65536 bytes" }
    }

    private companion object {
        private val logger = InlineLogger()
        private val GPP1_POS_MATCHER = InsnMatcher.compile("LDC (INVOKESPECIAL | INVOKEVIRTUAL) GETSTATIC")
        private val NEW_BUFFER_MATCHER = InsnMatcher.compile("NEW DUP (SIPUSH | LDC) INVOKESPECIAL PUTSTATIC")

        private fun findBuffer(method: MethodNode): MemberRef? {
            return GPP1_POS_MATCHER.match(method).filter {
                val ldc = it[0] as LdcInsnNode
                ldc.cst == "gpp1 pos:"
            }.map {
                val getstatic = it[2] as FieldInsnNode
                MemberRef(getstatic)
            }.firstOrNull()
        }
    }
}
