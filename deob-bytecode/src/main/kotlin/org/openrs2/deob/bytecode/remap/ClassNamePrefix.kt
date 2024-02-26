package org.openrs2.deob.bytecode.remap

import org.openrs2.asm.classpath.ExtendedRemapper
import org.openrs2.asm.classpath.Library

private val BOUNDARY_CHARS = charArrayOf('/', '!')

public fun String.splitAtLibraryBoundary(): Pair<String, String> {
    val index = indexOf('!')
    return Pair(substring(0, index), substring(index + 1))
}

public fun String.getLibraryAndPackageName(): String {
    return substring(0, lastIndexOfAny(BOUNDARY_CHARS) + 1)
}

public fun String.getClassName(): String {
    return substring(lastIndexOfAny(BOUNDARY_CHARS) + 1)
}

public class ClassNamePrefixRemapper(vararg libraries: Library) : ExtendedRemapper() {
    private val mapping = mutableMapOf<String, String>()

    init {
        for (library in libraries) {
            for (clazz in library) {
                require(!clazz.name.contains('!')) {
                    "Input class name contains !, which conflicts with library separator"
                }
                mapping.putIfAbsent(clazz.name, "${library.name}!${clazz.name}")
            }
        }
    }

    override fun map(internalName: String): String {
        return mapping.getOrDefault(internalName, internalName)
    }
}

public class DefaultPackagePrefixRemapper(packageName: String, vararg libraries: Library) : ExtendedRemapper() {
    private val mapping = mutableMapOf<String, String>()

    init {
        for (library in libraries) {
            for (clazz in library) {
                // since the class was already mapped as library!class we need to do 2 things
                // 1. split the library and class name
                // 2. if there is no package name (default package) we add one for com/jagex/ before the class name
                // so the end result is library!packageName/class
                val (libraryName, className) = clazz.name.splitAtLibraryBoundary()
                val mappedName = if (className.contains('/')) {
                    clazz.name
                } else {
                    "$libraryName!${packageName}/$className"
                }
                mapping[clazz.name] = mappedName
            }
        }
    }

    override fun map(internalName: String): String {
        return mapping.getOrDefault(internalName, internalName)
    }
}

public object StripClassNamePrefixRemapper : ExtendedRemapper() {
    override fun map(internalName: String): String {
        return internalName.substring(internalName.indexOf('!') + 1)
    }
}
