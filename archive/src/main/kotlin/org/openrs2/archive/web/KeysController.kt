package org.openrs2.archive.web

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.thymeleaf.ThymeleafContent
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.openrs2.archive.key.KeyExporter
import org.openrs2.archive.key.KeyImporter
import org.openrs2.archive.key.KeySource
import org.openrs2.crypto.SymmetricKey

@Singleton
public class KeysController @Inject constructor(
    private val importer: KeyImporter,
    private val exporter: KeyExporter
) {
    public suspend fun index(call: ApplicationCall) {
        val stats = exporter.count()
        val analysis = exporter.analyse()
        call.respond(ThymeleafContent("keys/index.html", mapOf("stats" to stats, "analysis" to analysis)))
    }

    public suspend fun import(call: ApplicationCall) {
        val keys = call.receive<Array<IntArray>>().mapTo(mutableSetOf(), SymmetricKey::fromIntArray)

        if (keys.isNotEmpty()) {
            importer.import(keys, KeySource.API)
        }

        call.respond(HttpStatusCode.NoContent)
    }

    public suspend fun exportAll(call: ApplicationCall) {
        call.respond(exporter.exportAll())
    }

    public suspend fun exportValid(call: ApplicationCall) {
        call.respond(exporter.exportValid())
    }
}
