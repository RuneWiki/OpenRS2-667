package org.openrs2.deob.bytecode.transform

import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.openrs2.asm.classpath.ClassPath
import org.openrs2.asm.transform.Transformer
import org.openrs2.deob.bytecode.Profile
import org.openrs2.deob.bytecode.remap.DefaultPackagePrefixRemapper
import org.openrs2.deob.bytecode.remap.TypedRemapper
import org.openrs2.deob.util.map.NameMap

@Singleton
public class RemapTransformer @Inject constructor(
    private val profile: Profile,
    private val nameMap: NameMap
) : Transformer() {
    override fun preTransform(classPath: ClassPath) {
        classPath.remap(TypedRemapper.create(classPath, profile, nameMap))

        for (library in classPath.libraries) {
            val defaultPackage = profile.libraries[library.name]?.defaultPackage
            if (defaultPackage != null) {
                library.remap(DefaultPackagePrefixRemapper(defaultPackage, library))
            }
        }
    }
}
