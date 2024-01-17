package org.openrs2.deob.ast

import com.github.michaelbull.logging.InlineLogger
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.openrs2.deob.ast.transform.Transformer
import org.openrs2.deob.bytecode.Profile
import org.openrs2.deob.util.Module

@Singleton
public class AstDeobfuscator @Inject constructor(
    allTransformers: Set<Transformer>,
    private val profile: Profile,
) {
    private val allTransformersByName = allTransformers.associateBy(Transformer::name)

    public fun run(modules: Set<Module>) {
        // read list of enabled transformers and their order from the profile
        val transformers = profile.sourceTransformers.map { name ->
            allTransformersByName[name] ?: throw IllegalArgumentException("Unknown transformer $name")
        }

        val group = LibraryGroup(modules.map(Library.Companion::parse))

        for (transformer in transformers) {
            logger.info { "Running source transformer ${transformer.javaClass.simpleName}" }
            transformer.transform(group)
        }

        group.forEach(Library::save)
    }

    private companion object {
        private val logger = InlineLogger()
    }
}
