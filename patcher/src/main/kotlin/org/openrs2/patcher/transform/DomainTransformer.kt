package org.openrs2.patcher.transform

import com.github.michaelbull.logging.InlineLogger
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.LdcInsnNode
import org.objectweb.asm.tree.MethodNode
import org.openrs2.asm.classpath.ClassPath
import org.openrs2.asm.classpath.Library
import org.openrs2.asm.transform.Transformer
import org.openrs2.conf.Config

@Singleton
public class DomainTransformer @Inject constructor(
    private val config: Config
) : Transformer() {
    private var domains = 0

    override fun preTransform(classPath: ClassPath) {
        domains = 0
    }

    override fun transformCode(classPath: ClassPath, library: Library, clazz: ClassNode, method: MethodNode): Boolean {
        for (insn in method.instructions) {
            if (insn !is LdcInsnNode) {
                continue
            }

            val cst = insn.cst
            if (cst !is String) {
                continue
            }

            insn.cst = cst.replace("runescape.com", config.domain)
            if (insn.cst != cst) {
                domains++
            }
        }

        return false
    }

    override fun postTransform(classPath: ClassPath) {
        logger.info { "Replaced $domains domains" }
    }

    private companion object {
        private val logger = InlineLogger()
    }
}
