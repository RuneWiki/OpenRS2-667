package org.openrs2.archive.cache

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import com.github.ajalt.clikt.parameters.types.path
import com.google.inject.Guice
import kotlinx.coroutines.runBlocking
import org.openrs2.archive.ArchiveModule
import org.openrs2.cache.Store
import org.openrs2.cli.instant

public class ImportCommand : CliktCommand(name = "import") {
    private val build by option().int()
    private val timestamp by option().instant()

    private val game by argument()
    private val input by argument().path(
        mustExist = true,
        canBeFile = false,
        mustBeReadable = true
    )

    override fun run(): Unit = runBlocking {
        val injector = Guice.createInjector(ArchiveModule)
        val importer = injector.getInstance(CacheImporter::class.java)

        Store.open(input).use { store ->
            importer.import(store, game, build, timestamp)
        }
    }
}
